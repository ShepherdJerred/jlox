package com.shepherdjerred.jlox;

public class SyntaxTreePrinter implements Expression.Visitor<String> {

  public static void main(String[] args) {
    Expression expression = new Expression.Binary(
        new Expression.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expression.Literal(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expression.Grouping(
            new Expression.Literal(45.67)));

    System.out.println(new SyntaxTreePrinter().print(expression));
  }

  String print(Expression expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpression(Expression.Binary expression) {
    return parenthesize(expression.operator.lexeme, expression.left, expression.right);
  }

  @Override
  public String visitGroupingExpression(Expression.Grouping expression) {
    return parenthesize("group", expression.expression);
  }

  @Override
  public String visitLiteralExpression(Expression.Literal expression) {
    if (expression.value == null) {
      return "nil";
    }
    return expression.value.toString();
  }

  @Override
  public String visitUnaryExpression(Expression.Unary expression) {
    return parenthesize(expression.operator.lexeme, expression.right);
  }

  private String parenthesize(String name, Expression... expressions) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expression expr : expressions) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }
}
