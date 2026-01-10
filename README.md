# Subayai Language Implementation in Java

This repository contains a reference implementation of the **Subayai** programming language, written in Java. Subayai is a modern, concise language featuring implicit semicolons, `lock`/`notlock` visibility modifiers, and a Python/Go-inspired syntax.

## Features Implemented

*   **Lexer**: Full tokenization support including keywords, identifiers, literals, and **implicit semicolon insertion** (similar to Go).
*   **Parser**: Recursive descent parser building an Abstract Syntax Tree (AST).
    *   Package & Import declarations.
    *   Class declarations with `lock`/`notlock` visibility.
    *   Control flow: `if`/`else` branches.
    *   Switch statements: `switch` with `case ... ->` and `default`.
    *   Variable declarations and assignments.
    *   Expression parsing (arithmetic, logic, comparisons).
*   **Interpreter**: AST-walking interpreter.
    *   Scope and environment management.
    *   Object instantiation via `new ClassName`.
    *   Method/Class body execution via `call ClassName`.
*   **Runtime**:
    *   `Sys.out`: Built-in standard library function for printing to console.
*   **CLI**: A command-line tool `subayai` to build and run projects.

## Getting Started

### Prerequisites
*   Java Development Kit (JDK) 8 or higher.
*   Bash (for the CLI wrapper script).

### Building the Project

Use the provided `subayai` script to compile the project:

```bash
./subayai build
```

The compiled class files will be placed in the `bin/` directory.

### Running a Script

To execute a Subayai source file (`.yai`):

```bash
./subayai run path/to/script.yai
```

### Cleaning Build Artifacts

```bash
./subayai clean
```

## Language Examples

### Hello World
```subayai
pkg site.meowcat.example

class main {
    Sys.out("Hello, World!")
}
```

### Object Instantiation
```subayai
class Cat {
    Sys.out("Meow!")
}

class main {
    c = new Cat
    Sys.out("Cat created")
}
```

### Control Flow
```subayai
class main {
    x = 10
    if (x > 5) {
        Sys.out("Greater than 5")
    } else {
        Sys.out("Smaller")
    }
}
```

### Switch Statement
```subayai
class main {
    val = "A"
    switch val {
        case "A" -> Sys.out("It is A")
        case "B" -> Sys.out("It is B")
        default  -> Sys.out("Unknown")
    }
}
```

## Project Structure

*   `src/site/meowcat/subayai/lang/`: Core implementation.
    *   `Lexer.java`: Tokenizer.
    *   `Parser.java`: AST Builder.
    *   `Interpreter.java`: Runtime engine.
    *   `Main.java`: CLI entry point.
*   `src/site/meowcat/subayai/lang/ast/`: AST Node classes (`Expr`, `Stmt`).
*   `src/site/meowcat/subayai/lang/runtime/`: Runtime library modules (`Sys`).
*   `subayai`: CLI wrapper script.
