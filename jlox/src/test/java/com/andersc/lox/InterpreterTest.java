package com.andersc.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private Expr parseExpr(String source) {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens);
        var stmts = parser.parse();
        return ((Stmt.Expression)stmts.get(0)).expression;
    }

    @Test
    void interpreterArithmeticExpr() {
        Expr expr = parseExpr("-1 + 6 / 3");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        var result = expr.accept(interpreter);
        assertEquals(1.0, result);
    }

    @Test
    void interpreterComparisonExpr() {
        Expr expr = parseExpr("(1 + 2) <= 5");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        var result = expr.accept(interpreter);
        assertEquals(true, result);
    }

    @Test
    void interpreterStringConcatExpr() {
        Expr expr = parseExpr("\"abc\" * 3");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        var result = expr.accept(interpreter);
        assertEquals("abcabcabc", result);
    }

    @Test
    void interpreterStringConcat2Expr() {
        Expr expr = parseExpr("3 * \"abc\"");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        var result = expr.accept(interpreter);
        assertEquals("abcabcabc", result);
    }

    @Test
    void interpreterRuntimeError() {
        Expr expr = parseExpr("2 * (3 / -\"abc\")");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        Exception exception = assertThrows(RuntimeError.class, () -> {
            expr.accept(interpreter);
        });
        assertEquals("Operand must be a number.", exception.getMessage());
    }
}