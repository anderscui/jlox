package com.andersc.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    static class FieldDef {
        final String type;
        final String name;

        FieldDef(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output dir>");
            System.exit(1);
        }
        String outputDir = args[0];

        defineAst(outputDir,
                "Expr",
                Arrays.asList(
                        "Assign : Token name, Expr value",
                        "Binary : Expr left, Token operator, Expr right",
                        "Call : Expr callee, Token paren, List<Expr> arguments",
                        "Grouping : Expr expression",
                        "Literal : Object value",
                        "Logical : Expr left, Token operator, Expr right",
                        "Unary : Token operator, Expr right",
                        "Variable: Token name"
                ));

        defineAst(outputDir,
                "Stmt",
                Arrays.asList(
                        "Block : List<Stmt> statements",
                        "Expression : Expr expression",
                        "Function : Token name, List<Token> params, List<Stmt> body",
                        "If : Expr condition, Stmt thenBranch, Stmt elseBranch",
                        "Print : Expr expression",
                        "Return : Token keyword, Expr value",
                        "Var : Token name, Expr initializer",
                        "While : Expr condition, Stmt body"
                ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.andersc.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            var parts = type.split(":");
            var className = parts[0].trim();
            var fields = parts[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // accept() method
        // writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("    }");
        writer.println();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {

        writer.println("    static class " + className + " extends " + baseName + " {");
        var fieldDefs = parseFields(fields);

        // fields
        for (var field : fieldDefs) {
            writer.printf("        final %s %s;", field.type, field.name);
            writer.println();
        }
        writer.println();

        // ctor
        writer.printf("        %s(%s) {", className, fields);
        writer.println();
        for (var field : fieldDefs) {
            writer.printf("            this.%s = %s;", field.name, field.name);
            writer.println();
        }
        writer.println("        }");

        // accept method.
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        // end of subclass
        writer.println("    }");
        writer.println();
    }

    private static List<FieldDef> parseFields(String fields) {
        List<FieldDef> result = new ArrayList<>();
        var fieldDefs = fields.split(",");
        for (var fieldDef : fieldDefs) {
            var parts = fieldDef.trim().split(" ");
            assert parts.length == 2;

            var fieldType = parts[0].trim();
            var fieldName = parts[1].trim();
            result.add(new FieldDef(fieldType, fieldName));
        }
        return result;
    }
}
