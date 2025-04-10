# Système de Gestion de Restaurant

Une application Java complète pour la gestion des opérations d'un restaurant, offrant des fonctionnalités pour la gestion des tables, des commandes, des plats et du personnel.

## Fonctionnalités

- **Gestion des plats** : Créer, modifier et supprimer des plats du menu avec prix, catégorie et description
- **Gestion des commandes** : Prendre des commandes, suivre leur statut, et marquer les commandes comme préparées
- **Gestion des tables** : Suivre les tables disponibles et occupées, assigner des commandes aux tables
- **Gestion du personnel** : Gérer les informations des employés et leurs rôles
- **Rapports financiers** : Consulter les statistiques de vente et générer des rapports

## Captures d'écran

![Gestion des tables](src/main/resources/com/example/javaprojectrestau/images/screenshot_tables.png)
![Menu](src/main/resources/com/example/javaprojectrestau/images/screenshot_menu.png)
![Gestion des commandes](src/main/resources/com/example/javaprojectrestau/images/screenshot_orders.png)

## Technologies utilisées

- **JavaFX** : Pour l'interface utilisateur graphique
- **JDBC** : Pour la connexion à la base de données
- **MySQL** : Base de données relationnelle
- **Maven** : Pour la gestion des dépendances et le build du projet

## Structure du projet

```
src/
├── main/
│   ├── java/com/example/javaprojectrestau/
│   │   ├── controller/     # Contrôleurs JavaFX
│   │   ├── dao/            # Data Access Objects pour l'accès à la base de données
│   │   ├── db/             # Configuration de la base de données
│   │   ├── model/          # Classes de modèle (Order, Table, Dish, etc.)
│   │   ├── service/        # Couche de service avec la logique métier
│   │   └── HelloApplication.java  # Point d'entrée de l'application
│   └── resources/
│       ├── com/example/javaprojectrestau/
│       │   ├── styles/     # Feuilles de style CSS
│       │   ├── images/     # Images et icônes
│       │   └── *.fxml      # Fichiers de mise en page FXML
│       └── META-INF/
└── test/                   # Tests unitaires
```

## Prérequis

- Java JDK 17 ou supérieur
- MySQL 8.0 ou supérieur
- Maven 3.8 ou supérieur

## Installation

1. Clonez le dépôt :
   ```
   git clone https://github.com/Mathias-Gh/Java-Project-restau.git
   cd java-project-restau
   ```

2. Configurez la base de données :
   - Créez une base de données MySQL nommée `restaurant_db`
   - Modifiez les identifiants de connexion dans `DatabaseConnection.java` si nécessaire

3. Compilez et lancez le projet :
   ```
   mvn clean javafx:run
   ```

## Utilisation

1. **Ajouter une table** : Cliquez sur "Gestion des tables" puis "Ajouter une table" et remplissez les détails nécessaires
2. **Créer une commande** : Naviguez vers "Gestion des commandes", sélectionnez une table disponible, puis ajoutez les plats à la commande
3. **Gérer le statut des commandes** : Utilisez la vue des commandes pour changer leur statut en "PRÉPARÉE" ou "ANNULÉE"
4. **Consulter les rapports** : Accédez à "Rapport financier" pour voir les ventes et les performances

## Fonctionnalités avancées

- **Chronomètre de service** : Système pour suivre le temps de préparation des commandes
- **Galerie de menu** : Affichage visuel des plats disponibles avec photos
- **Multi-utilisateurs** : Support pour différents niveaux d'accès (serveur, chef, gérant)

## Contribuer

Les contributions sont les bienvenues! Pour contribuer:

1. Forkez le projet
2. Créez votre branche de fonctionnalité (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Commitez vos changements (`git commit -m 'Ajout d'une nouvelle fonctionnalité'`)
4. Poussez vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Ouvrez une Pull Request

## Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.

## Remerciements

- Merci à tous ceux qui ont contribué au projet

