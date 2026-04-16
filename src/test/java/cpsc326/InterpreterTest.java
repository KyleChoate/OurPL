package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class InterpreterTest
{

    private Object scan(String source) {
        OurPL.hadError = false;

        Lexer lexy = new Lexer(source);
        List<Token> tolkein = lexy.scanTokens();
        Parser parsy = new Parser(tolkein);
        Expr exprsy = parsy.parse();
        Interpreter interprety = new Interpreter();
        Object joey = interprety.getInterpret(exprsy);
        return joey;

    }

    @Test
    public void TestOrderOfOperations() 
    {
        Object result;

        result = scan("1 + 2");
        assertEquals("3", result);

        result = scan("1 + 2 * 3 - 4");
        assertEquals("3", result);

        result = scan("1 + 2 + 3 + 4");
        assertEquals("10", result);

        assertFalse(OurPL.hadError);
    }

    @Test
    public void TestGroupings() 
    {
        Object result;

        result = scan("(1 + 2) * 3 + 4");
        assertEquals("13", result);

        result = scan("2 + ((1 + 2) * (3 + 4) + 1 / 1) / 2");
        assertEquals("13", result);

        assertFalse(OurPL.hadError);
    }

    @Test
    public void TestDivideByZero() 
    {
        scan("1 / 0");
        assertTrue(OurPL.hadError);
    }

    @Test
    public void TestAddStringToString() 
    {
        Object result;
        result = scan("\"I like \" + \"cheese\"");
        assertEquals("I like cheese", result);
        assertFalse(OurPL.hadError);
    }



    @Test
    public void TestAddStringToNumber() 
    {
        scan("\"test\" + 1");
        assertTrue(OurPL.hadError);
    }


    @Test
    public void TestBoolean() 
    {
        Object result;
        result = scan("true");
        assertEquals("true", result);
        result = scan("false");
        assertEquals("false", result);

        assertFalse(OurPL.hadError);
    }

    @Test
    public void TestBooleanBang() 
    {
        Object result;
        result = scan("!true");
        assertEquals("false", result);
        result = scan("!false");
        assertEquals("true", result);

        assertFalse(OurPL.hadError);
    }

    @Test
    public void TestBooleanComparison() 
    {
        Object result;

        result = scan("true == false");
        assertEquals("false", result);

        result = scan("true == !false");
        assertEquals("true", result);

        result = scan("(true == !false) != false");
        assertEquals("true", result);

        assertFalse(OurPL.hadError);
    }


    @Test
    public void TestNil() 
    {
        Object result;
        result = scan("nil");
        assertEquals("nil", result);
        // result = scan("!false");
        // assertEquals("true", result);
        
        assertFalse(OurPL.hadError);
    }

}