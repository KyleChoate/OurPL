package cpsc326;

import java.util.List;

abstract class Stmt 
{
  interface Visitor<R> 
  {
    R visitBlockStmt(Block stmt);

    R visitExpressionStmt(Expression expression);

    R visitIfStmt(If stmt);

    R visitPrintStmt(Print stmt);

    R visitVarStmt(Var stmt);

    R visitWhileStmt(While stmt);
  }

  static class Block extends Stmt
  {
    final List<Stmt> stmt;

    Block(List<Stmt> stmt) 
    {
      this.stmt = stmt;
    }

    @Override
    <R> R accept(Visitor<R> visitor) 
    {
      return visitor.visitBlockStmt(this);
    }
  }

  static class Expression extends Stmt
  {
    final Expr expression;

    Expression(Expr expression) 
    {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) 
    {
      return visitor.visitExpressionStmt(this);
    }
  }

  static class If extends Stmt
  {
    final Expr expr;

    If(Expr expr) 
    {
      this.expr = expr;
    }

    @Override
    <R> R accept(Visitor<R> visitor) 
    {
      return visitor.visitIfStmt(this);
    }
  }

  static class Print extends Stmt
  {
    final Expr expr;

    Print(Expr expr) 
    {
      this.expr = expr;
    }

    @Override
    <R> R accept(Visitor<R> visitor) 
    {
      return visitor.visitPrintStmt(this);
    }
  }

  static class Var extends Stmt
  {
    final Token name;
    final Expr expr;

    Var(Token name, Expr expr) 
    {
      this.name = name;
      this.expr = expr;
    }

    @Override
    <R> R accept(Visitor<R> visitor) 
    {
      return visitor.visitVarStmt(this);
    }
  }

  static class While extends Stmt
  {
    final Expr condition;
    final Stmt body;

    While(Expr condition, Stmt body) 
    {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) 
    {
      return visitor.visitWhileStmt(this);
    }
  }



  abstract <R> R accept(Visitor<R> visitor);
}

