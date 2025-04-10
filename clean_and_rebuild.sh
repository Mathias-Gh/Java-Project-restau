#!/bin/bash

echo "============= NETTOYAGE ET RECONSTRUCTION DU PROJET ============="
echo "Nettoyage complet du projet..."
mvn clean

echo "Suppression des fichiers .class potentiellement corrompus..."
find . -name "*.class" -delete

echo "Suppression des fichiers générés par l'IDE qui pourraient être corrompus..."
find . -name "*.iml" -delete

echo "Nettoyage du répertoire target..."
rm -rf ./target

echo "Modification du pom.xml pour utiliser --release 22 au lieu de -source et -target..."
# On pourrait utiliser sed ici, mais pour éviter les problèmes de compatibilité, vous devrez ajuster manuellement votre pom.xml

echo "Reconstruction du projet..."
mvn compile

echo "==============================================================" 
echo "Nettoyage terminé. Le projet est prêt à être exécuté avec 'mvn javafx:run'"
echo "=============================================================="
