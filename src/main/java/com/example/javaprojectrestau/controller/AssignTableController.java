package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.TableResto;
import com.example.javaprojectrestau.service.TableRestoService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AssignTableController {

    @FXML private TableView<TableResto> tableView;
    @FXML private TableColumn<TableResto, Integer> idColumn;
    @FXML private TableColumn<TableResto, Integer> tailleColumn;
    @FXML private TableColumn<TableResto, Integer> emplacementColumn;
    @FXML private TableColumn<TableResto, Boolean> disponibleColumn;

    @FXML private TextField clientIdField;
    @FXML private TextField commandeField;

    private TableRestoService tableService;
    private ObservableList<TableResto> tableList;

    @FXML
    public void initialize() {
        tableService = new TableRestoService(DatabaseConnection.getConnection());

        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tailleColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTaille()).asObject());
        emplacementColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getEmplacement()).asObject());
        disponibleColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isDisponible()).asObject());

        refreshTableData();
    }

    private void refreshTableData() {
        tableList = FXCollections.observableArrayList(tableService.getAllTables());
        tableView.setItems(tableList);
    }

    @FXML
    public void assignTableToClient() {
        TableResto selectedTable = tableView.getSelectionModel().getSelectedItem();

        if (selectedTable == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune table sélectionnée.");
            return;
        }

        if (!selectedTable.isDisponible()) {
            showAlert(Alert.AlertType.WARNING, "La table sélectionnée n'est pas disponible.");
            return;
        }

        try {
            int clientId = Integer.parseInt(clientIdField.getText().trim());
            String commande = commandeField.getText().trim();

            boolean success = tableService.assignTableToClient(selectedTable.getId(), clientId, commande); // <-- méthode modifiée
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "✅ Table assignée et commande enregistrée !");
                refreshTableData();
            } else {
                showAlert(Alert.AlertType.ERROR, "❌ Erreur lors de l'assignation de la table.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID client invalide.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Assignation de Table");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
