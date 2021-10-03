package com.andersc.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private Expr parse(String source) {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens);
        return parser.parse();
    }

    @Test
    void interpreterArithmeticExpr() {
        Expr expr = parse("-1 + 6 / 3");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        var result = expr.accept(interpreter);
        assertEquals(1.0, result);
    }

    @Test
    void interpreterComparisonExpr() {
        Expr expr = parse("(1 + 2) <= 5");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        var result = expr.accept(interpreter);
        assertEquals(true, result);
    }

    @Test
    void interpreterStringConcatExpr() {
        Expr expr = parse("\"abc\" * 3");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        var result = expr.accept(interpreter);
        assertEquals("abcabcabc", result);
    }

    @Test
    void interpreterStringConcat2Expr() {
        Expr expr = parse("3 * \"abc\"");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        var result = expr.accept(interpreter);
        assertEquals("abcabcabc", result);
    }

    @Test
    void interpreterRuntimeError() {
        Expr expr = parse("2 * (3 / -\"abc\")");
        System.out.println(new AstPrinter().print(expr));

        var interpreter = new Interpreter();
        Exception exception = assertThrows(RuntimeError.class, () -> {
            expr.accept(interpreter);
        });
        assertEquals("Operand must be a number.", exception.getMessage());
    }
}