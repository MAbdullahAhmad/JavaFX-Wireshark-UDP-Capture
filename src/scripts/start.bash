#!/bin/bash

# Set the path to the JavaFX SDK lib directory
JAVAFX_LIB=core/javafx-sdk/lib

# Set the path to your PostgreSQL JDBC driver
POSTGRESQL_JAR=core/postgresql/postgresql.jar

# Set the path to your application's main class (AppLauncher)
MAIN_CLASS=AppLauncher

# Collect all JavaFX JAR files
JAVAFX_JARS=$(echo $JAVAFX_LIB/*.jar | tr ' ' ':')

# Compile the Java source files, adding both the JavaFX SDK and PostgreSQL JDBC driver to the classpath
javac -cp "$JAVAFX_JARS:$POSTGRESQL_JAR" -d . lib/*.java main/*.java *.java

# Run the JavaFX application with the necessary modules and PostgreSQL driver
java --module-path "$JAVAFX_LIB" --add-modules=javafx.controls,javafx.fxml -cp ".:$POSTGRESQL_JAR:main" "$MAIN_CLASS"

# 
# Note:
#   Run this script from `/` of project
# 