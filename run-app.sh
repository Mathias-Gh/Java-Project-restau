#!/bin/bash

# Trouver le chemin vers JavaFX
JAVAFX_PATH="$HOME/Library/Java/JavaVirtualMachines/temurin-22.0.2/Contents/Home/lib"
MYSQL_JAR="$HOME/.m2/repository/mysql/mysql-connector-java/8.0.27/mysql-connector-java-8.0.27.jar"
H2_JAR="$HOME/.m2/repository/com/h2database/h2/2.1.214/h2-2.1.214.jar"

# Compiler l'application
echo "Compilation de l'application..."
mvn clean compile

# Créer le répertoire target/dependency s'il n'existe pas
mkdir -p target/dependency

# Lancer l'application avec la VM Java directement
echo "Lancement de l'application..."
java \
  --module-path "$JAVAFX_PATH:target/classes" \
  --add-modules javafx.controls,javafx.fxml \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
  -classpath "target/classes:$MYSQL_JAR:$H2_JAR" \
  com.example.javaprojectrestau.HelloApplication
```
