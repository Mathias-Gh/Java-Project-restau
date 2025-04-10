package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Client;
import com.example.javaprojectrestau.model.TableResto;
import com.example.javaprojectrestau.service.ClientService;
import com.example.javaprojectrestau.service.TableRestoService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TableViewController {

    @FXML private TableView<TableResto> tableView;
    @FXML private TableColumn<TableResto, Integer> idColumn;
    @FXML private TableColumn<TableResto, Integer> tailleColumn;
    @FXML private TableColumn<TableResto, Integer> emplacementColumn;
    @FXML private TableColumn<TableResto, Boolean> disponibleColumn;

    @FXML private TextField clientNameField;
    @FXML private ComboBox<Client> clientComboBox;
    @FXML private TextField tailleField;
    @FXML private TextField emplacementField;
    @FXML private CheckBox disponibleCheckBox;

    private TableRestoService tableRestoService;
    private ClientService clientService;
    private ObservableList<TableResto> tableList;
    private ObservableList<Client> clientList;

    @FXML
    public void initialize() {
        tableRestoService = new TableRestoService(DatabaseConnection.getConnection());
        clientService = new ClientService(DatabaseConnection.getConnection());

        // Initialisation des colonnes de la TableView des tables
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tailleColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTaille()).asObject());
        emplacementColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getEmplacement()).asObject());
        disponibleColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isDisponible()).asObject());

        refreshTableData();
        refreshClientData();
    }

    // Rafraîchir les données des tables disponibles
    private void refreshTableData() {
        tableList = FXCollections.observableArrayList(tableRestoService.getAllTables());
        tableView.setItems(tableList);
    }

    // Rafraîchir les données des clients
    private void refreshClientData() {
        clientList = FXCollections.observableArrayList(clientService.getAllClients());
        clientComboBox.setItems(clientList);
    }

    // Créer un client
    @FXML
    public void createClient() {
        String clientName = clientNameField.getText();
        if (clientName == null || clientName.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un nom pour le client.");
            return;
        }

        Client newClient = clientService.createClient(clientName);
        if (newClient != null) {
            showAlert("Succès", "Client créé avec succès !");
            refreshClientData(); // Mettre à jour la liste des clients dans le ComboBox
        } else {
            showAlert("Erreur", "Erreur lors de la création du client.");
        }
    }

    // Assigner une table à un client sélectionné
    @FXML
    public void assignTable() {
        Client selectedClient = clientComboBox.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Erreur", "Veuillez sélectionner un client.");
            return;
        }

        String tailleText = tailleField.getText();
        String emplacementText = emplacementField.getText();
        boolean estDisponible = disponibleCheckBox.isSelected();

        if (tailleText.isEmpty() || emplacementText.isEmpty()) {
            showAlert("Erreur", "Veuillez renseigner tous les champs pour la table.");
            return;
        }

        int taille;
        try {
            taille = Integer.parseInt(tailleText);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La taille de la table doit être un nombre.");
            return;
        }

        // Convertir emplacementText en int
        int emplacement;
        try {
            emplacement = Integer.parseInt(emplacementText); // Convertir en int
        } catch (NumberFormatException e) {
            showAlert("Erreur", "L'emplacement de la table doit être un nombre.");
            return;
        }

        TableResto tableDisponible = tableList.stream()
                .filter(TableResto::isDisponible)
                .findFirst()
                .orElse(null);

        if (tableDisponible == null) {
            showAlert("Aucune table disponible", "Toutes les tables sont occupées.");
            return;
        }

        // Mettre à jour la table avec les informations fournies
        tableDisponible.setTaille(taille);
        tableDisponible.setEmplacement(emplacement); // Assignation de l'emplacement
        tableDisponible.setDisponible(estDisponible); // Mettre à jour la disponibilité

        // Assigner la table au client
        tableRestoService.assignTableToClient(tableDisponible.getId(), selectedClient.getId());

        refreshTableData(); // Rafraîchir la TableView pour afficher les mises à jour
        showAlert("Succès", "Table " + tableDisponible.getId() + " assignée au client " + selectedClient.getNom());
    }

    // Afficher une alerte
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
