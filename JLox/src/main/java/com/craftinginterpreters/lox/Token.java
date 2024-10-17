package com.craftinginterpreters.lox;

/**
 * Token class which contains all the information
 * about a token including the type, the lexeme
 * a possible literal value, and the line.
 */
public class Token {
    final public TokenType type;
    final public String lexeme;
    final public Object literal;
    final public int line;

    private String tokenStr = null;

    public Token(TokenType type,
                 String lexeme,
                 Object literal,
                 int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        if (tokenStr == null)
            tokenStr = "[" + type.name() + " " + lexeme + " " + literal + "]";
        return tokenStr;
    }
}
