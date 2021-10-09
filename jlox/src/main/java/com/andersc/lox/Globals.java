package com.andersc.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.andersc.lox.Util.*;

public class Globals {
    static final Map<String, Object> values = new HashMap<>();

    static {
        values.put("PI", 3.141592653589793);
        values.put("E", 2.718281828459045);

        values.put("input", new LoxCallable() {
            @Override
            public String call(Interpreter interpreter, List<Object> arguments) {
                var sc = new java.util.Scanner(System.in);
                return sc.nextLine();
            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() {
                return "<native fn: input>";
            }
        });

        values.put("show", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.print(arguments.get(0));
                return null;
            }

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fn: show>";
            }
        });

        values.put("showLine", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(arguments.get(0));
                return null;
            }

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fn: showLine>";
            }
        });

        values.put("string", new LoxCallable() {
            @Override
            public String call(Interpreter interpreter, List<Object> arguments) {
                return stringify(arguments.get(0));
            }

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fn: string>";
            }
        });

        values.put("bool", new LoxCallable() {
            @Override
            public Boolean call(Interpreter interpreter, List<Object> arguments) {
                return isTruthy(arguments.get(0));
            }

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fn: bool>";
            }
        });

        values.put("number", new LoxCallable() {
            @Override
            public Double call(Interpreter interpreter, List<Object> arguments) {
                var arg = arguments.get(0);
                if (arg == null) return null;
                if (arg instanceof Double) return (Double) arg;
                if (arg instanceof String) {
                    try {
                        return Double.parseDouble((String) arg);
                    } catch (NumberFormatException nfe) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fn: number>";
            }
        });

        values.put("randomNumber", new LoxCallable() {
            @Override
            public Double call(Interpreter interpreter, List<Object> arguments) {
                var rand = new Random();
                var upper = (Double) arguments.get(0);
                assert upper > 0;
                return rand.nextDouble() * upper;
            }

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fn: randomNumber>";
            }
        });

        values.put("hasProperty", new LoxCallable() {
            @Override
            public Boolean call(Interpreter interpreter, List<Object> arguments) {
                var object = arguments.get(0);
                var propertyName = arguments.get(1);
                assert object instanceof LoxInstance;
                assert propertyName instanceof String;

                var nameToken = new Token(TokenType.IDENTIFIER, (String)propertyName, null, -1);

                try {
                    var property = ((LoxInstance)object).get(nameToken);
                    return true;
                } catch (RuntimeError re) {
                    return false;
                }
            }

            @Override
            public int arity() {
                return 2;
            }

            @Override
            public String toString() {
                return "<native fn: hasProperty>";
            }
        });
    }
}
