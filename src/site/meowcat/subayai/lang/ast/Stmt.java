package site.meowcat.subayai.lang.ast;

import site.meowcat.subayai.lang.Token;
import java.util.List;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitBlockStmt(Block stmt);

        R visitClassStmt(Class stmt);

        R visitExpressionStmt(Expression stmt);

        R visitIfStmt(If stmt);

        R visitPrintStmt(Print stmt); // Wait, spec uses Sys.out, which is a call. But for debugging or simple
                                      // implementation...
        // Sys.out is likely a native function call. So ExpressionStmt.

        R visitVarStmt(Var stmt); // Explicit var? Spec: "Variables may be declared without explicit type
                                  // annotations... cats = 3;"
        // Also "var" keyword exists. 3.3 Keywords: "var".
        // Maybe "var x = 1;" vs "x = 1;".
        // Spec 3.1: "cats = 3;" (no var keyword).
        // But "var" is a keyword.
        // I will support Var declaration.

        R visitCallStmt(CallStmt stmt); // "call Print;"

        R visitSwitchStmt(Switch stmt);

        R visitReturnStmt(Return stmt);

        R visitPkgStmt(Pkg stmt);

        R visitImportStmt(Import stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class Class extends Stmt {
        public final Token name;
        public final boolean isLock; // lock vs notlock (default notlock?)
        // Spec 5.2: lock class Print ...
        // Spec 5.1: class main ... (implicit notlock or default)
        public final List<Stmt> body; // Class body containing statements/methods?
        // As discussed, classes seem to contain statements.

        public Class(Token name, boolean isLock, List<Stmt> body) {
            this.name = name;
            this.isLock = isLock;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }
    }

    public static class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    // For debugging, or maybe Sys.out maps to this?
    // I will stick to function calls for Sys.out, so no PrintStmt special node
    // unless needed.
    public static class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    public static class Return extends Stmt {
        public final Token keyword;
        public final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Var extends Stmt {
        public final Token name;
        public final Expr initializer;

        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    public static class CallStmt extends Stmt {
        public final Token target;

        public CallStmt(Token target) {
            this.target = target;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallStmt(this);
        }
    }

    public static class Switch extends Stmt {
        public final Expr expression;
        public final List<Case> cases;
        public final Stmt defaultCase;

        public Switch(Expr expression, List<Case> cases, Stmt defaultCase) {
            this.expression = expression;
            this.cases = cases;
            this.defaultCase = defaultCase;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchStmt(this);
        }

        public static class Case {
            public final Expr value;
            public final Stmt body;

            public Case(Expr value, Stmt body) {
                this.value = value;
                this.body = body;
            }
        }
    }

    public static class Pkg extends Stmt {
        public final Token name; // Wait, pkg site.meowcat.subayai.lang; -> this is complex path?
        // Or just a string of tokens?
        // Simplify to keeping it as a String or list of tokens.
        public final List<Token> path;

        public Pkg(List<Token> path) {
            this.path = path;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPkgStmt(this);
        }
    }

    public static class Import extends Stmt {
        public final List<Token> path;
        public final boolean isWildcard;

        public Import(List<Token> path, boolean isWildcard) {
            this.path = path;
            this.isWildcard = isWildcard;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitImportStmt(this);
        }
    }
}
