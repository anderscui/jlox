package com.andersc.lox;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.andersc.lox.TokenType.*;

class ScannerTest {
    @Test
    void scanEmptyTokens() {
        var source = "";
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        assertEquals(1, tokens.size());

        var token = tokens.get(0);
        assertEquals(TokenType.EOF, token.type);
    }

    @Test
    void scanOneLineTokens() {
        var source = "var sum = a + b;";
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        assertEquals(8, tokens.size());

        var types = tokens.stream().map(token -> token.type).toList();
        var expectedTypes = List.of(VAR, IDENTIFIER, EQUAL, IDENTIFIER, PLUS, IDENTIFIER, SEMICOLON, EOF);
        assertArrayEquals(expectedTypes.toArray(), types.toArray());
    }

    @Test
    void scanLiteralTokens() {
        var source = "\"hello \r\n\tworld\"; 1.23; true; false; nil;";
        System.out.println(source);
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        System.out.println(tokens);
        assertEquals(11, tokens.size());

        var types = tokens.stream().map(token -> token.type).toList();
        var expectedTypes = List.of(STRING, SEMICOLON, NUMBER, SEMICOLON, TRUE, SEMICOLON, FALSE, SEMICOLON, NIL, SEMICOLON, EOF);
        assertArrayEquals(expectedTypes.toArray(), types.toArray());
    }
}