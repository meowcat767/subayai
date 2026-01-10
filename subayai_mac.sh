#!/bin/bash
# Subayai CLI Wrapper for MacOS

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BIN_DIR="$PROJECT_ROOT/bin"
SRC_DIR="$PROJECT_ROOT/src"
MAIN_CLASS="site.meowcat.subayai.lang.Main"

function build() {
    echo "Building Subayai..."
    mkdir -p "$BIN_DIR"
    
    # Find all java files
    SOURCES=$(find "$SRC_DIR" -name "*.java")
    
    # Compile
    javac -d "$BIN_DIR" $SOURCES
    
    if [ $? -eq 0 ]; then
        echo "Build successful."
    else
        echo "Build failed."
        exit 1
    fi
}

function clean() {
    rm -rf "$BIN_DIR"
    echo "Cleaned build artifacts."
}

function run_subayai() {
    # Check if built, if not build
    if [ ! -f "$BIN_DIR/site/meowcat/subayai/lang/Main.class" ]; then
        build
    fi
    
    java -cp "$BIN_DIR" "$MAIN_CLASS" "$@"
}

function show_help() {
    echo "Subayai CLI Tool (MacOS)"
    echo "Usage:"
    echo "  ./subayai_mac.sh run <file.yai>   Run a Subayai source file"
    echo "  ./subayai_mac.sh build            Compile the Subayai interpreter"
    echo "  ./subayai_mac.sh clean            Remove build artifacts"
    echo "  ./subayai_mac.sh help             Show this help message"
}

# Wrapper logic
COMMAND=$1

case "$COMMAND" in
    build)
        build
        ;;
    clean)
        clean
        ;;
    run)
        run_subayai "$@"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        if [ -z "$COMMAND" ]; then
            show_help
        else
             run_subayai "$@"
        fi
        ;;
esac
