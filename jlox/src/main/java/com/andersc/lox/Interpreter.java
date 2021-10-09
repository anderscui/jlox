package com.andersc.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.andersc.lox.Util.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        for (var entry : Globals.values.entrySet()) {
            globals.define(entry.getKey(), entry.getValue());
        }
    }

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

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
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
                // if (leftValue instanceof String && rightValue instanceof String) {
                //     return (String)leftValue + (String)rightValue;
                // }
                if (leftValue instanceof String || rightValue instanceof String) {
                    return stringify(leftValue) + stringify(rightValue);
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> args = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            args.add(evaluate(arg));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }
        // TODO: pattern variable.
        LoxCallable func = (LoxCallable) callee;
        if (args.size() != func.arity()) {
            throw new RuntimeError(expr.paren, "Expect " + func.arity() + " arguments but got " + args.size() + ".");
        }
        return func.call(this, args);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance)object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "Only instance have properties.");
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
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);
        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookupVariable(expr.keyword, expr);
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
        return lookupVariable(expr.name, expr);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
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

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme, null);

        Map<String, LoxFunction> methods = new HashMap<>();
        for (var method : stmt.methods) {
            LoxFunction function = new LoxFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }
        var klass = new LoxClass(stmt.name.lexeme, methods);
        environment.assign(stmt.name, klass);

        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
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
        // System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction func = new LoxFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, func);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }
        throw new Return(value);
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

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private Object lookupVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }
}
