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
        String source = "fun mod(a, b){if (a < 0) return false;if (a == 0) return true;return (mod(a-b, b));}fun threeOrFivesBelow(n){if (n <= 0) return 0;if (mod(n-1, 3)) return n-1 + threeOrFivesBelow(n-1);if (mod(n-1, 5)) return n-1 + threeOrFivesBelow(n-1);return threeOrFivesBelow(n-1);}print threeOrFivesBelow(10);";
        new Interpreter().interpret(parse(source));
    }
}
