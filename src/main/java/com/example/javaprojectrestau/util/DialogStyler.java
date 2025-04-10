package com.example.javaprojectrestau.util;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

/**
 * Utilitaire pour appliquer le style sombre aux dialogues et alertes
 */
public class DialogStyler {
    
    /**
     * Applique le style sombre à une scene
     * @param scene la scene à styliser
     */
    public static void applyDarkStyle(Scene scene) {
        if (scene != null && !scene.getStylesheets().contains(
                DialogStyler.class.getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm())) {
            scene.getStylesheets().add(
                DialogStyler.class.getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
            );
        }
    }
    
    /**
     * Applique le style sombre à un stage
     * @param stage le stage à styliser
     */
    public static void applyDarkStyle(Stage stage) {
        if (stage != null) {
            applyDarkStyle(stage.getScene());
        }
    }
    
    /**
     * Applique le style sombre à une alerte
     * @param alert l'alerte à styliser
     */
    public static void applyDarkStyle(Alert alert) {
        if (alert != null) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                DialogStyler.class.getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
            );
            dialogPane.getStyleClass().add("dialog-pane");
        }
    }
    
    /**
     * Crée et affiche une alerte stylisée avec le thème sombre
     * @param type le type d'alerte
     * @param title le titre de l'alerte
     * @param message le message de l'alerte
     * @return le résultat du dialogue (bouton cliqué)
     */
    public static ButtonType showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        applyDarkStyle(alert);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL);
    }
    
    /**
     * Crée et affiche une alerte de confirmation stylisée avec le thème sombre
     * @param title le titre de l'alerte
     * @param header l'en-tête de l'alerte
     * @param message le message de l'alerte
     * @return true si l'utilisateur confirme, false sinon
     */
    public static boolean showConfirmation(String title, String header, String message) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(title);
        confirmation.setHeaderText(header);
        confirmation.setContentText(message);
        
        applyDarkStyle(confirmation);
        
        return confirmation.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }
}
