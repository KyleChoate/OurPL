package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Parser2Test {

    @BeforeEach
    void resetFlags() {
        OurPL.hadError = false;
        OurPL.hadRuntimeError = false;
    }

    private List<Token> lex(String source) {
        return new Lexer(source).scanTokens();
    }

    private List<Stmt> parse(String source) {
        OurPL.hadError = false;
        return new Parser(lex(source)).parse();
    }

    private Stmt parseSingleStatement(String source) {
        List<Stmt> statements = parse(source);
        assertNotNull(statements, "Parser returned null statement list.");
        assertEquals(1, statements.size(), "Expected exactly one statement.");
        assertNotNull(statements.get(0), "Parser returned null for valid input.");
        return statements.get(0);
    }

    private Expr parseSingleExpression(String source) {
        Stmt stmt = parseSingleStatement(source);
        if (stmt instanceof Stmt.Expression) {
            return ((Stmt.Expression) stmt).expression;
        }
        if (stmt instanceof Stmt.Print) {
            return ((Stmt.Print) stmt).expression;
        }
        throw new AssertionError("Expected expression statement, got " + stmt.getClass().getSimpleName());
    }

    private String parseToAst(String source) {
        Expr expr = parseSingleExpression(source + ";");
        assertFalse(OurPL.hadError, "Unexpected parse error for valid input.");
        return new ASTPrinter().print(expr);
    }

    private ParseOutcome parseWithCapturedErr(String source) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            return new ParseOutcome(parse(source), err.toString().trim());
        } finally {
            System.setErr(originalErr);
        }
    }

    private EvalOutcome interpret(String source) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        try {
            new Interpreter().interpret(parse(source));
            return new EvalOutcome
            (
                out.toString().replace("\r", "").trim(),
                err.toString().replace("\r", "").trim()
            );
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    private static Stream<Arguments> logicalAstCases() {
        return Stream.of(
            Arguments.of("true or false", "(or true false)"),
            Arguments.of("true and false", "(and true false)"),
            Arguments.of("true or false and false", "(or true (and false false))"),
            Arguments.of("true and false or true", "(or (and true false) true)")
        );
    }

    // Expression parsing

    @ParameterizedTest
    @MethodSource("logicalAstCases")
    void parsesLogicalExpressions(String source, String expectedAst) {
        assertEquals(expectedAst, parseToAst(source));
    }

    @Test
    void parsesVariableReferenceAsPrimaryExpression() {
        Expr expr = parseSingleExpression("answer;");
        assertTrue(expr instanceof Expr.Variable);
        assertEquals("answer", ((Expr.Variable) expr).name.lexeme);
    }

    @Test
    void parsesAssignmentExpression() {
        Expr expr = parseSingleExpression("value = 3;");
        assertTrue(expr instanceof Expr.Assign);

        Expr.Assign assign = (Expr.Assign) expr;
        assertEquals("value", assign.name.lexeme);
        assertTrue(assign.value instanceof Expr.Literal);
        assertEquals(3.0, ((Expr.Literal) assign.value).value);
    }

    @Test
    void assignmentIsRightAssociative() {
        Expr expr = parseSingleExpression("a = b = 3;");
        assertTrue(expr instanceof Expr.Assign);

        Expr.Assign outer = (Expr.Assign) expr;
        assertEquals("a", outer.name.lexeme);
        assertTrue(outer.value instanceof Expr.Assign);

        Expr.Assign inner = (Expr.Assign) outer.value;
        assertEquals("b", inner.name.lexeme);
        assertTrue(inner.value instanceof Expr.Literal);
        assertEquals(3.0, ((Expr.Literal) inner.value).value);
    }

    @Test
    void assignmentHasLowerPrecedenceThanLogicalOperators() {
        Expr expr = parseSingleExpression("flag = false or true and false;");
        assertTrue(expr instanceof Expr.Assign);

        Expr.Assign assign = (Expr.Assign) expr;
        assertEquals("flag", assign.name.lexeme);
        assertTrue(assign.value instanceof Expr.Logical);

        Expr.Logical orExpr = (Expr.Logical) assign.value;
        assertEquals(TokenType.OR, orExpr.operator.type);
        assertTrue(orExpr.left instanceof Expr.Literal);
        assertEquals(false, ((Expr.Literal) orExpr.left).value);
        assertTrue(orExpr.right instanceof Expr.Logical);

        Expr.Logical andExpr = (Expr.Logical) orExpr.right;
        assertEquals(TokenType.AND, andExpr.operator.type);
        assertTrue(andExpr.left instanceof Expr.Literal);
        assertEquals(true, ((Expr.Literal) andExpr.left).value);
        assertTrue(andExpr.right instanceof Expr.Literal);
        assertEquals(false, ((Expr.Literal) andExpr.right).value);
    }

    // Statement parsing

    @Test
    void parsesVariableDeclarationWithoutInitializer() {
        Stmt.Var stmt = (Stmt.Var) parseSingleStatement("var answer;");
        assertEquals("answer", stmt.name.lexeme);
        assertNull(stmt.initializer);
    }

    @Test
    void parsesVariableDeclarationWithInitializer() {
        Stmt.Var stmt = (Stmt.Var) parseSingleStatement("var answer = true or false;");
        assertEquals("answer", stmt.name.lexeme);
        assertNotNull(stmt.initializer);
        assertTrue(stmt.initializer instanceof Expr.Logical);
        Expr.Logical logical = (Expr.Logical) stmt.initializer;
        assertEquals(TokenType.OR, logical.operator.type);
        assertTrue(logical.left instanceof Expr.Literal);
        assertEquals(true, ((Expr.Literal) logical.left).value);
        assertTrue(logical.right instanceof Expr.Literal);
        assertEquals(false, ((Expr.Literal) logical.right).value);
    }

    @Test
    void parsesBlockContainingMixedDeclarationsAndStatements() {
        Stmt.Block block = (Stmt.Block) parseSingleStatement("{ var a = 1; print a; a = 2; }");
        assertEquals(3, block.statements.size());
        assertTrue(block.statements.get(0) instanceof Stmt.Var);
        assertTrue(block.statements.get(1) instanceof Stmt.Print);
        assertTrue(block.statements.get(2) instanceof Stmt.Expression);
        Expr expression = ((Stmt.Expression) block.statements.get(2)).expression;
        assertEquals("(assign a 2.0)", new ASTPrinter().print(expression));
    }

    @Test
    void parsesIfWithoutElse() {
        Stmt.If stmt = (Stmt.If) parseSingleStatement("if (true) print 1;");
        assertTrue(stmt.condition instanceof Expr.Literal);
        assertEquals(true, ((Expr.Literal) stmt.condition).value);
        assertTrue(stmt.thenBranch instanceof Stmt.Print);
        assertNull(stmt.elseBranch);
    }

    @Test
    void parsesIfWithElse() {
        Stmt.If stmt = (Stmt.If) parseSingleStatement("if (flag) print 1; else print 2;");
        assertTrue(stmt.condition instanceof Expr.Variable);
        assertEquals("flag", ((Expr.Variable) stmt.condition).name.lexeme);
        assertTrue(stmt.thenBranch instanceof Stmt.Print);
        assertTrue(stmt.elseBranch instanceof Stmt.Print);
    }

    @Test
    void bindsElseToNearestIf() {
        Stmt.If outer = (Stmt.If) parseSingleStatement("if (a) if (b) print 1; else print 2;");
        assertNull(outer.elseBranch);
        assertTrue(outer.thenBranch instanceof Stmt.If);
        Stmt.If inner = (Stmt.If) outer.thenBranch;
        assertTrue(inner.elseBranch instanceof Stmt.Print);
    }

    @Test
    void parsesWhileStatementStructure() {
        Stmt.While stmt = (Stmt.While) parseSingleStatement("while (x and y) print x;");
        assertTrue(stmt.condition instanceof Expr.Logical);
        Expr.Logical condition = (Expr.Logical) stmt.condition;
        assertEquals(TokenType.AND, condition.operator.type);
        assertTrue(condition.left instanceof Expr.Variable);
        assertEquals("x", ((Expr.Variable) condition.left).name.lexeme);
        assertTrue(condition.right instanceof Expr.Variable);
        assertEquals("y", ((Expr.Variable) condition.right).name.lexeme);
        assertTrue(stmt.body instanceof Stmt.Print);
    }

    @Test
    void parsesForLoopIntoBlockWithInitializerAndWhile() {
        Stmt.Block block = (Stmt.Block) parseSingleStatement("for (var i = 0; i < 2; i = i + 1) print i;");
        assertEquals(2, block.statements.size());
        assertTrue(block.statements.get(0) instanceof Stmt.Var);
        assertTrue(block.statements.get(1) instanceof Stmt.While);

        Stmt.While loop = (Stmt.While) block.statements.get(1);
        assertTrue(loop.condition instanceof Expr.Binary);
        Expr.Binary condition = (Expr.Binary) loop.condition;
        assertEquals(TokenType.LESS, condition.operator.type);
        assertTrue(condition.left instanceof Expr.Variable);
        assertEquals("i", ((Expr.Variable) condition.left).name.lexeme);
        assertTrue(condition.right instanceof Expr.Literal);
        assertEquals(2.0, ((Expr.Literal) condition.right).value);
        assertTrue(loop.body instanceof Stmt.Block);

        Stmt.Block body = (Stmt.Block) loop.body;
        assertEquals(2, body.statements.size());
        assertTrue(body.statements.get(0) instanceof Stmt.Print);
        assertTrue(body.statements.get(1) instanceof Stmt.Expression);
        Expr increment = ((Stmt.Expression) body.statements.get(1)).expression;
        assertTrue(increment instanceof Expr.Assign);
        Expr.Assign incrementAssign = (Expr.Assign) increment;
        assertEquals("i", incrementAssign.name.lexeme);
        assertTrue(incrementAssign.value instanceof Expr.Binary);
        Expr.Binary plusExpr = (Expr.Binary) incrementAssign.value;
        assertEquals(TokenType.PLUS, plusExpr.operator.type);
        assertTrue(plusExpr.left instanceof Expr.Variable);
        assertEquals("i", ((Expr.Variable) plusExpr.left).name.lexeme);
        assertTrue(plusExpr.right instanceof Expr.Literal);
        assertEquals(1.0, ((Expr.Literal) plusExpr.right).value);
    }

    @Test
    void parsesForLoopWithoutInitializerAsWhileStatement() {
        Stmt.While loop = (Stmt.While) parseSingleStatement("for (; i < 2; i = i + 1) print i;");
        assertTrue(loop.condition instanceof Expr.Binary);
        Expr.Binary condition = (Expr.Binary) loop.condition;
        assertEquals(TokenType.LESS, condition.operator.type);
        assertTrue(condition.left instanceof Expr.Variable);
        assertEquals("i", ((Expr.Variable) condition.left).name.lexeme);
        assertTrue(condition.right instanceof Expr.Literal);
        assertEquals(2.0, ((Expr.Literal) condition.right).value);
        assertTrue(loop.body instanceof Stmt.Block);
    }

    @Test
    void parsesForLoopWithExpressionInitializer() {
        Stmt.Block block = (Stmt.Block) parseSingleStatement("for (i = 0; i < 2; i = i + 1) print i;");
        assertEquals(2, block.statements.size());
        assertTrue(block.statements.get(0) instanceof Stmt.Expression);
        Expr initializer = ((Stmt.Expression) block.statements.get(0)).expression;
        assertTrue(initializer instanceof Expr.Assign);
        Expr.Assign assign = (Expr.Assign) initializer;
        assertEquals("i", assign.name.lexeme);
        assertTrue(assign.value instanceof Expr.Literal);
        assertEquals(0.0, ((Expr.Literal) assign.value).value);
        assertTrue(block.statements.get(1) instanceof Stmt.While);
    }

    @Test
    void parsesForLoopWithoutConditionAsInfiniteLoop() {
        Stmt.Block block = (Stmt.Block) parseSingleStatement("for (var i = 0; ; i = i + 1) print i;");
        Stmt.While loop = (Stmt.While) block.statements.get(1);
        assertTrue(loop.condition instanceof Expr.Literal);
        assertEquals(true, ((Expr.Literal) loop.condition).value);
    }

    @Test
    void parsesForLoopWithoutIncrementLeavingOriginalBody() {
        Stmt.Block block = (Stmt.Block) parseSingleStatement("for (var i = 0; i < 2; ) print i;");
        Stmt.While loop = (Stmt.While) block.statements.get(1);
        assertTrue(loop.body instanceof Stmt.Print);
    }

    @Test
    void parsesForLoopWithoutInitializerOrIncrementAsBareWhile() {
        Stmt.While loop = (Stmt.While) parseSingleStatement("for (; i < 2; ) print i;");
        assertTrue(loop.condition instanceof Expr.Binary);
        Expr.Binary condition = (Expr.Binary) loop.condition;
        assertEquals(TokenType.LESS, condition.operator.type);
        assertTrue(condition.left instanceof Expr.Variable);
        assertEquals("i", ((Expr.Variable) condition.left).name.lexeme);
        assertTrue(condition.right instanceof Expr.Literal);
        assertEquals(2.0, ((Expr.Literal) condition.right).value);
        assertTrue(loop.body instanceof Stmt.Print);
    }

    @Test
    void parsesForLoopWithoutAnyClausesAsInfiniteWhile() {
        Stmt.While loop = (Stmt.While) parseSingleStatement("for (;; ) print 1;");
        assertTrue(loop.condition instanceof Expr.Literal);
        assertEquals(true, ((Expr.Literal) loop.condition).value);
        assertTrue(loop.body instanceof Stmt.Print);
    }

    @Test
    void preservesBlockBodyWhenForLoopAddsIncrement() {
        Stmt.Block outer = (Stmt.Block) parseSingleStatement("for (var i = 0; i < 2; i = i + 1) { print i; print i + 10; }");
        Stmt.While loop = (Stmt.While) outer.statements.get(1);
        assertTrue(loop.body instanceof Stmt.Block);

        Stmt.Block rewrittenBody = (Stmt.Block) loop.body;
        assertEquals(2, rewrittenBody.statements.size());
        assertTrue(rewrittenBody.statements.get(0) instanceof Stmt.Block);
        assertTrue(rewrittenBody.statements.get(1) instanceof Stmt.Expression);

        Stmt.Block originalBody = (Stmt.Block) rewrittenBody.statements.get(0);
        assertEquals(2, originalBody.statements.size());
        assertTrue(originalBody.statements.get(0) instanceof Stmt.Print);
        assertTrue(originalBody.statements.get(1) instanceof Stmt.Print);
    }

    @Test
    void rejectsInvalidAssignmentTargets() {
        ParseOutcome out = parseWithCapturedErr("(a + b) = 3;");
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertTrue(out.statements.get(0) instanceof Stmt.Expression);
        Expr expr = ((Stmt.Expression) out.statements.get(0)).expression;
        assertTrue(expr instanceof Expr.Grouping);
        Expr.Grouping grouping = (Expr.Grouping) expr;
        assertTrue(grouping.expression instanceof Expr.Binary);
        Expr.Binary inner = (Expr.Binary) grouping.expression;
        assertEquals(TokenType.PLUS, inner.operator.type);
        assertTrue(inner.left instanceof Expr.Variable);
        assertEquals("a", ((Expr.Variable) inner.left).name.lexeme);
        assertTrue(inner.right instanceof Expr.Variable);
        assertEquals("b", ((Expr.Variable) inner.right).name.lexeme);
    }

    // Parse errors

    @Test
    void reportsMissingSemicolonAfterVarDeclaration() {
        ParseOutcome out = parseWithCapturedErr("var a = 1");
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertNull(out.statements.get(0));
    }

    @Test
    void reportsMissingRightBraceInBlock() {
        ParseOutcome out = parseWithCapturedErr("{ print 1;");
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertNull(out.statements.get(0));
    }

    @Test
    void reportsMissingConditionParenthesisInIfStatement() {
        ParseOutcome out = parseWithCapturedErr("if true) print 1;");
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertNull(out.statements.get(0));
    }

    @Test
    void reportsMissingWhileRightParenthesis() {
        ParseOutcome out = parseWithCapturedErr("while (true print 1;");
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertNull(out.statements.get(0));
    }

    @Test
    void reportsMissingSemicolonInForInitializer() {
        ParseOutcome out = parseWithCapturedErr("for (var i = 0 i < 2; i = i + 1) print i;");
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertNull(out.statements.get(0));
    }

    @Test
    void reportsMissingSemicolonAfterForCondition() {
        ParseOutcome out = parseWithCapturedErr("for (; i < 2 i = i + 1) print i;");
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertNull(out.statements.get(0));
    }

    @Test
    void reportsMissingRightParenAfterForClauses() {
        ParseOutcome out = parseWithCapturedErr("for (var i = 0; i < 2; i = i + 1 print i;");
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertNull(out.statements.get(0));
    }

    // Error recovery

    @Test
    void parserRecoversAfterBrokenDeclarationAndKeepsLaterStatements() {
        ParseOutcome out = parseWithCapturedErr("var x = ; print x;");
        assertTrue(OurPL.hadError);
        assertEquals(2, out.statements.size());
        assertNull(out.statements.get(0));
        assertTrue(out.statements.get(1) instanceof Stmt.Print);
    }

    @Test
    void parserRecoversAfterBrokenIfAndKeepsLaterPrint() {
        ParseOutcome out = parseWithCapturedErr("if (true print 1; print 2;");
        assertTrue(OurPL.hadError);
        assertEquals(2, out.statements.size());
        assertNull(out.statements.get(0));
        assertTrue(out.statements.get(1) instanceof Stmt.Print);
    }

    @Test
    void parserRecoversAfterBrokenDeclarationInsideBlock() {
        ParseOutcome out = parseWithCapturedErr("{ var x = ; print 1; } print 2;");
        assertTrue(OurPL.hadError);
        assertEquals(2, out.statements.size());
        assertTrue(out.statements.get(0) instanceof Stmt.Block);
        Stmt.Block block = (Stmt.Block) out.statements.get(0);
        assertEquals(2, block.statements.size());
        assertNull(block.statements.get(0));
        assertTrue(block.statements.get(1) instanceof Stmt.Print);
        assertTrue(out.statements.get(1) instanceof Stmt.Print);
    }

    // Interpreter behavior

    @Test
    void interpretsVariableDeclarationAndAssignment() {
        EvalOutcome out = interpret("var value = 1; value = value + 2; print value;");
        assertFalse(OurPL.hadError);
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("3", out.stdout);
        assertTrue(out.stderr.isEmpty());
    }

    @Test
    void interpretsBlockUsingInnerScopeWithoutLeakingBindings() {
        EvalOutcome out = interpret("var a = 1; { var a = 2; print a; } print a;");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("2\n1", out.stdout);
    }

    @Test
    void interpretsIfElseBranches() {
        EvalOutcome out = interpret("var a = false; if (a) print 1; else print 2;");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("2", out.stdout);
    }

    @Test
    void interpretsWhileLoopAndAssignmentUpdate() {
        EvalOutcome out = interpret("var i = 0; while (i < 3) { print i; i = i + 1; }");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("0\n1\n2", out.stdout);
    }

    @Test
    void interpretsForLoopDesugaredByParser() {
        EvalOutcome out = interpret("for (var i = 0; i < 3; i = i + 1) print i;");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("0\n1\n2", out.stdout);
    }

    @Test
    void interpretsForLoopWithExpressionInitializer() {
        EvalOutcome out = interpret("var i = 0; for (i = 1; i < 3; i = i + 1) print i;");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("1\n2", out.stdout);
    }

    @Test
    void logicalAndShortCircuitsDuringInterpretation() {
        EvalOutcome out = interpret("var a = false; if (a and missing) print 1; else print 2;");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("2", out.stdout);
        assertTrue(out.stderr.isEmpty());
    }

    @Test
    void logicalOrShortCircuitsDuringInterpretation() {
        EvalOutcome out = interpret("var a = true; if (a or missing) print 1;");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("1", out.stdout);
        assertTrue(out.stderr.isEmpty());
    }

    @Test
    void assignmentInsideBlockUpdatesOuterScopedVariable() {
        EvalOutcome out = interpret("var a = 1; { a = 2; } print a;");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("2", out.stdout);
    }

    @Test
    void runtimeErrorForUndefinedVariableRead() {
        EvalOutcome out = interpret("print missing;");
        assertTrue(OurPL.hadRuntimeError);
        assertTrue(out.stdout.isEmpty());
        assertFalse(out.stderr.isBlank());
    }

    @Test
    void interpretsLoopWithoutIncrementWhenBodyUpdatesConditionVariable() {
        EvalOutcome out = interpret("var i = 0; for (; i < 2; ) { print i; i = i + 1; }");
        assertFalse(OurPL.hadRuntimeError);
        assertEquals("0\n1", out.stdout);
    }

    @Test
    void runtimeErrorForUndefinedVariableAssignment() {
        EvalOutcome out = interpret("missing = 1;");
        assertTrue(OurPL.hadRuntimeError);
        assertTrue(out.stdout.isEmpty());
        assertFalse(out.stderr.isBlank());
    }

    private static final class ParseOutcome {
        final List<Stmt> statements;
        final String stderr;

        ParseOutcome(List<Stmt> statements, String stderr) {
            this.statements = statements;
            this.stderr = stderr;
        }
    }

    private static final class EvalOutcome {
        final String stdout;
        final String stderr;

        EvalOutcome(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
