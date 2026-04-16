package cpsc326;

class Interpreter implements Expr.Visitor<Object>{
    void interpret(Expr expression) 
    {
        try 
        {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } 
        
        catch (RuntimeError error) 
        {
            OurPL.runtimeError(error);
        }
    }

    // Custom function to return an Object
    // This is so I can test variants in my test cases
    public Object getInterpret(Expr expression) {
        Object value = new Object();
        try 
        {
            value = evaluate(expression);
            return stringify(value);
        } 
        
        catch (RuntimeError error) 
        {
            OurPL.runtimeError(error);
        }

        return value; // VS-Code complains that the function must return object if I do not do this
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) 
    {
        return expr.value;
    }

    // If Unary, checks whether it begins with ! or -
    // If Bang, returns opposite of binary value
    // If Minus, makes sure it is number and then negates it
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) 
    {
        Object right = evaluate(expr.right);

        switch(expr.operator.type) 
        {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        return null;
    }

    // Makes sure that when <op> x, x is a number (double)
    private void checkNumberOperand(Token operator, Object operand) 
    {
        if (!(operand instanceof Double))
            throw new RuntimeError(operator,"Operand must be a number.");

        return;
    }

    // Makes sure that when x <op> y, x and y are numbers (doubles)
    private void checkNumberOperands(Token operator, Object left, Object right) 
    {
        if (!(left instanceof Double) && !(right instanceof Double))
            throw new RuntimeError(operator,"Operands must be a number.");

        if (!(left instanceof Double))
            throw new RuntimeError(operator,"Operands must be a number.");

        if (!(right instanceof Double))
            throw new RuntimeError(operator,"Operands must be a number.");

        return;

    }

    // If null (in other words, the literal is null) returns false
    // If boolean, returns its value
    // Anything else (numbers, string) is true
    // This is a very weirdly named function by the way
    private boolean isTruthy(Object object) 
    {

        if (object == null)
            return false;

        if (object instanceof Boolean)
            return (boolean)object;

        return true;
    }

    // If the object equals the other object, true
    // If left and right are null, that's okay since null can equal null
    // But we will have a problem if null is compared to not null
    private boolean isEqual(Object left, Object right) 
    {
        if (left == null)
            return (right == null);

        // Can learn aboutr Object.equals() here: https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html
        return left.equals(right);  
    }

    private String stringify(Object object) 
    {
        if (object == null) 
            {
            return "nil";
        }

        if (object instanceof Double) 
        {
            String text = object.toString();
            if(text.endsWith(".0")) 
                
                text = text.substring(0, text.length() - 2);
            return text;
        }

        return object.toString();
    }

    // Do like nothing since we're just checking the expression inside.
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) 
    {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) 
    {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) 
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type)
        {
            // Equality
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case BANG_EQUAL:
                return !isEqual(left, right);

            // Comparison
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;

            // Term
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;
                if (left instanceof String && right instanceof String)
                    return (String)left + (String)right;
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            // Factor
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double)right == 0)
                    throw new RuntimeError(expr.operator, "You cannot divide by zero!");
                return (double)left / (double)right;
        }

        return null;
    }
}
