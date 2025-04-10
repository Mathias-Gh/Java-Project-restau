package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Employe;
import com.example.javaprojectrestau.service.EmployeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EmployeController implements Initializable {

    private final EmployeService employeService = new EmployeService();

    // Champs existants pour les tables et les colonnes
    @FXML
    private TableView<Employe> employeTable;
    @FXML private TableColumn<Employe, Long> idColumn;
    @FXML private TableColumn<Employe, String> nameColumn;
    @FXML private TableColumn<Employe, String> postColumn;
    @FXML private TableColumn<Employe, Integer> working_hourColumn;
    @FXML private TableColumn<Employe, Integer> hour_workedColumn;

    // Champs pour les formulaires
    @FXML private TextField nameField;
    @FXML private TextField working_hourField;
    @FXML private TextField hour_workedField;
    @FXML private TextField postField;

    // Nouveaux champs pour les images et la liste
    @FXML private ListView<Employe> employeListView;
    @FXML private Label employeDetailsName;
    @FXML private Label employeDetailsWorking_hour;
    @FXML private Label employeDetailsHour_worked;
    @FXML private Label employeDetailsPost;
    private Employe selectedEmploye;
    private final ObservableList<Employe> employeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurer les colonnes du TableView
        if (employeTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            working_hourColumn.setCellValueFactory(new PropertyValueFactory<>("working_hour"));
            hour_workedColumn.setCellValueFactory(new PropertyValueFactory<>("hour_worked"));
            postColumn.setCellValueFactory(new PropertyValueFactory<>("post"));

            // Ajouter un listener pour afficher les détails de l'employé sélectionné
            employeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedEmploye = newSelection;
                    showEmployeDetails(selectedEmploye);
                }
            });
        }

        // Charger les employés dans la liste
        refreshEmployeList();
    }

    private void refreshEmployeList() {
        employeList.clear();
        employeList.addAll(employeService.getAllEmploye());

        if (employeTable != null) {
            employeTable.setItems(employeList);

            // Réafficher les détails de l'employé sélectionné
            if (selectedEmploye != null) {
                // Rechercher l'employé mis à jour dans la liste
                selectedEmploye = employeList.stream()
                        .filter(e -> e.getId().equals(selectedEmploye.getId()))
                        .findFirst()
                        .orElse(null);

                showEmployeDetails(selectedEmploye);
            }
        }
    }

    private void showEmployeDetails(Employe employe) {
        if (employeDetailsName != null) {
            employeDetailsName.setText(employe.getName());
        }
        if (employeDetailsWorking_hour != null) {
            employeDetailsWorking_hour.setText(employe.getWorking_hour() + " heures");
        }
        if (employeDetailsHour_worked != null) {
            employeDetailsHour_worked.setText(employe.getHour_worked() + " heures");
        }
        if (employeDetailsPost != null) {
            employeDetailsPost.setText(employe.getPost());
        }
    }

    // Modifier la méthode pour ouvrir une nouvelle fenêtre d'ajout de l'employé
    @FXML
    public void handleAddEmploye() {
        try {
            // Charger la vue pour ajouter un employé
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/add-employe-view.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur
            AddEmployeController controller = loader.getController();

            // Définir le callback à exécuter après l'ajout d'un employé
            controller.setOnEmployeAddedCallback(this::refreshEmployeList);

            // Créer une nouvelle scène et fenêtre
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouvel Employé");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Empêcher l'interaction avec la fenêtre principale

            // Afficher la fenêtre
            stage.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'ouverture de la fenêtre d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    public void handleAddHourWorked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/add-hour_worked-view.fxml"));
            Parent root = loader.load();

            AddHourWorkedController controller = loader.getController();
            controller.setOnHoursAddedCallback(() -> {
                refreshEmployeList(); // Met à jour la liste et les détails
            });

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Ajouter des heures travaillées");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'ouverture du formulaire : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteEmploye() {
        if (selectedEmploye != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmer la suppression");
            confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer cet employé ?");
            confirmation.setContentText(selectedEmploye.getName());

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    employeService.deleteEmploye(selectedEmploye.getId());
                    clearFields();
                    selectedEmploye = null;
                    refreshEmployeList();
                }
            });
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un employé à supprimer.");
        }
    }

    private void clearFields() {
        if (nameField != null) nameField.clear();
        if (working_hourField != null) working_hourField.clear();
        if (hour_workedField != null) hour_workedField.clear();
        if (postField != null) postField.clear();
    }

    private void showAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
