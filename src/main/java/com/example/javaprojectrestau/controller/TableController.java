package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.model.Table;
import com.example.javaprojectrestau.service.OrderService;
import com.example.javaprojectrestau.service.TableService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TableController implements Initializable {

    @FXML private ListView<Table> availableTablesListView;
    @FXML private ListView<Table> occupiedTablesListView;
    
    @FXML private Label tableNumeroLabel;
    @FXML private Label tableCapaciteLabel;
    @FXML private Label tableEmplacementLabel;
    @FXML private Label tableStatutLabel;
    @FXML private Label tableCommandeLabel;
    
    @FXML private Button viewOrderBtn;
    @FXML private Button assignTableBtn;
    @FXML private Button releaseTableBtn;
    @FXML private Button editTableBtn;
    @FXML private Button deleteTableBtn;
    
    private final TableService tableService = new TableService();
    private final OrderService orderService = new OrderService();
    
    private ObservableList<Table> availableTables = FXCollections.observableArrayList();
    private ObservableList<Table> occupiedTables = FXCollections.observableArrayList();
    
    private Table selectedTable;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les listes
        availableTablesListView.setItems(availableTables);
        occupiedTablesListView.setItems(occupiedTables);
        
        // Configurer le rendu personnalisé des tables dans la ListView
        availableTablesListView.setCellFactory(param -> new ListCell<Table>() {
            @Override
            protected void updateItem(Table table, boolean empty) {
                super.updateItem(table, empty);
                if (empty || table == null) {
                    setText(null);
                } else {
                    setText(table.getNumero() + " (" + table.getCapacite() + " pers.)");
                }
            }
        });
        
        occupiedTablesListView.setCellFactory(param -> new ListCell<Table>() {
            @Override
            protected void updateItem(Table table, boolean empty) {
                super.updateItem(table, empty);
                if (empty || table == null) {
                    setText(null);
                } else {
                    setText(table.getNumero() + " (" + table.getCapacite() + " pers.)");
                }
            }
        });
        
        // Écouteurs de sélection
        availableTablesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedTable = newVal;
                occupiedTablesListView.getSelectionModel().clearSelection();
                showTableDetails(selectedTable);
            }
        });
        
        occupiedTablesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedTable = newVal;
                availableTablesListView.getSelectionModel().clearSelection();
                showTableDetails(selectedTable);
            }
        });
        
        // Charger les tables
        refreshTables();
    }
    
    private void refreshTables() {
        List<Table> allTables = tableService.getAllTables();
        
        availableTables.clear();
        occupiedTables.clear();
        
        for (Table table : allTables) {
            if (table.isOccupee()) {
                occupiedTables.add(table);
            } else {
                availableTables.add(table);
            }
        }
        
        // Actualiser les détails de la table sélectionnée
        if (selectedTable != null) {
            Optional<Table> updatedTable = allTables.stream()
                    .filter(t -> t.getId().equals(selectedTable.getId()))
                    .findFirst();
            
            updatedTable.ifPresent(table -> {
                selectedTable = table;
                showTableDetails(selectedTable);
            });
        }
    }
    
    private void showTableDetails(Table table) {
        tableNumeroLabel.setText(table.getNumero());
        tableCapaciteLabel.setText(String.valueOf(table.getCapacite()) + " personnes");
        tableEmplacementLabel.setText(table.getEmplacement());
        
        if (table.isOccupee()) {
            tableStatutLabel.setText("Occupée");
            tableStatutLabel.setStyle("-fx-text-fill: #e74c3c;");
            
            // Afficher les informations de la commande associée
            if (table.getOrderId() != null) {
                Optional<Order> orderOpt = orderService.getOrderById(table.getOrderId());
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    tableCommandeLabel.setText("Commande #" + order.getId() + " - " + order.getCustomerName());
                    viewOrderBtn.setVisible(true);
                } else {
                    tableCommandeLabel.setText("Aucune commande associée");
                    viewOrderBtn.setVisible(false);
                }
            } else {
                tableCommandeLabel.setText("Aucune commande associée");
                viewOrderBtn.setVisible(false);
            }
            
            // Activer/désactiver les boutons appropriés
            assignTableBtn.setDisable(true);
            releaseTableBtn.setDisable(false);
        } else {
            tableStatutLabel.setText("Disponible");
            tableStatutLabel.setStyle("-fx-text-fill: #2ecc71;");
            tableCommandeLabel.setText("Aucune");
            viewOrderBtn.setVisible(false);
            
            // Activer/désactiver les boutons appropriés
            assignTableBtn.setDisable(false);
            releaseTableBtn.setDisable(true);
        }
        
        // Activer les boutons d'édition et de suppression
        editTableBtn.setDisable(false);
        deleteTableBtn.setDisable(false);
    }
    
    private void clearTableDetails() {
        tableNumeroLabel.setText("");
        tableCapaciteLabel.setText("");
        tableEmplacementLabel.setText("");
        tableStatutLabel.setText("");
        tableCommandeLabel.setText("");
        viewOrderBtn.setVisible(false);
        
        assignTableBtn.setDisable(true);
        releaseTableBtn.setDisable(true);
        editTableBtn.setDisable(true);
        deleteTableBtn.setDisable(true);
    }
    
    @FXML
    public void handleAddTable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/add-table-view.fxml"));
            Parent root = loader.load();
            
            AddTableController controller = loader.getController();
            controller.setMode(AddTableController.Mode.ADD);
            controller.setOnTableSavedCallback(this::refreshTables);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une table");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleEditTable() {
        if (selectedTable == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une table à modifier.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/add-table-view.fxml"));
            Parent root = loader.load();
            
            AddTableController controller = loader.getController();
            controller.setMode(AddTableController.Mode.EDIT);
            controller.setTable(selectedTable);
            controller.setOnTableSavedCallback(this::refreshTables);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setTitle("Modifier une table");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleDeleteTable() {
        if (selectedTable == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une table à supprimer.");
            return;
        }
        
        if (selectedTable.isOccupee()) {
            showAlert(Alert.AlertType.WARNING, "Table occupée", "Impossible de supprimer une table occupée.");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer la table " + selectedTable.getNumero() + " ?");
        confirmation.setContentText("Cette action est irréversible.");
        
        // Appliquer le style au dialogue
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = tableService.deleteTable(selectedTable.getId());
            
            if (success) {
                selectedTable = null;
                clearTableDetails();
                refreshTables();
                
                showAlert(Alert.AlertType.INFORMATION, "Suppression réussie", "La table a été supprimée avec succès.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la table.");
            }
        }
    }
    
    @FXML
    public void handleReleaseTable() {
        if (selectedTable == null || !selectedTable.isOccupee()) {
            showAlert(Alert.AlertType.WARNING, "Action impossible", "Veuillez sélectionner une table occupée.");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Libérer la table");
        confirmation.setHeaderText("Libérer la table " + selectedTable.getNumero() + " ?");
        confirmation.setContentText("La table sera marquée comme disponible.");
        
        // Appliquer le style au dialogue
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = tableService.releaseTable(selectedTable.getId());
            
            if (success) {
                refreshTables();
                showAlert(Alert.AlertType.INFORMATION, "Table libérée", "La table a été libérée avec succès.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de libérer la table.");
            }
        }
    }
    
    @FXML
    public void handleAssignTable() {
        if (selectedTable == null || selectedTable.isOccupee()) {
            showAlert(Alert.AlertType.WARNING, "Action impossible", "Veuillez sélectionner une table disponible.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/add-order-view.fxml"));
            Parent root = loader.load();
            
            AddOrderController controller = loader.getController();
            controller.setTableId(selectedTable.getId());
            controller.setOnOrderAddedCallback(this::refreshTables);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setTitle("Nouvelle commande - Table " + selectedTable.getNumero());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de commande: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleViewOrder() {
        if (selectedTable == null || selectedTable.getOrderId() == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune commande", "Aucune commande n'est associée à cette table.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/order-detail-view.fxml"));
            Parent root = loader.load();
            
            OrderDetailController controller = loader.getController();
            controller.loadOrder(selectedTable.getOrderId());
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setTitle("Détails de la commande - Table " + selectedTable.getNumero());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de détails: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Appliquer le style au dialogue
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }
}
