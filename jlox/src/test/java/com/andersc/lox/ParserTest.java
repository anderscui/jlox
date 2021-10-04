package com.andersc.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void parseArithmeticExpr() {
        var source = "-1 + 6 / 3;";
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens);
        // (+ (- 1.0) (/ 6.0 3.0))
        var stmts = parser.parse();
        assertEquals(1, stmts.size());

        var expr = ((Stmt.Expression)stmts.get(0)).expression;
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

    @Test
    void parseAssignExpr() {
        var source = "var a = 1 + 2;";
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens);
        var stmts = parser.parse();
        assertEquals(1, stmts.size());

        var stmt = ((Stmt.Var)stmts.get(0));

        var name = stmt.name;
        var initializer = stmt.initializer;
        System.out.println(name);
        System.out.println(initializer);
        assertEquals(TokenType.IDENTIFIER, name.type);
        assertEquals(TokenType.PLUS, ((Expr.Binary)initializer).operator.type);
    }
}