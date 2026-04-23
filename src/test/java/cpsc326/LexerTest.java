package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.Test;

class LexerTest {

    private List<Token> scan(String source) {
        OurPL.hadError = false;
        return new Lexer(source).scanTokens();
    }

    private void assertToken(Token token, TokenType type, String lexeme, Object literal, int line) {
        assertEquals(type, token.type);
        assertEquals(lexeme, token.lexeme);
        assertEquals(literal, token.literal);
        assertEquals(line, token.line);
    }

    @Test
    void leftParenToken() {
        List<Token> tokens = scan("(");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.LEFT_PAREN, "(", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void rightParenToken() {
        List<Token> tokens = scan(")");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.RIGHT_PAREN, ")", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void leftBraceToken() {
        List<Token> tokens = scan("{");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.LEFT_BRACE, "{", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void rightBraceToken() {
        List<Token> tokens = scan("}");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.RIGHT_BRACE, "}", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void commaToken() {
        List<Token> tokens = scan(",");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.COMMA, ",", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void dotToken() {
        List<Token> tokens = scan(".");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.DOT, ".", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void plusToken() {
        List<Token> tokens = scan("+");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.PLUS, "+", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void minusToken() {
        List<Token> tokens = scan("-");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.MINUS, "-", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void starToken() {
        List<Token> tokens = scan("*");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.STAR, "*", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void slashToken() {
        List<Token> tokens = scan("/");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.SLASH, "/", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void semicolonToken() {
        List<Token> tokens = scan(";");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.SEMICOLON, ";", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void bangToken() {
        List<Token> tokens = scan("!");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.BANG, "!", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void bangEqualToken() {
        List<Token> tokens = scan("!=");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.BANG_EQUAL, "!=", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void equalToken() {
        List<Token> tokens = scan("=");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.EQUAL, "=", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void equalEqualToken() {
        List<Token> tokens = scan("==");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.EQUAL_EQUAL, "==", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void greaterToken() {
        List<Token> tokens = scan(">");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.GREATER, ">", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void greaterEqualToken() {
        List<Token> tokens = scan(">=");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.GREATER_EQUAL, ">=", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void lessToken() {
        List<Token> tokens = scan("<");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.LESS, "<", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void lessEqualToken() {
        List<Token> tokens = scan("<=");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.LESS_EQUAL, "<=", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void identifierToken() {
        List<Token> tokens = scan("abc");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "abc", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void stringToken() {
        List<Token> tokens = scan("\"abc\"");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.STRING, "\"abc\"", "abc", 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void numberToken() {
        List<Token> tokens = scan("12.34");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.NUMBER, "12.34", 12.34, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void andToken() {
        List<Token> tokens = scan("and");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.AND, "and", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void elseToken() {
        List<Token> tokens = scan("else");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.ELSE, "else", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void falseToken() {
        List<Token> tokens = scan("false");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.FALSE, "false", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void forToken() {
        List<Token> tokens = scan("for");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.FOR, "for", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void funToken() {
        List<Token> tokens = scan("fun");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.FUN, "fun", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void ifToken() {
        List<Token> tokens = scan("if");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.IF, "if", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void nilToken() {
        List<Token> tokens = scan("nil");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.NIL, "nil", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void orToken() {
        List<Token> tokens = scan("or");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.OR, "or", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void printToken() {
        List<Token> tokens = scan("print");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.PRINT, "print", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void returnToken() {
        List<Token> tokens = scan("return");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.RETURN, "return", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void thisToken() {
        List<Token> tokens = scan("this");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.THIS, "this", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void structToken() {
        List<Token> tokens = scan("struct");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.STRUCT, "struct", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void trueToken() {
        List<Token> tokens = scan("true");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.TRUE, "true", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void varToken() {
        List<Token> tokens = scan("var");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.VAR, "var", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void whileToken() {
        List<Token> tokens = scan("while");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.WHILE, "while", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void eofTokenOnEmptyInput() {
        List<Token> tokens = scan("");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.EOF, "", null, 1);
    }

    @Test
    void whitespaceOnlyInputProducesOnlyEof() {
        List<Token> tokens = scan("   \r\t\n  ");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.EOF, "", null, 2);
    }

    @Test
    void identifiersThatContainKeywordsRemainIdentifiers() {
        List<Token> tokens = scan("anderson");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "anderson", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void uppercaseKeywordLikeIdentifierRemainsIdentifier() {
        List<Token> tokens = scan("Print");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "Print", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void identifierMayContainUnderscoreAfterFirstCharacter() {
        List<Token> tokens = scan("abc_def");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "abc_def", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void identifierMayContainDigitsAfterFirstCharacter() {
        List<Token> tokens = scan("abc123");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "abc123", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void commentsAndWhitespaceAreSkipped() {
        List<Token> tokens = scan("var x = 1; # comment\nprint x;");
        assertEquals(9, tokens.size());
        assertToken(tokens.get(0), TokenType.VAR, "var", null, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "x", null, 1);
        assertToken(tokens.get(2), TokenType.EQUAL, "=", null, 1);
        assertToken(tokens.get(3), TokenType.NUMBER, "1", 1.0, 1);
        assertToken(tokens.get(4), TokenType.SEMICOLON, ";", null, 1);
        assertToken(tokens.get(5), TokenType.PRINT, "print", null, 2);
        assertToken(tokens.get(6), TokenType.IDENTIFIER, "x", null, 2);
        assertToken(tokens.get(7), TokenType.SEMICOLON, ";", null, 2);
        assertToken(tokens.get(8), TokenType.EOF, "", null, 2);
    }

    @Test
    void commentAtEndOfFileWithoutTrailingNewlineIsSkipped() {
        List<Token> tokens = scan("print 1; # trailing comment");
        assertEquals(4, tokens.size());
        assertToken(tokens.get(0), TokenType.PRINT, "print", null, 1);
        assertToken(tokens.get(1), TokenType.NUMBER, "1", 1.0, 1);
        assertToken(tokens.get(2), TokenType.SEMICOLON, ";", null, 1);
        assertToken(tokens.get(3), TokenType.EOF, "", null, 1);
    }

    @Test
    void commentOnlyInputProducesOnlyEof() {
        List<Token> tokens = scan("# only comment");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.EOF, "", null, 1);
    }

    @Test
    void commentThenNewlineTracksNextLineCorrectly() {
        List<Token> tokens = scan("# comment\nvar");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.VAR, "var", null, 2);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 2);
    }

    @Test
    void hashInsideStringDoesNotStartComment() {
        List<Token> tokens = scan("\"# not a comment\"");
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.STRING, "\"# not a comment\"", "# not a comment", 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void slashSlashIsTwoSlashTokensNotAComment() {
        List<Token> tokens = scan("//");
        assertEquals(3, tokens.size());
        assertToken(tokens.get(0), TokenType.SLASH, "/", null, 1);
        assertToken(tokens.get(1), TokenType.SLASH, "/", null, 1);
        assertToken(tokens.get(2), TokenType.EOF, "", null, 1);
    }

    @Test
    void splitOperatorsWithWhitespaceStaySeparate() {
        List<Token> tokens = scan("! = < >");
        assertEquals(5, tokens.size());
        assertToken(tokens.get(0), TokenType.BANG, "!", null, 1);
        assertToken(tokens.get(1), TokenType.EQUAL, "=", null, 1);
        assertToken(tokens.get(2), TokenType.LESS, "<", null, 1);
        assertToken(tokens.get(3), TokenType.GREATER, ">", null, 1);
        assertToken(tokens.get(4), TokenType.EOF, "", null, 1);
    }

    @Test
    void numberFollowedByLettersSplitsIntoNumberAndIdentifier() {
        List<Token> tokens = scan("1abc");
        assertEquals(3, tokens.size());
        assertToken(tokens.get(0), TokenType.NUMBER, "1", 1.0, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "abc", null, 1);
        assertToken(tokens.get(2), TokenType.EOF, "", null, 1);
    }

    @Test
    void multilineInputTracksLineNumbers() {
        List<Token> tokens = scan("print 1;\nprint 2;\nprint 3;");
        assertEquals(10, tokens.size());
        assertToken(tokens.get(0), TokenType.PRINT, "print", null, 1);
        assertToken(tokens.get(1), TokenType.NUMBER, "1", 1.0, 1);
        assertToken(tokens.get(2), TokenType.SEMICOLON, ";", null, 1);
        assertToken(tokens.get(3), TokenType.PRINT, "print", null, 2);
        assertToken(tokens.get(4), TokenType.NUMBER, "2", 2.0, 2);
        assertToken(tokens.get(5), TokenType.SEMICOLON, ";", null, 2);
        assertToken(tokens.get(6), TokenType.PRINT, "print", null, 3);
        assertToken(tokens.get(7), TokenType.NUMBER, "3", 3.0, 3);
        assertToken(tokens.get(8), TokenType.SEMICOLON, ";", null, 3);
        assertToken(tokens.get(9), TokenType.EOF, "", null, 3);
    }

    @Test
    void multilineStringAdvancesLineCounter() {
        List<Token> tokens = scan("\"a\nb\"\nprint");
        assertEquals(3, tokens.size());
        assertToken(tokens.get(0), TokenType.STRING, "\"a\nb\"", "a\nb", 2);
        assertToken(tokens.get(1), TokenType.PRINT, "print", null, 3);
        assertToken(tokens.get(2), TokenType.EOF, "", null, 3);
    }

    @Test
    void unterminatedStringReportsErrorAndStillEndsWithEof() {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            List<Token> tokens = scan("\"unterminated");
            assertTrue(OurPL.hadError);
            assertTrue(err.toString().contains("Unterminated string."));
            assertEquals(1, tokens.size());
            assertToken(tokens.get(0), TokenType.EOF, "", null, 1);
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void unterminatedMultilineStringReportsFinalLineAndStillEndsWithEof() {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            List<Token> tokens = scan("\"a\nb");
            assertTrue(OurPL.hadError);
            assertTrue(err.toString().contains("Unterminated string."));
            assertEquals(1, tokens.size());
            assertToken(tokens.get(0), TokenType.EOF, "", null, 2);
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void unexpectedCharacterReportsErrorAndScanningContinues() {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            List<Token> tokens = scan("@print");
            assertTrue(OurPL.hadError);
            assertTrue(err.toString().contains("Unexpected character."));
            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.PRINT, "print", null, 1);
            assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void underscoreCannotStartIdentifierInCurrentLexer() {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            List<Token> tokens = scan("_abc");
            assertTrue(OurPL.hadError);
            assertTrue(err.toString().contains("Unexpected character."));
            assertEquals(2, tokens.size());
            assertToken(tokens.get(0), TokenType.IDENTIFIER, "abc", null, 1);
            assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void decimalEdgeCasesMatchCurrentImplementation() {
        List<Token> tokens = scan("123. .5 1.2.3");
        assertEquals(8, tokens.size());
        assertToken(tokens.get(0), TokenType.NUMBER, "123", 123.0, 1);
        assertToken(tokens.get(1), TokenType.DOT, ".", null, 1);
        assertToken(tokens.get(2), TokenType.DOT, ".", null, 1);
        assertToken(tokens.get(3), TokenType.NUMBER, "5", 5.0, 1);
        assertToken(tokens.get(4), TokenType.NUMBER, "1.2", 1.2, 1);
        assertToken(tokens.get(5), TokenType.DOT, ".", null, 1);
        assertToken(tokens.get(6), TokenType.NUMBER, "3", 3.0, 1);
        assertToken(tokens.get(7), TokenType.EOF, "", null, 1);
    }

    @Test
    void doubleCharacterOperatorMatchingIsGreedy() {
        List<Token> tokens = scan("!==");
        assertEquals(3, tokens.size());
        assertToken(tokens.get(0), TokenType.BANG_EQUAL, "!=", null, 1);
        assertToken(tokens.get(1), TokenType.EQUAL, "=", null, 1);
        assertToken(tokens.get(2), TokenType.EOF, "", null, 1);
    }

    @Test
    void uppercaseLettersAreAcceptedInIdentifiers() {
        List<Token> tokens = scan("Camel");
        assertFalse(OurPL.hadError);
        assertEquals(2, tokens.size());
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "Camel", null, 1);
        assertToken(tokens.get(1), TokenType.EOF, "", null, 1);
    }

    @Test
    void complexProgramTokenization() {
        List<Token> tokens = scan("var Name = 12;\nprint Name");
        assertEquals(8, tokens.size());
        assertToken(tokens.get(0), TokenType.VAR, "var", null, 1);
        assertToken(tokens.get(1), TokenType.IDENTIFIER, "Name", null, 1);
        assertToken(tokens.get(2), TokenType.EQUAL, "=", null, 1);
        assertToken(tokens.get(3), TokenType.NUMBER, "12", 12.0, 1);
        assertToken(tokens.get(4), TokenType.SEMICOLON, ";", null, 1);
        assertToken(tokens.get(5), TokenType.PRINT, "print", null, 2);
        assertToken(tokens.get(6), TokenType.IDENTIFIER, "Name", null, 2);
        assertToken(tokens.get(7), TokenType.EOF, "", null, 2);
    }
}
