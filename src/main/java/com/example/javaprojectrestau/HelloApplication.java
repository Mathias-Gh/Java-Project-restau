package com.example.javaprojectrestau;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.service.DishService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialiser la connexion à la base de données
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            showDatabaseAlert();
        }
        
        // Initialiser le service et créer les tables si nécessaire
        DishService dishService = new DishService();
        
        // Charger l'interface utilisateur
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("dish-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Système de Gestion de Restaurant");
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void stop() {
        // Fermer proprement la connexion à la base de données
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

    public static void main(String[] args) {
        launch();
    }
}