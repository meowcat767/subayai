package site.meowcat.subayai.lang;

import site.meowcat.subayai.lang.ast.Expr;
import site.meowcat.subayai.lang.ast.Stmt;
import site.meowcat.subayai.lang.runtime.Sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();
    private final Map<String, Stmt.Class> classes = new HashMap<>(); // Standard classes

    public Interpreter() {
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeException error) {
            System.err.println(error.getMessage());
            error.printStackTrace();
        }
    }

    private void execute(Stmt stmt) {
        if (stmt != null)
            stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        classes.put(stmt.name.lexeme, stmt);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitCallStmt(Stmt.CallStmt stmt) {
        String className = stmt.target.lexeme;
        Stmt.Class classStmt = classes.get(className);
        if (classStmt == null) {
            throw new RuntimeException("Undefined class '" + className + "' at " + stmt.target.line);
        }

        executeBlock(classStmt.body, new Environment(environment));
        return null;
    }

    @Override
    public Void visitSwitchStmt(Stmt.Switch stmt) {
        Object value = evaluate(stmt.expression);
        boolean matched = false;

        for (Stmt.Switch.Case kase : stmt.cases) {
            Object caseValue = evaluate(kase.value);
            if (isEqual(value, caseValue)) {
                execute(kase.body);
                matched = true;
                break;
            }
        }

        if (!matched && stmt.defaultCase != null) {
            execute(stmt.defaultCase);
        }

        return null;
    }

    @Override
    public Void visitPkgStmt(Stmt.Pkg stmt) {
        return null;
    }

    @Override
    public Void visitImportStmt(Stmt.Import stmt) {
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                throw new RuntimeException("Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (callee instanceof SubayaiCallable) {
            SubayaiCallable function = (SubayaiCallable) callee;
            if (arguments.size() != function.arity()) {
                throw new RuntimeException("Expected " +
                        function.arity() + " arguments but got " +
                        arguments.size() + ".");
            }
            return function.call(this, arguments);
        }

        throw new RuntimeException("Can only call functions and classes.");
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof SubayaiInstance) {
            return ((SubayaiInstance) object).get(expr.name);
        } else if (object instanceof Sys.SysInstance) {
            return ((Sys.SysInstance) object).get(expr.name);
        }

        throw new RuntimeException("Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        // Simple logical defaults
        Object left = evaluate(expr.left);
        // Logical logic here if we had tokens...
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof SubayaiInstance)) {
            throw new RuntimeException("Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((SubayaiInstance) object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        if (expr.name.lexeme.equals("Sys")) {
            return new Sys.SysInstance();
        }
        return environment.get(expr.name);
    }

    @Override
    public Object visitNewExpr(Expr.New expr) {
        String className = expr.className.lexeme;
        Stmt.Class classStmt = classes.get(className);
        if (classStmt == null) {
            throw new RuntimeException("Undefined class '" + className + "' at " + expr.className.line);
        }

        SubayaiInstance instance = new SubayaiInstance(classStmt);
        Environment instanceEnv = new Environment(environment);
        instance.setEnvironment(instanceEnv);
        executeBlock(classStmt.body, instanceEnv);

        return instance;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeException(operator + "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeException(operator + "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private static class Return extends RuntimeException {
        final Object value;

        Return(Object value) {
            this.value = value;
        }
    }

    public interface SubayaiCallable {
        int arity();

        Object call(Interpreter interpreter, List<Object> arguments);
    }

    public static class SubayaiInstance {
        private Stmt.Class klass;
        private final Map<String, Object> fields = new HashMap<>();
        private Environment env;

        public SubayaiInstance(Stmt.Class klass) {
            this.klass = klass;
        }

        public void setEnvironment(Environment env) {
            this.env = env;
        }

        public Object get(Token name) {
            if (env != null) {
                try {
                    return env.get(name);
                } catch (Exception e) {
                }
            }
            if (fields.containsKey(name.lexeme)) {
                return fields.get(name.lexeme);
            }
            throw new RuntimeException("Undefined property '" + name.lexeme + "'.");
        }

        public void set(Token name, Object value) {
            fields.put(name.lexeme, value);
            if (env != null)
                env.define(name.lexeme, value);
        }

        @Override
        public String toString() {
            return klass.name.lexeme + " instance";
        }
    }
}
