#!/bin/bash

# Ce script utilise le plugin JavaFX Maven pour exécuter l'application
echo "Exécution de l'application via le plugin JavaFX Maven..."

# Chemin vers JavaFX - ajustez selon votre installation
JAVAFX_PATH="$HOME/Library/Java/JavaVirtualMachines/temurin-22.0.2/Contents/Home/lib"

# Exécuter avec Maven et le plugin JavaFX
JAVAFX_MODULES_PATH=$JAVAFX_PATH mvn javafx:run

