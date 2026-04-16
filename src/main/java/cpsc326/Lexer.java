package cpsc326;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cpsc326.TokenType.*;

class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    Lexer(String source) 
    {
        this.source = source;
    }

    static 
    {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("struct", STRUCT);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("true", TRUE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("this", THIS);
        keywords.put("while", WHILE);
        keywords.put("var", VAR);
        
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) 
        {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

     private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isAlphaNumeric(char c) {
        return c == '_' || isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        addToken(type,null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void string() 
    {
        String literal = "";

        while (peek() != '\"')
        {
            char tmp = advance();
            if (tmp == '\n') line++;
            literal += tmp;
            
            if (isAtEnd())
            {
                OurPL.error(line, "Unterminated string.");
                return;
            }
        }

        advance();
        addToken(STRING, literal);

    }

    private void number() 
    {
        current--; // Resets reading number so it can re-read what was read as a digit in scanToken()
        String literal = "";
        int dot_num = 0;
        
        while (Character.isDigit(peek()) || (peek() == '.' && dot_num == 0))
        {
            char tmp = advance();
            literal += tmp;
            if (tmp == '.')
                dot_num++;
        }

        // Learned about split from: https://www.w3schools.com/java/ref_string_split.asp
        String[] parts = literal.split("\\.");

        // If ends in 'num.num' (ex: 12.34)
        if (parts.length == 2)
        {
            tokens.add(new Token(NUMBER, literal, Double.parseDouble(literal), line));
        }

        else if (parts.length == 1)
        {
            tokens.add(new Token(NUMBER, parts[0], Double.parseDouble(parts[0]), line));
            // Add dot token if dot is present at end
            if (literal.charAt(literal.length() - 1) == '.')
                tokens.add(new Token(DOT, ".", null, line));
        }
                
    }

    private void identifier() 
    {
        current--; // Resets current so it can re-read keyword starting at front since scanToken() already advanced first char
        String literal = "";

        while (isAlphaNumeric(peek()))
        {
            char tmp = advance();
            literal += tmp;

        }

        if (literal.length() == 0)
        {
            OurPL.error(line, "Unexpected character.");
            advance();
            return;
        }


        if (keywords.containsKey(literal))
            addToken(keywords.get(literal));

        else
        {
            if (literal.charAt(0) == '_')
            {
                OurPL.error(line, "Unexpected character.");
                literal = literal.substring(1);
                start++;
                // tokens.add(new Token(IDENTIFIER, literal, null, line));
            }
            addToken(IDENTIFIER);
        }
            
    }

    private void scanToken() 
    {
        char tmp = advance();

        if (Character.isDigit(tmp))
        {
            number();
            return;
        }

        // Single-character cases
        switch (tmp)
        {

            default: identifier();                  break;
            case '\"': string();                    break;

            case '0':
            case '1': 
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':   number();                   break;

            case '(': addToken(LEFT_PAREN);         break;
            case ')': addToken(RIGHT_PAREN);        break;
            case '{': addToken(LEFT_BRACE);         break;
            case '}': addToken(RIGHT_BRACE);        break;
            case ',': addToken(COMMA);              break;
            case '.': addToken(DOT);                break;
            case '+': addToken(PLUS);               break;
            case '-': addToken(MINUS);              break;
            case '*': addToken(STAR);               break;
            case '/': addToken(SLASH);              break;
            case ';': addToken(SEMICOLON);          break;


            // Odd cases (ex: comments, carriage returns)
            case '#': 
                while (current < source.length() && peek() != '\n')
                    tmp = advance();                break;
            
            case ' ':                               break;
            case '\t':                              break;
            case '\r':                              break;
            case '\n': line++;                      break;

        
            // Multi-character cases (ex: !=)
            case '!':
                if (match('='))
                    addToken(BANG_EQUAL);
                else
                    addToken(BANG);
                break;

            case '=':
                if (match('='))
                    addToken(EQUAL_EQUAL);
                else
                    addToken(EQUAL);
                break;

            case '<':
                if (match('='))
                    addToken(LESS_EQUAL);
                else
                    addToken(LESS);
                break;

            case '>':
                if (match('='))
                    addToken(GREATER_EQUAL);
                else
                    addToken(GREATER);
                break;

        }
    }
}
