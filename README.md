# jlox

A Lox implementation in Java.

* [Read the book - Crafting Interpreters](https://craftinginterpreters.com/contents.html)
* [Visit the repository](https://github.com/munificent/craftinginterpreters)

## Features

* tokens and lexing
* abstract syntax trees
* recursive descent parsing
* prefix and infix expressions
* runtime representation of objects
* interpreting code using the Visitor pattern
* lexical scope
* environment chains for storing variables
* control flow
* functions with parameters
* closures
* static variable resolution and error detection
* classes
* constructors
* fields
* methods
* inheritance

## BNF grammar

```bash
program        → declaration* EOF ;

declaration    → classDecl
               | funDecl
               | varDecl
               | statement ;

classDecl      → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;

funDecl        → "fun" function ;
function       → IDENTIFIER "(" parameters? ")" block ;
parameters     → IDENTIFIER ( "," IDENTIFIER )* ;

varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | returnStmt
               | whileStmt
               | block ;

block          → "{" declaration* "}" ;

ifStmt         → "if" "(" expression ")" statement
               ( "else" statement )? ;

whileStmt      → "while" "(" expression ")" statement ;

forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                 expression? ";"
                 expression? ")" statement ;

exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;
returnStmt     → "return" expression? ";" ;

expression     → assignment ;
assignment     → ( call ".")? IDENTIFIER "=" assignment
               | logic_or ;

logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → equality ( "and" equality )* ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary | call ;
call           → primary ( "(" arguments? ") | "." IDENTIFIER )* ;
arguments      → expression ( "," expression )* ;

primary        → "true" | "false" | "nil"
               | NUMBER | STRING | IDENTIFIER
               | "(" expression ")"
               | "super" "." IDENTIFIER ;
```

## Built-in global functions and constants

* E: `e`
* PI: `π`
* input(): read characters from console
* show(object): print
* showLine(object): println
* string(object)
* bool(object)
* number(object)
* randomNumber(upperBound)
* hasProperty(object, name)
