package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.printers.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    // Flag that indicates there was an error parsing
    static boolean hadError = false;


    /**
     * Main of JLox just scan the file provided from the command line.
     * JLox allows running Lox in two ways, one is providing one file
     * with JLox code, and the other is directly as a command prompt
     * not providing any parameter
     *
     * @param args array with an optional lox file
     */
    public static void main(String[] args) throws IOException {
        //showPrinters();

        if (args.length > 1) {
            System.out.println("USAGE: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }


    private static void showPrinters() {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Literal(45.67))
        );

        System.out.println("ASTPrinter");
        System.out.println(new AstPrinter().print(expression));

        Expr expression2 = new Expr.Binary(
                new Expr.Binary(
                        new Expr.Literal(1),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Expr.Literal(2)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Binary(
                        new Expr.Literal(4),
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(3)
                )
        );

        System.out.println("ASTPrinter");
        System.out.println(new AstPrinter().print(expression2));
        System.out.println("RPNPrinter");
        System.out.println(new RPNPrinter().print(expression2));
    }

    /**
     * In case the user provided one file, here we will directly run it
     * in the interpreter.
     *
     * @param path path to a lox file
     * @throws IOException
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // In case there was an error parsing, we can exit with a
        // specific error
        if (hadError) System.exit(65);
    }

    /**
     * In case the user didn't provide any file, the user wants to run
     * Lox as a command line, so we will provide a basic prompt, and then
     * we will run each line.
     */
    private static void runPrompt() throws IOException {
        // Java needs streams to read from the keyboard
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // now loop to run lines
        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            // in the loop, we just clean the flag
            // for not to kill the whole session
            hadError = false;
        }
    }

    /**
     * V1 of run method, it only uses the scanner to get the tokens
     * and then it prints those tokens showing its line.
     *
     * @param source raw input with the code
     */
    private static void run_v1(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        int currLine = 1;
        System.out.print(currLine + ": ");
        // For now, print the tokens
        for (Token token : tokens) {
            if (token.line != currLine) {
                currLine = token.line;
                System.out.println();
                System.out.print(currLine + ": ");
            }
            System.out.print(token);
        }
        System.out.println();
    }

    /***
     * V2 of run method, here we retrieve the tokens with a scanner,
     * and then we parse it into an Expression AST, then we print it
     * using our AstPrinter
     *
     * @param source raw input with the code
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expr = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        // we use the AstPrinter to print the expression
        System.out.println(new AstPrinter().print(expr));
    }


    static void error(Token token, String message) {
        if (token.type == TokenType.EOF)
            report(token.line, " at end, message", message);
        else
            report(token.line, " at '"+token.lexeme+"'", message);
    }

    /*
    For reporting errors in the prorgam, the errors will detail where
    the problem in the code happens.
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

}
