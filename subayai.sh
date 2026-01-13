#!/bin/bash

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$PROJECT_ROOT/src"
BIN_DIR="$PROJECT_ROOT/bin"
MAIN_CLASS="site.meowcat.subayai.lang.Main"
TARGET_EXE="$PROJECT_ROOT/subayai"

function build() {
    echo "Building Subayai for Linux..."
    
    # 1. Clean and Prepare
    rm -rf "$BIN_DIR"
    mkdir -p "$BIN_DIR"
    
    # 2. Compile Java Source
    SOURCES=$(find "$SRC_DIR" -name "*.java")
    javac -d "$BIN_DIR" $SOURCES
    
    if [ $? -ne 0 ]; then
        echo "Build failed."
        exit 1
    fi

    # 3. Create the self-contained executable
    echo "Creating single executable file..."
    
    # Create a temporary JAR
    jar cfe subayai.jar "$MAIN_CLASS" -C "$BIN_DIR" .
    
    # Construct the hybrid script/JAR
    # The shell runs the first few lines, Java ignores them and reads the ZIP data
    cat << 'EOF' > subayai_new
#!/bin/sh
exec java -jar "$0" "$@"
EOF
    cat subayai.jar >> subayai_new
    
    # 4. Finalize and Cleanup
    chmod +x subayai_new
    mv subayai_new "$TARGET_EXE"
    
    rm subayai.jar
    rm -rf "$BIN_DIR"
    
    echo "Build successful. You can now run ./subayai"
}

function clean() {
    rm -rf "$BIN_DIR"
    echo "Cleaned build artifacts."
}

function show_help() {
    echo "Subayai CLI Tool"
    echo "Usage:"
    echo "  ./subayai build            Compile into a single executable"
    echo "  ./subayai run <file.yai>   Run a Subayai source file"
    echo "  ./subayai clean            Remove build artifacts"
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
        # If the file is the hybrid executable, we just run it
        # Otherwise, we pass the 'run' command to the Main class
        shift
        "$TARGET_EXE" run "$@"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        if [ -z "$COMMAND" ]; then
            show_help
        else
            # Default to passing all args to the interpreter
            "$TARGET_EXE" "$@"
        fi
        ;;
esac