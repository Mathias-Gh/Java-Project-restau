package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Dish;
import com.example.javaprojectrestau.service.DishService;
import com.example.javaprojectrestau.db.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;

public class AddDishController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ImageView dishImageView;
    
    private final DishService dishService = new DishService();
    private File selectedImageFile;
    private Runnable onDishAddedCallback;
    
    @FXML
    public void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(dishImageView.getScene().getWindow());
        
        if (selectedImageFile != null) {
            Image image = new Image(selectedImageFile.toURI().toString());
            dishImageView.setImage(image);
        }
    }
    
    @FXML
    public void handleAddDish() {
        try {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String description = descriptionArea.getText().trim();
            
            if (name.isEmpty() || priceText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Le nom et le prix sont obligatoires.");
                return;
            }
            
            BigDecimal price;
            try {
                price = new BigDecimal(priceText.replace(",", "."));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Le prix doit être un nombre valide.");
                return;
            }
            
            String category = "Non catégorisé";
            if (categoryComboBox.getValue() != null) {
                category = categoryComboBox.getValue();
            }
            
            // Créer un nouveau plat
            Dish newDish = new Dish(null, name, price, description, category);
            Dish savedDish = dishService.saveDish(newDish);
            
            // Sauvegarder l'image si elle a été sélectionnée
            if (selectedImageFile != null && savedDish.getId() != null) {
                try (FileInputStream fis = new FileInputStream(selectedImageFile)) {
                    boolean imageSaved = DatabaseConnection.saveImage(savedDish.getId(), fis);
                    if (imageSaved) {
                        System.out.println("Image sauvegardée avec succès pour le plat ID: " + savedDish.getId());
                    } else {
                        System.err.println("Impossible de sauvegarder l'image pour le plat ID: " + savedDish.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la sauvegarde de l'image: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Plat ajouté", 
                      "Le plat '" + name + "' a été ajouté avec succès.");
                      
            // Appeler le callback si défini
            if (onDishAddedCallback != null) {
                onDishAddedCallback.run();
            }
            
            // Fermer la fenêtre
            ((Stage) nameField.getScene().getWindow()).close();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleCancel() {
        // Fermer la fenêtre sans sauvegarder
        ((Stage) nameField.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Définir un callback à exécuter quand un plat est ajouté
    public void setOnDishAddedCallback(Runnable callback) {
        this.onDishAddedCallback = callback;
    }
}
