package site.meowcat.subayai.lang.ast;

import site.meowcat.subayai.lang.Token;
import java.util.List;

public abstract class Expr {
    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);

        R visitBinaryExpr(Binary expr);

        R visitCallExpr(Call expr);

        R visitGetExpr(Get expr);

        R visitGroupingExpr(Grouping expr);

        R visitLiteralExpr(Literal expr);

        R visitLogicalExpr(Logical expr);

        R visitSetExpr(Set expr);

        R visitUnaryExpr(Unary expr);

        R visitVariableExpr(Variable expr);

        R visitNewExpr(New expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    public static class Get extends Expr {
        public final Expr object;
        public final Token name;

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Literal extends Expr {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Set extends Expr {
        public final Expr object;
        public final Token name;
        public final Expr value;

        public Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Variable extends Expr {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    public static class New extends Expr {
        public final Token keyword; // 'new' keyword token? Or just the type token? Spec says: p = new Parser;
        // It seems 'new' is not in the keyword list in spec... wait.
        // 3.3 Keywords list doesn't have 'new'.
        // But 5.4 Example says: p = new Parser;
        // Is 'new' a keyword? Or is it implicit?
        // Spec 3.3 Keywords: break, case, call, class... NO 'new'.
        // Does 'call' replace 'new'?
        // 5.2: call Print;
        // 5.4: p = new Parser;
        // Maybe 'new' IS a keyword but missed in the list? Or maybe 'new' is just an
        // identifier that is treated specially or I missed it?
        // Spec 3.3 says "The language currently defines the following 25 keywords". It
        // lists 25.
        // 'new' is NOT in the list.
        // Maybe `New` is a class/type? No.
        // If `new` is used in code but not in keywords, maybe it's valid to use as
        // identifier or it's a documentation error.
        // However, `call` is a keyword.
        // Let's assume `new` is a keyword that was accidentally omitted from 3.3 list
        // but present in 5.4.
        // I'll add `NEW` to TokenType and Lexer to be safe, or treat it as `call`?
        // 5.4: `p = new Parser;` -> assignment.
        // 5.2: `call Print;` -> statement.
        // I'll add NEW to TokenType.

        public final Token className;

        public New(Token className) {
            this.className = className;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNewExpr(this);
        }
    }
}
