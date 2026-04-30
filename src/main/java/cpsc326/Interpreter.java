package cpsc326;

import java.util.List;

import java.util.ArrayList;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>
{
    public Environment globals = new Environment();
    public Environment environment = new Environment(globals);

    Interpreter() 
    {
        // Adds to globals hashmap a clock function
        // This clock function overrides arity with 0
        globals.define("clock", new OurPLCallable() 
        {
            @Override
            public int arity() {return 0;};

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {return (System.currentTimeMillis()/1000);};
        });
    }

    void interpret(List<Stmt> statements) 
    {
        try 
        {
            for (Stmt statement : statements) 
                execute(statement);
        } 
        catch (RuntimeError error) 
        {
            OurPL.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) 
    {
        if (stmt == null)
            throw new RuntimeException ("Executed invalid statement");

        stmt.accept(this);
    }

    private Object evaluate(Expr expr) 
    {
        return expr.accept(this);
    }




/*#############################################################################
  _   _      _                    _____                 _   _                 
 | | | | ___| |_ __   ___ _ __   |  ___|   _ _ __   ___| |_(_) ___  _ __  ___ 
 | |_| |/ _ \ | '_ \ / _ \ '__|  | |_ | | | | '_ \ / __| __| |/ _ \| '_ \/ __|
 |  _  |  __/ | |_) |  __/ |     |  _|| |_| | | | | (__| |_| | (_) | | | \__ \
 |_| |_|\___|_| .__/ \___|_|     |_|   \__,_|_| |_|\___|\__|_|\___/|_| |_|___/
              |_|                                                             
###############################################################################*/
 
    private void checkNumberOperand(Token operator, Object operand) 
    {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator,"Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) 
    {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator,"Operandd must be numbers.");
    }

    private boolean isTruthy(Object object) 
    {
        if (object == null) 
            return false;
        if (object instanceof Boolean) 
            return (boolean)object;

        return true;
    }

    private boolean isEqual(Object left, Object right) 
    {
        if (left == null && right == null) return true;
        if (left == null) return false;

        return left.equals(right);
    }

    private String stringify(Object object) 
    {
        if (object == null) 
            return "nil";

        if (object instanceof Double) 
        {
            String text = object.toString();
            if(text.endsWith(".0")) 
                text = text.substring(0, text.length() - 2);

            return text;
        }

        return object.toString();
    }



    
/*#########################################################################
 __     ___     _ _      _____                              _             
 \ \   / (_)___(_) |_   | ____|_  ___ __  _ __ ___  ___ ___(_) ___  _ __  
  \ \ / /| / __| | __|  |  _| \ \/ / '_ \| '__/ _ \/ __/ __| |/ _ \| '_ \ 
   \ V / | \__ \ | |_   | |___ >  <| |_) | | |  __/\__ \__ \ | (_) | | | |
    \_/  |_|___/_|\__|  |_____/_/\_\ .__/|_|  \___||___/___/_|\___/|_| |_|
                                   |_|                                    
###########################################################################*/

    // Assign
    @Override
    public Object visitAssignExpr(Expr.Assign expr) 
    {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    // Variable
    @Override
    public Object visitVariableExpr(Expr.Variable expr) 
    {
        return environment.get(expr.name);
    }

    // Logical
    @Override 
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) 
        {
            if (isTruthy(left)) return left;
        }

        else 
        {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    // Literal
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) 
    {
        return expr.value;
    }

    // Unary
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) 
    {
        Object right = evaluate(expr.right);

        switch(expr.operator.type) 
        {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
            default:
        }

        return null;
    }

    // Grouping
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) 
    {
        return evaluate(expr.expression);
    }

    // Grouping
    @Override
    public Object visitCallExpr(Expr.Call expr) 
    {

        // if 

        // evaluate(expr.callee) will return callee's corresponding
        // value in hashmaps since calleee will be a Expr.Variable
        Object callee = evaluate(expr.callee);

        // Error if not callable
        if (!(callee instanceof OurPLCallable)) 
            throw new RuntimeError(expr.paren, "Expecting a function");

        // Cast to a callable type
        OurPLCallable function = (OurPLCallable) callee;

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) 
        {
            arguments.add(evaluate(argument));
        }

        if (arguments.size() != function.arity()) 
            throw new RuntimeError(expr.paren, "Invalid number of parameters");

        return function.call(this, arguments);
    }

    // Binary
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type){
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
            case BANG_EQUAL:
                return !isEqual(left,right);
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            default:
        }

        return null;
    }




/*########################################################################
 __     ___     _ _      ____  _        _                            _   
 \ \   / (_)___(_) |_   / ___|| |_ __ _| |_ ___ _ __ ___   ___ _ __ | |_ 
  \ \ / /| / __| | __|  \___ \| __/ _` | __/ _ \ '_ ` _ \ / _ \ '_ \| __|
   \ V / | \__ \ | |_    ___) | || (_| | ||  __/ | | | | |  __/ | | | |_ 
    \_/  |_|___/_|\__|  |____/ \__\__,_|\__\___|_| |_| |_|\___|_| |_|\__|

##########################################################################*/

    // Block
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) 
    {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }
    
    // Block Helper
    void executeBlock(List<Stmt> statements, Environment environment) 
    {
        Environment previous = this.environment;
        try 
        {
            this.environment = environment;

            for (Stmt statement : statements) 
                execute(statement);
        } 
        finally 
        {
            this.environment = previous;
        }
    }

    // Var
    @Override
    public Void visitVarStmt(Stmt.Var stmt) 
    {
        Object value = null;
        if (stmt.initializer != null) 
            value = evaluate(stmt.initializer);

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    // Expression
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) 
    {
        evaluate(stmt.expression);
        return null;
    }

    // Print
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) 
    {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    // If
    @Override
    public Void visitIfStmt(Stmt.If stmt) 
    {
        if (isTruthy(evaluate(stmt.condition))) 
            execute(stmt.thenBranch);
        
        else if (stmt.elseBranch != null) 
            execute(stmt.elseBranch);
        
        return null;
    }

    // Function
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) 
    {
        // Goal:    Add function to environment's hashmap
        globals.define(stmt.name.lexeme, new OurPLFunction(stmt));
        return null;
    }

    // Return
    @Override
    public Void visitReturnStmt(Stmt.Return stmt) 
    {
        throw new Return(evaluate(stmt.expression));
    }

    // While
    @Override
    public Void visitWhileStmt(Stmt.While stmt) 
    {
        while (isTruthy(evaluate(stmt.condition))) 
        {
            execute(stmt.body);
        }
        return null;
    }

}
