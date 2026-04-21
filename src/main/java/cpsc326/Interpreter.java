package cpsc326;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>
{

    private Environment environment;

    void interpret(List<Stmt> statements) 
    {
        environment = new Environment();
        try 
        {
            for (int i = 0; i < statements.size() ; i++)
            {
                Object value = evaluate(statements.get(i));
                System.out.println(stringify(value));
            }
            
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


    // Makes sure that when x <op> y, x and y are numbers (doubles)
    private void checkBooleanOperands(Token operator, Object left, Object right) 
    {
        if (!(left instanceof Boolean) && !(right instanceof Boolean))
            throw new RuntimeError(operator,"Operands must be a boolean.");

        if (!(left instanceof Boolean))
            throw new RuntimeError(operator,"Operands must be a boolean.");

        if (!(right instanceof Boolean))
            throw new RuntimeError(operator,"Operands must be a boolean.");

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

    private Object evaluate(Stmt stmt) 
    {
        return stmt.accept(this);
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








    
    @Override
    public Object visitAssignExpr(Expr.Assign expr) 
    {
        return expr.value;
    }




    // 
    // 
    //
    // WORK ON THESE AND BELOW
    // 
    // Expr Changes
    // 
    //
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) 
    {
        Object left = evaluate(expr.left);
        Object right;
        // If left is true, then right will be skipped for evaluation
        if ((boolean)left)
            right = true;
        else
            right = evaluate(expr.right);
        
        Token operator = expr.operator;

        switch(operator.type)
        {
            // Equality
            case AND:
                checkBooleanOperands(expr.operator, left, right);
                return (boolean)left && (boolean)right;
            case OR:
                checkBooleanOperands(expr.operator, left, right);
                return (boolean)left && (boolean)right;
            default:
                break;
        }
        return null;
    }


    @Override
    public Object visitVariableExpr(Expr.Variable expr) 
    {
        Token name = expr.name;
        return environment.get(name);
    }



    //
    //
    //
    // Stmt changes
    //
    //
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) 
    {
        // Enter new environment
        environment = new Environment(environment);
        // Evaluate all statements in block
        List<Stmt> statements = stmt.statements;
        for (int i = 0; i < statements.size() ; i++)
        {
            Object value = evaluate(statements.get(i));
            System.out.println(stringify(value));
        }
        return null;
    }


    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) 
    {
        Expr expression = stmt.expression;
        evaluate(expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) 
    {
        Expr condition = stmt.condition;
        Stmt thenBranch = stmt.thenBranch;
        Stmt elseBranch = stmt.elseBranch;


        // Evaluates condition and runs corresponding branch
        // If it is a boolean, it will parse as boolean
        // If a normal object, returns true
        // If null, will return false
        if (isTruthy(evaluate(condition)))
            evaluate(thenBranch);
        else
            evaluate(elseBranch);

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) 
    {
        Expr expression = stmt.expression;
        Object value = evaluate(expression);
        System.out.println(stringify(value));
        return null;
    }


    @Override
    public Void visitVarStmt(Stmt.Var stmt) 
    {
        Token name = stmt.name;
        Expr initializer = stmt.initializer;
        environment.assign(name, evaluate(initializer));
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) 
    {
        Expr condition = stmt.condition;
        Stmt body = stmt.body;
        while (isTruthy(evaluate(condition)))
        {
            evaluate(body);
        }
        return null;
    }

}
