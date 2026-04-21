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
        values.put(name.toString(), value);
    }

    public Object get(Token name)
    {

        if (values.containsKey(name.toString()))
            return values.get(name.toString());

        if (enclosing == null)
        {
            throw new RuntimeError(name,"Variable does not exist");
        }

        return enclosing.get(name);

    }

}
