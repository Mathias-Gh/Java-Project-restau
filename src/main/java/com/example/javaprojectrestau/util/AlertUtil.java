package com.example.javaprojectrestau.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Classe utilitaire pour afficher des alertes et dialogues stylisés
 */
public class AlertUtil {
    
    /**
     * Affiche une alerte simple avec un type, un titre et un message
     * 
     * @param alertType Le type d'alerte (INFO, WARNING, ERROR, etc.)
     * @param title Le titre de l'alerte
     * @param message Le message à afficher
     * @return Le résultat du dialogue (le bouton cliqué)
     */
    public static ButtonType showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Appliquer des styles si nécessaire
        applyStyles(alert);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL);
    }
    
    /**
     * Affiche une demande de confirmation avec un titre et un message
     * 
     * @param title Le titre de la confirmation
     * @param message Le message à afficher
     * @return true si OK est cliqué, false sinon
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Appliquer des styles si nécessaire
        applyStyles(alert);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Applique des styles à l'alerte
     * 
     * @param alert L'alerte à styliser
     */
    private static void applyStyles(Alert alert) {
        // Obtenir la scène et y appliquer la feuille de styles
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert.getDialogPane().getStylesheets().add(
            AlertUtil.class.getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        
        // Autres personnalisations du style si nécessaire
    }
}
