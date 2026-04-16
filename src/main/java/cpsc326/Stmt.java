package cpsc326;

import java.util.List;

abstract class Stmt 
{
  interface Visitor<R> 
  {
    R visitStatement(Statement v);

    R visitVariableDeclaration(varDecl v);
  }

  static class varDecl extends Stmt
  {
    final Token name;
    final Expr expr;

    varDecl(Token name, Expr expr) 
    {
      this.name = name;
      this.expr = expr;
    }

    @Override
    <R> R accept(Visitor<R> visitor) 
    {
      return visitor.visitVariableDeclaration(this);
    }
  }


  static class Statement extends Stmt
  {
    final Token name;
    final Expr expr;

    Statement(Token name, Expr expr) 
    {
      this.name = name;
      this.expr = expr;
    }

    @Override
    <R> R accept(Visitor<R> visitor) 
    {
      return visitor.visitStatement(this);
    }
  }


  abstract <R> R accept(Visitor<R> visitor);
}

