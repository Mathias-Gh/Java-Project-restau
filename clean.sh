#!/bin/bash

echo "Nettoyage complet du projet..."
mvn clean

echo "Suppression des fichiers .class potentiellement corrompus..."
find ./target -name "*.class" -delete

echo "Nettoyage du répertoire target..."
rm -rf ./target

echo "Reconstruction du projet..."
mvn compile

echo "Nettoyage terminé. Le projet est prêt à être exécuté."
