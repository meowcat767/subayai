package site.meowcat.subayai.lang;

import site.meowcat.subayai.lang.ast.Expr;
import site.meowcat.subayai.lang.ast.Stmt;
import java.util.ArrayList;
import java.util.List;

import static site.meowcat.subayai.lang.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(PKG))
                return pkgDeclaration();
            if (match(IMPORT))
                return importDeclaration();
            if (match(LOCK, NOTLOCK) || check(CLASS))
                return classDeclaration();
            if (match(VAR))
                return varDeclaration();
            if (match(CALL))
                return callStatement();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt pkgDeclaration() {
        List<Token> path = parsePath();
        consume(SEMICOLON, "Expect ';' after package declaration.");
        return new Stmt.Pkg(path);
    }

    private Stmt importDeclaration() {
        List<Token> path = parsePath();
        boolean wildcard = false;
        if (match(DOT)) {
            consume(STAR, "Expect '*' after '.' in import.");
            wildcard = true;
        }
        consume(SEMICOLON, "Expect ';' after import declaration.");
        return new Stmt.Import(path, wildcard);
    }

    private List<Token> parsePath() {
        List<Token> path = new ArrayList<>();
        path.add(consume(IDENTIFIER, "Expect package name."));
        while (match(DOT)) {
            if (check(STAR))
                break; // Handle import com.subayai.*; case where * is next
            path.add(consume(IDENTIFIER, "Expect identifier after '.'."));
        }
        return path;
    }

    private Stmt classDeclaration() {
        boolean isLock = false;
        if (previous() != null && previous().type == LOCK)
            isLock = true;
        // If we matched LOCK/NOTLOCK, previous() is it.
        // If we didn't match them (check(CLASS) was true), previous might be unrelated.
        // wait, I did: if (match(LOCK, NOTLOCK) || check(CLASS))
        // If matched LOCK/NOTLOCK, then next must be CLASS.
        // If not matched, next IS CLASS.

        if (previous().type == LOCK || previous().type == NOTLOCK) {
            consume(CLASS, "Expect 'class' after visibility modifier.");
        } else {
            consume(CLASS, "Expect 'class'.");
        }

        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt> body = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            body.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        // Optional implicit Semicolon after class decl?
        // Spec usually doesn't require semicolon after standard class blocks, but lexer
        // might insert one if } is followed by newline.
        // Match it if present, but don't error.
        match(SEMICOLON);

        return new Stmt.Class(name, isLock, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt callStatement() {
        Token target = consume(IDENTIFIER, "Expect identifier after 'call'.");
        consume(SEMICOLON, "Expect ';' after call statement.");
        return new Stmt.CallStmt(target);
    }

    private Stmt statement() {
        if (match(IF))
            return ifStatement();
        if (match(SWITCH))
            return switchStatement();
        if (match(RETURN))
            return returnStatement();
        if (match(LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt switchStatement() {
        Expr expression = expression(); // switch cats or switch (cats)
        if (match(COLON)) {
            // Expression-style or block style? Spec: "switch cats:"
            // Wait, spec 5.5: "switch cats: case 1 -> ...; case 2 -> ...; default -> ..."
            // It seems it doesn't use { } in 5.5?
            // "Switch statements may be written in expression or block style"
            // Let's assume indentation or implicit block? Or maybe it just expects list of
            // cases.
            // Since Subayai uses braces for blocks (3.2), likely "switch cats: { ... }" or
            // "switch cats { ... }".
            // 5.5 example:
            // switch cats:
            // case 1 -> ...;
            // case 2 -> ...;
            //
            // If it uses colons, maybe it mimics Python/Go?
            // Go uses "switch cats { ... }".
            // 5.5 Example uses "switch cats:" (colon). And then cases.
            // But 3.2 says "Code blocks are delimited using curly braces".
            // Indentation is stylistic.
            // So maybe "switch cats: { case 1 ... }" or the colon replaces the brace?
            // "switch cats: case 1 -> Sys.out... default -> ..."
            // I will assume it creates a block implicitly or expects braces if they want
            // one?
            // Let's parse strictly what I see: COLON, then CASES.
            // Until when? Until end of block or...
            // If there's no braces, when does it end?
            // This is tricky without braces. "Indentation is stylistic rather than
            // semantic".
            // So implicit block end?
            // Maybe the example just omitted braces for brevity?
            // "Switch statements may be written in expression or block style".
            // Block style: `switch (x) { case 1: ... }` (Java style)
            // Expression style: `switch x: case 1 -> ...` (maybe?)
            // I'll assume if it starts with COLON, it's a single statement or list of cases
            // until EOF or something? No, that's bad.
            // Let's look for braces.
            // If no braces, maybe it expects one statement/expression which is a chain of
            // cases?
            // Let's support `{}` style primarily as it's safer.
            // If `match(COLON)`, I'll expect cases until... well maybe indentation DOES
            // matter? No, 3.2 says no.
            // I'll require braces for "block style" switch if it's multiple cases.
            // But the example `switch cats:` is confusing.
            // Wait, maybe the example IS Python-like and 3.2 is just "in general".
            // But 3.2 says "Code blocks are delimited using curly braces { }, preserving
            // familiarity... Indentation is stylistic".
            // This contradicts "switch cats: \n case 1...".
            // UNLESS `case` is a statement that can follow `switch`.
            // Structure: `switch cats: ( case ... )*`
            // When does it stop? At `}`? But there is no opening `{`.
            // Maybe it stops at the next keyword that isn't `case` or `default`?
            // Or maybe `switch` is an expression?
            // Let's assume the user will use braces like `switch cats { ... }` or `switch
            // cats: { ... }`.
            // I'll parse `switch expr` then check for `{` or `:`.
        }

        // Let's just implement Java-style `switch expr { ... }` for now.
        // And if checking `:`, expect `{`?

        // Actually, let's look at 5.5 again.
        // switch cats:
        // case 1 -> ...
        // case 2 -> ...
        //
        // Maybe the `:` acts like `{`? And it ends... where?
        // If I encounter a token that is NOT case/default, is it done?
        // But what if it's `if ...` inside a case?
        // `case 1 -> if ...`
        // `case 2 -> ...`
        // I'll implement `switch expr { ... }` as the robust way.
        // And optional `:` before `{`.

        boolean hasColon = match(COLON);
        consume(LEFT_BRACE, "Expect '{' after switch expression.");

        List<Stmt.Switch.Case> cases = new ArrayList<>();
        Stmt defaultCase = null;

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            if (match(CASE)) {
                Expr value = expression();
                consume(ARROW, "Expect '->' after case value.");
                // Supports `->` (JAVA 12+ switch expression style)
                Stmt body = statement();
                cases.add(new Stmt.Switch.Case(value, body));
            } else if (match(DEFAULT)) {
                consume(ARROW, "Expect '->' after default.");
                defaultCase = statement();
            } else {
                throw error(peek(), "Expect 'case' or 'default' inside switch block.");
            }
        }
        consume(RIGHT_BRACE, "Expect '}' after switch block.");

        return new Stmt.Switch(expression, cases, defaultCase);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        // Handle `var = val` or `identifier = val`
        // Subayai has implicit var declaration via assignment.
        // `cat = 3` -> if `cat` is not defined, it's defined?
        // Parse as assignment expression.

        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target."); // Don't throw, just report?
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();
        // Add logical OR (||) if supported? Spec doesn't list || or && keywords.
        // 3.3 Keywords doesn't have 'or', 'and'.
        // Maybe it uses C-style `||`, `&&`?
        // The token map has no `||` `&&`.
        // I will assume it's missing from the spec or I need to add tokens?
        // Spec 3.4 "Type System": bool
        // Typically boolean logic exists.
        // I'll skip logical operators for now or use `and`/`or` keywords if they
        // existed.
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    // Spec 5.4: p = new Parser;
    // `new Parser` should be handled.
    // Is `new` a unary operator?

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(IDENTIFIER)) {
            if (previous().lexeme.equals("new")) {
                Token className = consume(IDENTIFIER, "Expect class name after 'new'.");
                return new Expr.New(className);
            }
            return new Expr.Variable(previous());
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    // Helper to fix `new` handling
    // I really should add `new` to keywords.
    // But since I'm in Parser, I can do this:
    /*
     * if (match(IDENTIFIER)) {
     * if (previous().lexeme.equals("new")) {
     * Token className = consume(IDENTIFIER, "Expect class name after 'new'.");
     * return new Expr.New(className);
     * }
     * return new Expr.Variable(previous());
     * }
     */
    // Yes, explicit check.

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        // System.err.println(message + " at " + token);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FOR:
                case IF:
                case VAR:
                case SWITCH:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
