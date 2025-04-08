#!/bin/bash

# Compiler l'application
mvn clean compile

# Lancer l'application avec la VM Java directement
java --module-path $JAVA_HOME/lib:./target/classes:./target/dependency --add-modules javafx.controls,javafx.fxml,java.sql -cp "./target/classes:./target/dependency/*" com.example.javaprojectrestau.HelloApplication
