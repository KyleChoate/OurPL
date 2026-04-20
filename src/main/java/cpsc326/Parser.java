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

    Expr parse() 
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
        while (!match(EOF))
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
        Token case = advance();
        // Consume printStmt, exprStmt, block, ifStmt, whileStmt, or forStmt
        // Return that
        return null;
    }

    
    private Stmt ifStmt()
    {
        // Consume if
        // Consume expression
        // Consume statement
        // Consume else and another statement (optional)
        return null;
    }

    private Stmt forStmt()
    {
        // Consume for
        // Consume left paren
        // Consume varDecl or exprStmt (optional)
        // Consume  semi-colon
        // Consume expression (optional)
        // Consume semi-colon
        // Consume expression (optional)
        // Consome right_paren
        // Consume statement
        // Consume semi-colon
        return null;
    }

    private Stmt whileStmt()
    {
        // consume while
        // Consume left paren
        // Consume expression
        // Consume right paren
        // consume statement
        // Return result
        return null;
    }

    private Stmt block()
    {
        // Consume left paren
        // Consume declaration
        // Consume right paren
        // Return result
        return null;
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
        // consume print
        // Consume expression
        // Consume semicolon
        // Return result
        return null;
    }

    private Expr expression() 
    {
        return assignment();
    }

    private Expr assignment()
    {
        // Consume identifier
        // Consume equal
        // Consume assignment or logic_or
        return null;
    }


    private Expr logic_or()
    {
        // Consume logic_and
        // Consume (or <logic_and>) optionally infinitely many times
        // Return result
        return null;
    }

    private Expr logic_and()
    {
        // Consume equality
        // Consume (and <equality>) optionally infinitely many times
        // Return result
        return null;
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
