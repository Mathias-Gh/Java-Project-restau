package com.example.javaprojectrestau;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.service.DishService;
import com.example.javaprojectrestau.service.OrderService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // connexion à la base de données
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            showDatabaseAlert();
        }
        
        // Initialiser les services et créer les tables si nécessaire
        DishService dishService = new DishService();
        OrderService orderService = new OrderService();
        
        // menu pour naviguer entre les vues
        MenuBar menuBar = new MenuBar();
        Menu navigationMenu = new Menu("Navigation");
        
        MenuItem dishesItem = new MenuItem("Gestion des plats");
        dishesItem.setOnAction(e -> loadView(stage, "dish-view.fxml", "Gestion des plats"));
        
        MenuItem ordersItem = new MenuItem("Gestion des commandes");
        ordersItem.setOnAction(e -> loadView(stage, "order-view.fxml", "Gestion des commandes"));
        
        // Ajouter le nouvel élément pour afficher le menu avec une belle interface visuelle
        MenuItem menuGalleryItem = new MenuItem("Affichage du Menu");
        menuGalleryItem.setOnAction(e -> loadView(stage, "menu-gallery-view.fxml", "Menu du Restaurant"));
        
        navigationMenu.getItems().addAll(dishesItem, ordersItem, menuGalleryItem);
        menuBar.getMenus().add(navigationMenu);
        
        // Charge la vue des plats par défaut
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("dish-view.fxml"));
        VBox rootContent = new VBox();
        rootContent.getChildren().add(menuBar);
        rootContent.getChildren().add(fxmlLoader.load());
        
        Scene scene = new Scene(rootContent, 900, 600);
        stage.setTitle("Système de Gestion de Restaurant");
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void stop() {
        // Ferme proprement la connexion à la base de données
        DatabaseConnection.closeConnection();
    }

    private void showDatabaseAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Problème de connexion à la base de données");
        alert.setHeaderText("Impossible de se connecter à MySQL");
        alert.setContentText("L'application fonctionnera en mode limité sans accès à la base de données.\n\n" +
                "Vérifiez que:\n" +
                "1. MySQL est installé et en cours d'exécution\n" +
                "2. Les identifiants dans DatabaseConnection.java sont corrects");
        
        alert.showAndWait();
    }

    private void loadView(Stage stage, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node content = loader.load();
            
            VBox root = (VBox) stage.getScene().getRoot();
            
            // Conserve le menuBar et remplace le contenu
            root.getChildren().remove(1, root.getChildren().size());
            root.getChildren().add(content);
            
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}