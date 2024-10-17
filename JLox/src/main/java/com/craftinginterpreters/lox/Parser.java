package com.craftinginterpreters.lox;

import java.util.List;

// statically import all the token types, so we don't need to use
// all the time Token.**
import static com.craftinginterpreters.lox.TokenType.*;

/***
 * The Parser will be implemented as a recursive descent parsing, we have a
 * series of rules that will be implemented to avoid being left recursive (so
 * we will not use recursivity on the left side of the rule, and with this
 * we will create the whole grammar.
 *
 * expression -> equality ;
 * equality -> comparison (("!=" | "==") comparison )* ;
 * comparison -> term ((">"|">="|"<"|"<=") term)* ;
 * term -> factor (("-"|"+") factor)* ;
 * factor -> unary (("/"|"*") unary)* ;
 * unary -> ("!"|"-") unary
 *       | primary ;
 * primary -> NUMBER | STRING | "true" | "false" | "nil"
 *          | "(" expression ")" ;
 *
 * We can augment this grammar with a comma operator like the one from C
 *
 * expression -> comma ;
 * comma -> equality ( "," equality)* ;
 * equality -> comparison (("!=" | "==") comparison )* ;
 * comparison -> term ((">"|">="|"<"|"<=") term)* ;
 * term -> factor (("-"|"+") factor)* ;
 * factor -> unary (("/"|"*") unary)* ;
 * unary -> ("!"|"-") unary
 *       | primary ;
 * primary -> NUMBER | STRING | "true" | "false" | "nil"
 *          | "(" expression ")" ;
 *
 * Now we can even include a ternary operator with a higher precedence to
 * the comma operator:
 *
 * expression -> comma ;
 * comma -> ternary ( "," ternary)* ;
 * ternary -> equality ( "?" equality ":" equality )* ;
 * equality -> comparison (("!=" | "==") comparison )* ;
 * comparison -> term ((">"|">="|"<"|"<=") term)* ;
 * term -> factor (("-"|"+") factor)* ;
 * factor -> unary (("/"|"*") unary)* ;
 * unary -> ("!"|"-") unary
 *       | primary ;
 * primary -> NUMBER | STRING | "true" | "false" | "nil"
 *          | "(" expression ")" ;
 *
 * The precedence of these rules goes from bottom (primary) to top (expression),
 * so bottom has a higher precedence than top.
 * But in a recursive descent parsing, we will parse from top to bottom, or
 * from expression to primary.
 *
 * As we can see, the ternary operator does not allow another
 * ternary operator right in the true_statement of the expression after "?"
 * that's to avoid left recursion.
 *
 * We will apply the next to transform rules into imperative code:
 *
 * +-------------------+-----------------------------------+
 * | Grammar Notation  | Code representation               |
 * | Terminal          | Code to match and consume a token |
 * | Nonterminal       | Call to that rule's function      |
 * | "|"               | if or switch statement            |
 * | "*" or "+"        | while or for loop                 |
 * | "?"               | if statement                      |
 * +-------------------+-----------------------------------+
 */

public class Parser {

    private static class ParseError extends RuntimeException {}

    // list of tokens to parse
    private final List<Token> tokens;
    // pointer to the analyzed token
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        try {
            return expression();
        } catch (ParseError e) {
            return null;
        }
    }

    /**
     * Method to parse the expression, expression just directly
     * calls to the next rule `equality`
     *
     * @return a parsed expression
     */
    private Expr expression() {
        return comma();
    }

    /**
     * Match the next rule:
     *
     * comma -> equality ( "," equality)* ;
     *
     * @return a parsed expression
     */
    private Expr comma() {
        Expr expr = ternary();

        while (match(COMMA)) {
            Token operator = previous();
            Expr right = ternary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Match the next rule:
     *
     * ternary -> equality ( "?" equality ":" equality )* ;
     *
     * @return a parsed expression
     */
    private Expr ternary() {
        Expr expr = equality();

        while (match(QUESTION_MARK)) {
            Expr true_expr = equality();
            consume(COLON, "Expected ':' token in ternary operation.");
            Expr false_expr = equality();
            expr = new Expr.Conditional(expr, true_expr, false_expr);
        }

        return expr;
    }

    /**
     * Match the next rule:
     *
     * equality -> comparison (("!=" | "==") comparison )* ;
     *
     * @return a parsed expression
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) { // while we have != or ==
            Token operator = previous(); // get the operator
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Match the next rule:
     *
     * comparison -> term ((">"|">="|"<"|"<=") term)* ;
     *
     * @return a parsed expression
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Match the next rule:
     *
     * term -> factor (("-"|"+") factor)* ;
     *
     * @return a parsed expression
     */
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Match the next rule:
     *
     * factor -> unary (("/"|"*") unary)* ;
     *
     * @return a parsed expression
     */
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Match the next rule:
     *
     * unary -> ("!"|"-") unary
     *       | primary ;
     *
     * @return a parsed expression
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        // | part of the rule
        return primary();
    }

    /**
     * Match the next rule:
     *
     * primary -> NUMBER | STRING | "true" | "false" | "nil"
     *          | "(" expression ")" ;
     *
     * @return
     */
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression.");
    }

    private void synchronize() {
        // skip the token where the error happened
        advance();

        while (!isAtEnd()) { // while it is not at the end, look for the next statement
            if (previous().type == SEMICOLON) return; // if the previous token is a semicolon, we have found the next statement

            switch (peek().type) { // we look for possible initial tokens for statements
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

    /**
     * Consume will check for a provided type, this type is the expected token
     * if the token is not the expected one we will throw a ParseError that will
     * help us to synchronize if the error happens.
     *
     * @param type expected type of token
     * @param message message in case of error
     * @return current token if the type is correct
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    /**
     * Generate a new Lox error, and throw a parser error
     *
     * @param token token where error happened
     * @param message message to show
     * @return a new error
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Match a list of given types.
     *
     * @param type list of types
     * @return `true` if some type matches the current token, `false` otherwise
     */
    private boolean match(TokenType... type) {
        for (TokenType tokenType : type) {
            // check if type of current token matches
            if (check(tokenType)) {
                // consume the token
                advance();
                // return true
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current token is the one of provided type without consuming tokens
     *
     * @param type type to check
     * @return `true` if current token is the type, `false` otherwise
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * Consumes the current token if it is not at the end of the list.
     *
     * @return consumed token
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * Return the previous token to the current one
     *
     * @return previous token
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * @return current token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * @return `true` if we are at the end of the list of Tokens, `false` otherwise
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }
}
