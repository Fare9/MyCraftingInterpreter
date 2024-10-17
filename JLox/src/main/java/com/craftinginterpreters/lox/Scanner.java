package com.craftinginterpreters.lox;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * Class that will retrieve all the characters from the source
 * code, and it will transform them into a list of tokens.
 */
public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    // map to keep the existing keywords in our scanner
    // we use these to see if the read identifiers are
    // keywords instead of identifiers
    private static final Map<String, TokenType> keywords;

    // fields to keep track of where the scanner is
    private int start = 0;
    private int current = 0;
    private int line = 1;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            // Tokens that only have one character
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case ':': addToken(COLON); break;
            case '?': addToken(QUESTION_MARK); break;
            // !=, ==, <=, >=
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            // now the slash '/' can represent different things
            case '/':
                if (match('/')) { // We are in a comment due to double slash
                    // consume tokens until the end of the line or
                    // the end of the file
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) { // C-Style comments
                    blockComment();
                } else {
                    // if not, the token is just the slash
                    addToken(SLASH);
                }
                break;
            // manage string literals;
            case '"':
                string();
                break;

            // Things to ignore in the code
            case ' ':
            case '\r':
            case '\t':
                // ignore them
                break;
            case '\n':
                // new line!
                line++;
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character: " + c + ".");
                }
                break;
        }
    }

    /**
     * Useful method to read the string token
     * between double-quotes. Lox allows multi-line
     * strings.
     */
    private void string() {
        // read while we do not find another
        // double-quote and we still have file
        // to read
        while (peek() != '"' && !isAtEnd()) {
            // did we jump to another line?
            if (peek() == '\n') line++;
            // advance the pointer
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing '"'.
        advance();

        // Now Trim the surrounding quotes.
        // Create a token with the value
        String value = source.substring(start+1, current-1);
        addToken(STRING, value);
    }

    /**
     * Method to consume a number and produce a token
     * with the real value.
     */
    private void number() {
        // while we are reading digits,
        // advance the pointer
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        // we keep reading alphanumeric values
        // as part of an identifier
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type, text);
    }

    /**
     * Skip all the characters inside a block of comments
     * C-style
     */
    private void blockComment() {
        // consume tokens while we do not have '*/'
        int currLine = line;
        while ((peek() != '*' || // check we have '*'
                peekNext() != '/') && // and then '/'
                !isAtEnd()) { // and also to avoid an infinite loop...
            // did we jump to another line?
            if (peek() == '\n') line++;
            // advance the pointer
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "You didn't finish a fucking comment block starting at line: " + currLine+ ".");
            return;
        }

        // if we are not at the end, we need to consume
        // the tokens, because it means we found */
        advance();
        advance();
    }

    /**
     * @return if the parser ies out of the input file.
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * A function to check the next token without advancing if it doesn't
     * match the expected token (LL(1))
     *
     * @param expected expected token
     * @return the next character matches the expected one
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    /**
     * Retrieve a character from the source code, and advance the pointer.
     * @return character from source
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Retrieve just one value without advancing the pointer,
     * in case we are at the end return a 0. This is known
     * as `lookahead`
     *
     * @return current character or 0 if end of file
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Check if there's a character in the next position
     * if not return a 0, otherwise go to the next position
     * and retrieve the character, do not advance the current
     * pointer.
     *
     * @return next character to current if any, 0 instead
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /**
     * Add a token with a simple type, and no literal value.
     *
     * @param type type of the token
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Add a token given its type and the literal value.
     *
     * @param type type of the token
     * @param literal object with the literal value
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    /**
     * Method to check if a given character is a Digit or not.
     *
     * @param c character to check
     * @return `true` if character is a number, `false` otherwise
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Util Method to detect if a character is an alphabetic character
     * and alphabetic character is one between a-z or A-Z or _.
     *
     * @param c character to check
     * @return if character is alphabetic character
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_');
    }
}
