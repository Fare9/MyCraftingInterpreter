package com.craftinginterpreters.lox.printers;

import com.craftinginterpreters.lox.Expr;

public class RPNPrinter implements Expr.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return generateRPN(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitConditionalExpr(Expr.Conditional expr) {
        return "";
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return generateRPN("", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return generateRPN(expr.operator.lexeme, expr.right);
    }

    private String generateRPN(String op, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        for (Expr expr : exprs) {
            builder.append(expr.accept(this));
            builder.append(" ");
        }

        builder.append(op);

        return builder.toString();
    }
}
