package cpsc326;

import java.util.List;

class OurPLFunction implements OurPLCallable
{
    private final Stmt.Function declaration;

    OurPLFunction(Stmt.Function declaration) 
    {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) 
    {
        Environment previous = interpreter.environment;
        try 
        {
            // Creates a new environment with only globals as
            // enclosing, so that the function can use globals
            // without having access to other environments
            interpreter.environment = new Environment(interpreter.globals);

            // System.out.println("Calling function: " + declaration.name.lexeme);
            // interpreter.environment.define(declaration.name.lexeme, new OurPLFunction(declaration));

            // If invalid match-up, throw error
            if (arguments.size() != arity())
                throw new RuntimeError(declaration.params.get(0),"Invalid number of arguments, expected " + arity());

            // Assign parameters in the new environment
            for (int i = 0 ; i < arguments.size() ; i++)
            {
                interpreter.environment.define(declaration.params.get(i).lexeme, arguments.get(i));
            }

            // Execute statements
            // Note: it will throw a Return if an executed statement is a Return Stmt
            interpreter.interpret(declaration.body);

        }
        // If a Return Statement is thrown, then catch and return its value
        catch (Return ret)
        {
            // Catch a Return object and return its value
            return ret.value;
        }
        finally 
        {
            interpreter.environment = previous;
        }
        // If there is no return, return nil
        return new Expr.Literal(null);
    }

    @Override
    public int arity() 
    {
        return declaration.params.size();
    }

    @Override
    public String toString() 
    {
        return declaration.name.lexeme;
    }
}
