#!/bin/bash

# Set the path to the JavaFX SDK lib directory
JAVAFX_LIB=core/javafx-sdk/lib

# Set the path to your application's main class (AppLauncher)
MAIN_CLASS=AppLauncher

# Collect all JavaFX JAR files
JAVAFX_JARS=$(echo $JAVAFX_LIB/*.jar | tr ' ' ':')

# Compile the Java source files, adding the JavaFX SDK to the classpath
javac -cp "$JAVAFX_JARS" -d . main/*.java *.java

# Run the JavaFX application with the necessary modules
java --module-path "$JAVAFX_LIB" --add-modules=javafx.controls,javafx.fxml -cp .:main "$MAIN_CLASS"
