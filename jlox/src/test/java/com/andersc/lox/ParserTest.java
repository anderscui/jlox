package com.andersc.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void parseArithmeticExpr() {
        var source = "-1 + 6 / 3";
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens);
        // (+ (- 1.0) (/ 6.0 3.0))
        Expr expr = parser.parse();

        System.out.println(new AstPrinter().print(expr));
        assertInstanceOf(Expr.Binary.class, expr);

        var root = (Expr.Binary)expr;
        assertInstanceOf(Expr.Unary.class, root.left);
        assertInstanceOf(Expr.Binary.class, root.right);
        var op = root.operator;
        System.out.println(op);
        assertEquals(TokenType.PLUS, op.type);
        assertEquals("+", op.lexeme);
    }
}