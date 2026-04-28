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
        return null;
    }

    @Override
    public int arity() 
    {
        return declaration.params.size();
    }

    @Override
    public String toString() 
    {
        return null;
    }
}
