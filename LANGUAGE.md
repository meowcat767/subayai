# Subayai Language Specification

Subayai is a concise, interpreted programming language running on the Java Virtual Machine. Originally designed for "Fast" (subayai) scripting, it blends syntax from Go, Kotlin, and Python with direct Java interoperability.

## 1. Syntax Overview

### 1.1 Comments
Single-line comments start with `//`.

```subayai
// This is a comment
```

### 1.2 Statements and Semicolons
Subayai uses **implicit semicolons**. Line breaks automatically terminate statements, similar to Go or Kotlin. You do not need to type `;` at the end of definitions or statements.

### 1.3 Blocks
Blocks are delimited by curly braces `{` and `}`.

```subayai
if (true) {
    Sys.out("Inside block")
}
```

## 2. Variables

### 2.1 Declaration
Variables are logically typed but dynamically resolved at runtime in the current interpreter. They can be declared implicitly via assignment.

```subayai
x = 10         // Number
message = "Hi" // String
```

Explicit `var` declaration is also supported in the grammar usage but implicit assignment is idiomatic.

### 2.2 Types
Supported primitive types:
*   `Number` (Double precision float)
*   `String`
*   `Boolean` (`true`, `false`)
*   `Nil` (`nil`)
*   References (Objects, Arrays from Java)

## 3. Control Flow

### 3.1 If/Else
Standard C-style `if`/`else` control flow. Parentheses around conditions are currently required by the parser logic.

```subayai
if (score > 10) {
    Sys.out("Win")
} else {
    Sys.out("Lose")
}
```

### 3.2 Switch
The `switch` statement uses `->` arrow syntax for cases, removing the need for `break`.

```subayai
switch status {
    case 200 -> Sys.out("OK")
    case 404 -> Sys.out("Not Found")
    default  -> Sys.out("Error")
}
```

## 4. Classes and Objects

### 4.1 Class Definition
Classes can be defined with an optional visibility modifier `lock` or `notlock`.

```subayai
notlock class User {
    name = "Guest"
    Sys.out("User class loaded")
}
```

*   `lock`: Intended for package-private/protected visibility (enforcement implementation pending).
*   `notlock` (or omitted): Public visibility.

### 4.2 Instantiation
Use the `new` keyword followed by the class name (and arguments if supported, currently default constructor).

```subayai
u = new User
```

When a class is instantiated (in the interpreted mode), its body is executed, effectively acting as a constructor.

### 4.3 Methods
Subayai treats class bodies as valid executable blocks. To define "methods", one typically relies on the class body execution or defines callables. Currently, the most robust way to define reusable logic is through Java classes or strictly defined class-as-procedures.

(Note: Future versions will formalize 'def' or 'func' within classes).

## 5. Built-in Runtime

### 5.1 Sys
The `Sys` module provides system-level interaction.

*   `Sys.out(arg)`: Prints stringified argument to stdout.

## 6. Java Interoperability

Subayai has first-class support for the Java ecosystem.

### 6.1 Importing Classes
Use the standard `import` keyword to load Java classes.

```subayai
import java.util.ArrayList
import java.util.Random
```

### 6.2 Using Java Objects
You can instantiate imported Java classes and call their methods using Reflection.

```subayai
list = new ArrayList
list.add("Item 1")
list.add("Item 2")

Sys.out(list.size()) // Prints 2
```

### 6.3 Method Calls
Method resolution handles basic primitive conversions (e.g. Subayai `Double` -> Java `int`). Overloaded methods are resolved based on argument count and type compatibility.

## 7. Project Structure

A typical Subayai project looks like this:

```
project/
  src/
    main.yai
    utils/
      helper.yai
  subayai      (CLI Wrapper)
```

## 8. Compiler/Interpreter Usage

Use the provided CLI tool:

*   **Build**: `./subayai build`
*   **Run**: `./subayai run file.yai`
*   **Clean**: `./subayai clean`
