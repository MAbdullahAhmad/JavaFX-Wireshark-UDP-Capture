#!/bin/bash

# Default path to JavaFX SDK
DEFAULT_MODULE_PATH="core/javafx-sdk/lib"

# Initialize variables
MODULE_PATH=$DEFAULT_MODULE_PATH
MODULES=""
CLASS_TO_RUN=""

# Parse arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --module-path) MODULE_PATH="$2"; shift ;;  # Replace default module path if --module-path is provided
        *) 
            if [[ -z "$CLASS_TO_RUN" ]]; then
                CLASS_TO_RUN="$1"  # The first non-option argument is the class to run
            else
                MODULES+="$1 "  # Subsequent arguments are treated as module names
            fi
            ;;
    esac
    shift
done

# Check if class to run is provided
if [ -z "$CLASS_TO_RUN" ]; then
    echo "Error: No class to run provided."
    echo "Usage: ./run.bash <class-name> [module-names] [--module-path <path>]"
    exit 1
fi

# Check if module names are provided
if [ -z "$MODULES" ]; then
    echo "Error: No module names provided."
    echo "Usage: ./run.bash <class-name> [module-names] [--module-path <path>]"
    exit 1
fi

# Compile the Java program
javac --module-path "$MODULE_PATH" --add-modules $MODULES $(find . -name "*.java")
if [ $? -eq 0 ]; then
    # Run the compiled Java program
    java --module-path "$MODULE_PATH" --add-modules $MODULES $CLASS_TO_RUN
else
    echo "Compilation failed."
fi
