package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Class to generate the java code that it will contain
 * the definitions of the different expressions from the
 * Lox language for JLox.
 */
public class GenerateAst {

    static String spacer = "  ";
    static String version = "0.1";

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java -jar generate-ast.jar <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Conditional : Expr condition, Expr true_statement, Expr false_statement",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(
            String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        try (PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writeComment(writer);

            // header of the file
            writer.println("package com.craftinginterpreters.lox;");
            writer.println();
            writer.println("import java.util.List;");

            // abstract base class
            writer.println("public abstract class " + baseName + " {");

            // Method declaration
            defineVisitor(writer, baseName, types);

            // The base accept() method
            writer.println();
            writer.println(spacer + "// The base abstract accept method");
            writer.println(spacer + "// all the classes will override it");
            writer.println(spacer + "public abstract <R> R accept(Visitor<R> visitor);");
            writer.println();
            writer.println();

            // here it will go all the magic
            for (String type : types) {
                String className = type.split(":")[0].trim();
                String fields = type.split(":")[1].trim();
                defineType(writer, baseName, className, fields);
            }

            // end of the file
            writer.println("}");
        }
    }

    /**
     * Generation of internal types that extends the Base class.
     *
     * @param writer
     * @param baseName
     * @param className
     * @param fieldList
     */
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        // definition of the class
        writer.println(spacer + "public static class " + className + " extends " + baseName + " {");

        // get the fields
        String[] fields = fieldList.split(", ");

        // Fields
        writer.println();
        for (String field : fields) {
            writer.println(spacer + spacer + "final public " + field + ";");
        }

        writer.println();

        // Constructor
        writer.println(spacer + spacer + className + "(" + fieldList + ") {");

        // Store paremeters in fields
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println(spacer + spacer + spacer + "this." + name + " = " + name + ";");
        }

        // constructor bracket
        writer.println(spacer + spacer + "}");

        // Now each subclass will implement its accept method that will call its visitor
        writer.println();
        writer.println(spacer + spacer + "@Override");
        writer.println(spacer + spacer + "public <R> R accept(Visitor<R> visitor) {");
        writer.println(spacer + spacer + spacer + "return visitor.visit" + className + baseName + "(this);");
        writer.println(spacer + spacer + "}");

        // final bracket
        writer.println(spacer + "}");
    }

    /**
     * Define visitor methods for implementing a `visitor` and `accept` methods. The
     * `visitor` will be implemented in the base class, then `accept` will be a definition.
     *
     * @param writer
     * @param baseName
     * @param types
     */
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println();
        writer.println(spacer + "public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(spacer + spacer + "R visit" + typeName + baseName + "(" + typeName + " " +
                    baseName.toLowerCase() + ");");
        }

        writer.println(spacer + "}");
        writer.println();
    }

    private static void writeComment(PrintWriter writer) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());

        writer.println("/*************************************");
        writer.println("* Generated by GenerateAst program");
        writer.println("* Do not modify this code.");
        writer.println("* Version "+version);
        writer.println("* Generated the date: " + timeStamp);
        writer.println("* Author: Fare9");
        writer.println("* Expr.java contains all the expresions");
        writer.println("* from the language, as well as their");
        writer.println("* variables, and the visit method");
        writer.println("*************************************/");
        writer.println();
    }
}
