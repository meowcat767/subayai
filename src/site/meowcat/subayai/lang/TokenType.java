package site.meowcat.subayai.lang;

public enum TokenType {
    // Single-character tokens
    LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    LEFT_PAREN, RIGHT_PAREN, COLON,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    ARROW, // ->

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    BREAK, CASE, CALL, CLASS, CONTINUE, DEFAULT, DEFER,
    DEFINE, ELSE, FOR, FINAL, IF, IMPORT, INTERFACE,
    LOCK, NOTLOCK, PKG, RANGE, RETURN, SELECT, SWITCH,
    TYPE, VAR,
    
    // Special
    EOF
}
