package com.shepherdjerred.jlox;

import static com.shepherdjerred.jlox.TokenType.*;

import java.util.List;

public class Parser {

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  Expression parse() {
    try {
      return expression();
    } catch (ParseException error) {
      return null;
    }
  }

  private Expression expression() {
    return equality();
  }

  private Expression equality() {
    Expression expr = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expression right = comparison();
      expr = new Expression.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expression comparison() {
    Expression expr = addition();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expression right = addition();
      expr = new Expression.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expression addition() {
    Expression expr = multiplication();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expression right = multiplication();
      expr = new Expression.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expression multiplication() {
    Expression expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expression right = unary();
      expr = new Expression.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expression unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expression right = unary();
      return new Expression.Unary(operator, right);
    }

    return primary();
  }

  private Expression primary() {
    if (match(FALSE)) {
      return new Expression.Literal(false);
    }
    if (match(TRUE)) {
      return new Expression.Literal(true);
    }
    if (match(NIL)) {
      return new Expression.Literal(null);
    }

    if (match(NUMBER, STRING)) {
      return new Expression.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {
      Expression expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expression.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) {
      return advance();
    }

    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) {
      return false;
    }
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) {
      current++;
    }
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseException error(Token token, String message) {
    Main.error(token, message);
    return new ParseException();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) {
        return;
      }

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
}
