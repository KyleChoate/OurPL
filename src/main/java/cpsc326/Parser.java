package cpsc326;

import java.util.ArrayList;
import java.util.List;
import static cpsc326.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException{ }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) 
    {
        this.tokens = tokens;
        // for (Token token : this.tokens){
        //     System.out.println(token);
        // }
    }

    List<Stmt> parse() 
    {
        try 
        {
            return program();
            // return expression();
        } 
        catch (ParseError error) 
        {
            return null;
        }
    }

    private List<Stmt> program()
    {
        // Initialize list of declarations
        List<Stmt> declarations = new ArrayList<>();
        // Add each declaration to list until hit EOF
        while (!isAtEnd())
        {
            Stmt declaration = declaration();
            declarations.add(declaration);
        }
        return declarations;
    }

    private Stmt declaration()
    {
        try
        {
            if (match(VAR))
                return varDecl();
            else
                return statement();
        }
        catch (ParseError error)
        {
            synchronize();
            return null;
        }
    }

    private Stmt varDecl()
    {
        // Consume Var not needed since match Var in declaration()
        // Consume IDENTIFIER
        Token name = consume(IDENTIFIER, "Expected variable name");
        // Consume '=' and expression (optional)
        Expr initializer = null;
        if (match(EQUAL))
            initializer = expression();
        // Consume semi colon
        consume(SEMICOLON, "Expected ';' after variable declaration");
        // Return result
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement()
    {
        // Peek for printStmt, exprStmt, block, ifStmt, whileStmt, or forStmt
        switch(peek().type)
        {
            case PRINT: 
                return printStmt(); 
            case LEFT_BRACE:
                return block();
            case IF:
                return ifStmt();
            case WHILE:
                return whileStmt();
            case FOR:
                return forStmt();
            default:
                return exprStmt();
        }
    }

    
    private Stmt ifStmt()
    {
        // Initialize variables
        Expr condition;
        Stmt thenBranch;
        Stmt elseBranch;
        // Consume if
        consume(IF, "Expected 'if'");
        // Consume left parenthesis
        consume(LEFT_PAREN, "Expected '('");
        // Consume expression
        condition = expression();
        consume(RIGHT_PAREN, "Expected ')'");
        // Consume thenBranch
        thenBranch = statement();
        // Consume else and elseStatement (optional)
        if (match(ELSE))
            elseBranch = statement();
        else 
            elseBranch = null;
        // Return result
        return new Stmt.If(condition, thenBranch, elseBranch);

    }

    private Stmt forStmt()
    {
        Stmt varDecl;
        Expr condition;
        Expr iterator;
        Stmt statement;
        // Initialize variables
        // Consume for
        consume(FOR, "Expected 'for'");
        // Consume left paren
        consume(LEFT_PAREN, "Expected '('");
        // Consume varDecl or exprStmt (optional)
        if (!match(SEMICOLON))
        {
            varDecl = varDecl();
            // Consume semi-colon
            consume(SEMICOLON, "Expected ';' after variable declaration");
        }
        // Consume expression/condition (optional)
        if (!match(SEMICOLON))
        {
            condition = expression();
            // Consume semi-colon
            consume(SEMICOLON, "Expected ';' after variable declaration");
        }
        // Make condition true if it is blank
        else
            condition = new Expr.Literal(new Token(TRUE, "true", null, 0));
        // Consume expression (optional)
        if (!match(SEMICOLON))
        {
            iterator = expression();
            // Consume semi-colon
            consume(SEMICOLON, "Expected ';' after iterator");
        }
        // Consome right_paren
        consume(LEFT_PAREN, "Expected ')'");
        // Consume statement
        statement = statement();
        // Consume semi-colon
        consume(SEMICOLON, "Expected ';'");
        // Generate a while
        return new Stmt.While(condition, statement);
    }

    private Stmt whileStmt()
    {
        Expr condition;
        Stmt statement;
        // consume while
        consume(WHILE, "Expected 'while'");
        // Consume left paren
        consume(LEFT_PAREN, "Expected '('");
        // Consume expression
        condition = expression();
        // Consume right paren
        consume(RIGHT_PAREN, "Expected ')'");
        // consume statement
        statement = statement();
        // Return result
        return new Stmt.While(condition, statement);
    }

    private Stmt block()
    {
        List<Stmt> declarations = new ArrayList<>();
        // Consume left paren
        consume(LEFT_BRACE, "Expected '{'");
        // Create list of declarations until you hit a right paren
        while (!match(RIGHT_PAREN))
        {
            Stmt declaration = declaration();
            declarations.add(declaration);
        }
        // Return result
        return new Stmt.Block(declarations);
    }

    private Stmt exprStmt()
    {
        // Consume expression
        Expr expr = expression();
        // consume semi colon
        consume(SEMICOLON, "Expected ';' after expression statement");
        // Return result
        return new Stmt.Expression(expr);
    }

    private Stmt printStmt()
    {
        Expr expression;
        // consume print
        consume(PRINT, "Expected 'print'");
        // Consume expression
        expression = expression();
        // Consume semicolon
        consume(SEMICOLON, "Expected ';'");
        // Return result
        return new Stmt.Print(expression);
    }

    private Expr expression() 
    {
        return assignment();
    }

    private Expr assignment()
    {
        // Consume (IDENTIFIER <assignment>) or <logic_or>
        if (match(IDENTIFIER))
        {
            Token identifier = previous();

            if (match(EQUAL))
                return new Expr.Assign(identifier, assignment());
            else
                return new Expr.Variable(previous());
        } 
        else
            return logic_or();
    }


    private Expr logic_or()
    {
        Expr left;
        Token operator;
        Expr right;
        // Consume <logic_and>
        left = logic_and();
        // Consume (or <logic_and>) optionally infinitely many times
        while (match(OR))
        {
            operator = previous();
            right = logic_and();
            left = new Expr.Logical(left, operator, right);
        }
        // Return result
        return left;
    }

    private Expr logic_and()
    {
        Expr left;
        Token operator;
        Expr right;
        // Consume <equality>
        left = equality();
        // Consume (and <equality>) optionally infinitely many times
        while (match(AND))
        {
            operator = previous();
            right = equality();
            left = new Expr.Logical(left, operator, right);
        }
        // Return result
        return left;
    }

    // I discovered issues with my initial recursive method and I couldn't
    // find a satisfying issue. Needless to say, I learned a lot from it
    // Bummer I couldn't find a recursive method
    private Expr equality() // (BANG_EQUAL, EQUAL_EQUAL)
    {
        Expr left;
        Token operator;
        Expr right;

        left = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL))
        {
            operator = previous();
            right = comparison();
            left = new Expr.Binary(left, operator, right);
        } 
        return left;
    }

    private Expr comparison()  // (GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
    {
        Expr left;
        Token operator;
        Expr right;

        left = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL))
        {
            operator = previous();
            right = term();
            left = new Expr.Binary(left, operator, right);
        } 
        return left;
    }

    private Expr term() 
    {
        Expr left;
        Token operator;
        Expr right;

        left = factor();

        while (match(PLUS, MINUS))
        {
            operator = previous();
            right = factor();
            left = new Expr.Binary(left, operator, right);
        } 
        return left;
    }

    private Expr factor() 
    {
        Expr left;
        Token operator;
        Expr right;

        left = unary();

        while (match(SLASH, STAR))
        {
            operator = previous();
            right = unary();
            left = new Expr.Binary(left, operator, right);
        } 
        return left;
    }

    private Expr unary() 
    {
        Token operator;
        Expr right;

        if (match(MINUS,BANG))
        {
            operator = previous();
            right = unary();
            return new Expr.Unary(operator, right);
        }
            
        return primary();
    }

    private Expr primary() 
    {
        if (match(NUMBER, STRING, TRUE, FALSE, NIL))
            return new Expr.Literal(previous());  
        
        if (match(LEFT_PAREN))
        {
            Expr group = new Expr.Grouping(expression());
            consume(RIGHT_PAREN, "Expected )");
            return group;
        }

        if (match(IDENTIFIER))
        {
            return new Expr.Variable(previous());
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match (TokenType... types) 
    {
        for (TokenType type : types) 
        {
            if (check(type)) 
            {
                advance();
                return true;
            }
        }

        return false;
    }
    

    private Token consume(TokenType type, String message) 
    {
        if(check(type))
            return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) 
    {
        OurPL.error(token.line, message);
        return new ParseError();
    }

    private void synchronize() 
    {
        advance();

        while(!isAtEnd()) 
        {
            if (previous().type == SEMICOLON) 
                return;
            switch(peek().type) 
            {
                case STRUCT:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }
            
            advance();
        }
    }


    private boolean check(TokenType type) 
    {
        if (isAtEnd())
            return false;

        return peek().type == type;
    }



    private Token advance() 
    {
        if(!isAtEnd()) 
            current++;
        return previous();
    }


    private boolean isAtEnd() 
    {
        return peek().type == EOF;
    }


    private Token peek() 
    {
        return tokens.get(current);
    }


    private Token previous() 
    {
        return tokens.get(current - 1);
    }
}
