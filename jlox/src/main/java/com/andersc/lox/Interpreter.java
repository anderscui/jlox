package com.andersc.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    public void interpret(List<Stmt> statements) {
        try {
            for (var stmt : statements) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object leftValue = evaluate(expr.left);
        Object rightValue = evaluate(expr.right);
        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, leftValue, rightValue);
                return (double)leftValue > (double)rightValue;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, leftValue, rightValue);
                return (double)leftValue >= (double)rightValue;
            case LESS:
                checkNumberOperands(expr.operator, leftValue, rightValue);
                return (double)leftValue < (double)rightValue;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, leftValue, rightValue);
                return (double)leftValue <= (double)rightValue;
            case MINUS:
                checkNumberOperands(expr.operator, leftValue, rightValue);
                return (double)leftValue - (double)rightValue;
            case SLASH:
                checkNumberOperands(expr.operator, leftValue, rightValue);
                return (double)leftValue / (double)rightValue;
            case STAR:
                if (leftValue instanceof String && rightValue instanceof Double) {
                    return repeatString((String)leftValue, ((Double) rightValue).intValue());
                }
                if (leftValue instanceof Double && rightValue instanceof String) {
                    return repeatString((String)rightValue, ((Double) leftValue).intValue());
                }
                checkNumberOperands(expr.operator, leftValue, rightValue);
                return (double)leftValue * (double)rightValue;
            case PLUS:
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return (double)leftValue + (double)rightValue;
                }
                if (leftValue instanceof String && rightValue instanceof String) {
                    return (String)leftValue + (String)rightValue;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object rightValue = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, rightValue);
                return -(double)rightValue;
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String repeatString(String s, int n) {
        if (s == null) return null;
        if (n <= 0) return "";

        StringBuilder builder = new StringBuilder();
        while (n > 0) {
            builder.append(s);
            n--;
        }
        return builder.toString();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (var statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }
}
