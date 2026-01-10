package site.meowcat.subayai.lang;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import site.meowcat.subayai.lang.ast.Stmt;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 2 && args[0].equals("run")) {
            runFile(args[1]);
        } else {
            System.out.println("Usage: subayai run [path]");
            System.exit(64);
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        // Debug: print tokens
        // for (Token token : tokens) { System.out.println(token); }

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (statements == null)
            return;

        Interpreter interpreter = new Interpreter();
        // Register standard classes or run main?
        // We need to find "main" class and run it?
        // Spec 4.2 "Execution begins when this class (main) is loaded".

        // Use interpreter to "interpret" all statements (definitions).
        // This will register classes.
        interpreter.interpret(statements);

        // But simply defining classes doesn't run code inside them unless we execute
        // them?
        // Ah, `interpreter.visitClassStmt` only registers them.
        // We need a post-pass to find `main` class and run it.

        // Since we are inside Interpreter, we need a way to look up `main`.
        // I should add a method to Interpreter to run main.

        // Actually, let's just trigger `call main;` equivalent manually if we can.
        // Or just iterate classes and find main.

        // For now, let's see if we can expose a "runMain" method.
        // But since `interpret` is void, maybe I should add logic to `interpret`?
        // Or just manually construct a CallStmt for main?

        Token mainToken = new Token(TokenType.IDENTIFIER, "main", null, 0);
        Stmt.CallStmt callMain = new Stmt.CallStmt(mainToken);
        interpreter.interpret(java.util.Collections.singletonList(callMain));
    }
}
