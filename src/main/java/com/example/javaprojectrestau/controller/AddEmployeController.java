package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Employe;
import com.example.javaprojectrestau.service.EmployeService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddEmployeController {

    @FXML private TextField nameField;
    @FXML private Spinner working_hourField;
    @FXML private TextField postField;

    private final EmployeService employeService = new EmployeService();
    private Runnable onEmployeAddedCallback;


    @FXML
    public void handleAddEmploye() {
        try {
            String name = nameField.getText().trim();
            int working_hour = (int) working_hourField.getValue();
            String post = postField.getText().trim();

            if (name.isEmpty() || post.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Le nom et le poste sont obligatoires.");
                return;
            }

            // Créer un nouvel employé
            Employe newEmploye = new Employe(null, name, working_hour, 0, post);
            Employe savedEmploye = employeService.saveEmploye(newEmploye);

            // Sauvegarder l'image si elle a été sélectionnée

            showAlert(Alert.AlertType.INFORMATION, "employé ajouté",
                    "L'employé '" + name + "' a été ajouté avec succès.");

            // Appeler le callback si défini
            if (onEmployeAddedCallback != null) {
                onEmployeAddedCallback.run();
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

    // Définir un callback à exécuter quand un employé est ajouté
    public void setOnEmployeAddedCallback(Runnable callback) {
        this.onEmployeAddedCallback = callback;
    }
}