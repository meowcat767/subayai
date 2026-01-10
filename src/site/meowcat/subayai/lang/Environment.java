package site.meowcat.subayai.lang;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeException("Undefined variable '" + name.lexeme + "' at line " + name.line);
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        // Subayai allows implicit declaration on assignment if not exists?
        // Spec 3.1: "cats = 3;"
        // If it's effectively a declaration, we define it.
        // But what if it's shadowing?
        // If I follow Python/Lua rules: if not local/global, define new local?
        // Or if I follow "no explicit types required", it's like Go `:=`.
        // But Subayai uses `=` for both.
        // Let's assume: if not found, define in CURRENT scope.
        define(name.lexeme, value);
    }
}
