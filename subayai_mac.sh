#!/bin/bash

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$PROJECT_ROOT/src"
BIN_DIR="$PROJECT_ROOT/bin"
MAIN_CLASS="site.meowcat.subayai.lang.Main"
TARGET_EXE="$PROJECT_ROOT/subayai"

function build() {
    echo "🔨 Building Subayai for macOS..."
    
    # 1. Clean and Prepare
    rm -rf "$BIN_DIR"
    mkdir -p "$BIN_DIR"
    
    # 2. Compile Java Source
    find "$SRC_DIR" -name "*.java" > sources.txt
    javac -d "$BIN_DIR" @sources.txt
    rm sources.txt
    
    if [ $? -ne 0 ]; then
        echo "❌ Compilation failed."
        exit 1
    fi

    # 3. Package into a self-executing hybrid file
    echo "📦 Packaging into a single executable..."
    
    # Create the JAR part
    jar cfe subayai.jar "$MAIN_CLASS" -C "$BIN_DIR" .
    
    # Create the executable with a Java shebang
    # This allows the shell to run it, and Java to interpret the ZIP data
    echo '#!/bin/sh' > "$TARGET_EXE"
    echo 'exec java -jar "$0" "$@"' >> "$TARGET_EXE"
    cat subayai.jar >> "$TARGET_EXE"
    
    # 4. Permissions and Cleanup
    chmod +x "$TARGET_EXE"
    rm subayai.jar
    rm -rf "$BIN_DIR"
    
    echo "✅ Success! Run with: ./subayai"
}

function clean() {
    rm -rf "$BIN_DIR"
    rm -f "$TARGET_EXE"
    echo "🧹 Cleaned."
}

# CLI Logic
case "$1" in
    build)
        build
        ;;
    clean)
        clean
        ;;
    run)
        shift
        "$TARGET_EXE" run "$@"
        ;;
    *)
        echo "Usage: $0 {build|run|clean}"
        exit 1
        ;;
esac
