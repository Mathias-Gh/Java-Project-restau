package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Employe;
import com.example.javaprojectrestau.service.EmployeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
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
        employeList.addAll(employeService.getAllEmployes());

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

    // Méthode pour effacer les détails de l'employé
    private void clearEmployeDetails() {
        if (employeDetailsName != null) {
            employeDetailsName.setText("");
        }
        if (employeDetailsWorking_hour != null) {
            employeDetailsWorking_hour.setText("");
        }
        if (employeDetailsHour_worked != null) {
            employeDetailsHour_worked.setText("");
        }
        if (employeDetailsPost != null) {
            employeDetailsPost.setText("");
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
            // Appliquer le style sombre
            scene.getStylesheets().add(getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouvel Employé");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Empêcher l'interaction avec la fenêtre principale

            // Afficher la fenêtre
            stage.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout d'un employé: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddHourWorked() {
        if (selectedEmploye == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un employé.");
            return;
        }

        // Créer le dialogue personnalisé
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Ajouter des heures travaillées");
        dialog.setHeaderText("Ajouter des heures pour " + selectedEmploye.getName());

        // Appliquer le thème sombre au dialogue
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        // Ajouter les boutons
        ButtonType buttonTypeOk = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

        // Créer et configurer le spinner pour les heures
        Spinner<Integer> spinner = new Spinner<>(1, 100, 1);
        spinner.setEditable(true);

        // Créer le conteneur pour le spinner avec label
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Heures:"), 0, 0);
        grid.add(spinner, 1, 0);

        dialogPane.setContent(grid);

        // Convertir le résultat du dialogue
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk) {
                return spinner.getValue();
            }
            return null;
        });

        // Afficher le dialogue et traiter le résultat
        Optional<Integer> result = dialog.showAndWait();

        result.ifPresent(hours -> {
            try {
                // Mettre à jour les heures travaillées dans le modèle de l'employé
                int currentHours = selectedEmploye.getHour_worked();
                selectedEmploye.setHour_worked(currentHours + hours);

                // Mettre à jour l'employé dans la base de données
                employeService.updateEmploye(selectedEmploye);

                refreshEmployeList();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        hours + " heure(s) ajoutée(s) pour " + selectedEmploye.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'ajouter les heures: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteEmploye() {
        if (selectedEmploye == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un employé à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'employé");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + selectedEmploye.getName() + " ?");

        // Appliquer le thème sombre
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
        dialogPane.getStyleClass().add("warning");

        // Personnaliser les boutons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("button-danger");
        okButton.setText("Supprimer");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Annuler");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Supprimer l'employé
                employeService.deleteEmploye(selectedEmploye.getId());
                selectedEmploye = null;
                refreshEmployeList();
                clearEmployeDetails();
                showAlert(Alert.AlertType.INFORMATION, "Suppression réussie", "L'employé a été supprimé avec succès.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'employé: " + e.getMessage());
            }
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

        // Appliquer le thème sombre
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

}
