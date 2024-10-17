package com.craftinginterpreters.lox;

import java.util.Map; /**
 * Enum which contains all the possible tokens from
 * Lox language.
 */
public enum TokenType {
    // Single Character Tokens
    LEFT_PAREN, // (
    RIGHT_PAREN, // )
    LEFT_BRACE, // {
    RIGHT_BRACE, // }
    COMMA, // ,
    DOT, // .
    MINUS, // -
    PLUS, // +
    SEMICOLON, // ;
    SLASH, // /
    STAR, // *
    COLON, // :
    QUESTION_MARK, // ?


    // One or two character tokens
    BANG,
    BANG_EQUAL,
    EQUAL, // =
    EQUAL_EQUAL, // ==
    GREATER, // >
    GREATER_EQUAL, // >=
    LESS, // <
    LESS_EQUAL, // <=

    // Literals
    IDENTIFIER, // the name of a variable or a function...
    STRING, // any string used in the code
    NUMBER, // real value

    // Keywords
    AND,
    CLASS,
    ELSE,
    FALSE,
    FUN,
    FOR,
    IF,
    NIL,
    OR,
    PRINT,
    RETURN,
    SUPER,
    THIS,
    TRUE,
    VAR,
    WHILE,

    EOF // end of file!
}