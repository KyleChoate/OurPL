package cpsc326;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DebugTool 
{

    private List<Token> lex(String source) 
    {
        return new Lexer(source).scanTokens();
    }

    private List<Stmt> parse(String source) 
    {
        OurPL.hadError = false;
        return new Parser(lex(source)).parse();
    }

    @Test
    void rawRecursiveTest()
    {
        String source = 
        "fun helloWorld() { print \"Hello World\"; } " +
        "helloWorld();";
        new Interpreter().interpret(parse(source));
    }
}
