package site.meowcat.subayai.lang.runtime;

import site.meowcat.subayai.lang.Interpreter;
import site.meowcat.subayai.lang.Token;
import java.util.List;

public class Sys {
    public static class SysInstance {
        public Object get(Token name) {
            if (name.lexeme.equals("out")) {
                return new SysOut();
            }
            throw new RuntimeException("Undefined property on Sys: " + name.lexeme);
        }

        @Override
        public String toString() {
            return "Sys";
        }
    }

    public static class SysOut implements Interpreter.SubayaiCallable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            System.out.println(arguments.get(0));
            return null;
        }

        @Override
        public String toString() {
            return "<native fn Sys.out>";
        }
    }
}
