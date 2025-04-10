package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Employe;
import com.example.javaprojectrestau.service.EmployeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class AddHourWorkedController {

    @FXML private ComboBox<Employe> employeeComboBox;
    @FXML private Spinner<Integer> hourWorkedSpinner;

    private final EmployeService employeService = new EmployeService();
    private Runnable onHoursAddedCallback;

    @FXML
    public void initialize() {
        // Charger les employés dans la ComboBox
        ObservableList<Employe> employeList = FXCollections.observableArrayList(employeService.getAllEmploye());
        employeeComboBox.setItems(employeList);

        // Configurer le ComboBox pour afficher uniquement les noms
        employeeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employe employe) {
                return employe != null ? employe.getName() : "";
            }

            @Override
            public Employe fromString(String string) {
                return employeList.stream()
                        .filter(employe -> employe.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }
    @FXML
    public void handleAddHourWorked() {
        try {
            Employe selectedEmploye = employeeComboBox.getValue();
            int hoursWorked = hourWorkedSpinner.getValue();

            if (selectedEmploye == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Veuillez sélectionner un employé.");
                return;
            }

            // Mettre à jour les heures travaillées dans la colonne hour_worked
            selectedEmploye.setHour_worked(selectedEmploye.getHour_worked() + hoursWorked);
            employeService.saveEmploye(selectedEmploye);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Les heures travaillées ont été ajoutées avec succès.");

            // Appeler le callback si défini
            if (onHoursAddedCallback != null) {
                onHoursAddedCallback.run();
            }

            // Fermer la fenêtre
            ((Stage) employeeComboBox.getScene().getWindow()).close();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        // Fermer la fenêtre sans sauvegarder
        ((Stage) employeeComboBox.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setOnHoursAddedCallback(Runnable callback) {
        this.onHoursAddedCallback = callback;
    }
}