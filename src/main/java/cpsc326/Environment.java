package cpsc326;

import java.util.HashMap;
import java.util.Map;

public class Environment 
{
    private Environment enclosing;
    private Map<String, Object> values;

    public Environment()
    {
        this.enclosing = null;
        this.values = new HashMap<>();
    }

    public Environment(Environment enclosing)
    {
        this.enclosing = enclosing;
        this.values = new HashMap<>();
    }

    public void define(String name, Object value)
    {
        values.put(name, value);
    }

    public void assign(Token name, Object value)
    {
        if (values.containsKey(name.lexeme))
        {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null)
        {
            enclosing.assign(name, value);
            return;
        }
        
        throw new RuntimeError(name,"Variable does not exist");
    }

    public Object get(Token name)
    {

        if (values.containsKey(name.lexeme))
            return values.get(name.lexeme);

        if (enclosing == null)
        {
            throw new RuntimeError(name,"Variable does not exist");
        }

        return enclosing.get(name);

    }

}
