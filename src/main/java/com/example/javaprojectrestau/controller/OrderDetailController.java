package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.model.OrderItem;
import com.example.javaprojectrestau.model.Table;
import com.example.javaprojectrestau.service.OrderService;
import com.example.javaprojectrestau.service.TableService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrderDetailController implements Initializable {

    @FXML private Label customerNameLabel;
    @FXML private Label tableLabel;
    @FXML private Label statusLabel;
    @FXML private Label orderTimeLabel;
    @FXML private Label totalPriceLabel;
    @FXML private TextArea notesTextArea;
    
    @FXML private TableView<OrderItem> orderItemsTableView;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, String> dishNameColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> priceColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> totalColumn;
    
    @FXML private Button markCompletedBtn;
    @FXML private Button closeBtn;
    
    private final OrderService orderService = new OrderService();
    private final TableService tableService = new TableService();
    private Order currentOrder;
    private ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes du tableau
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dishNameColumn.setCellValueFactory(new PropertyValueFactory<>("dishName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(cellData -> {
            OrderItem item = cellData.getValue();
            BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new SimpleObjectProperty<>(total);
        });
        
        orderItemsTableView.setItems(orderItems);
    }
    
    public void loadOrder(Long orderId) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            currentOrder = orderOpt.get();
            displayOrderDetails();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Commande introuvable");
            closeWindow();
        }
    }
    
    private void displayOrderDetails() {
        // Afficher les informations de base
        customerNameLabel.setText(currentOrder.getCustomerName());
        
        // Afficher la table associée si présente
        if (currentOrder.getTableId() != null) {
            Optional<Table> tableOpt = tableService.getTableById(currentOrder.getTableId());
            tableOpt.ifPresent(table -> tableLabel.setText(table.getNumero()));
        } else {
            tableLabel.setText("Aucune");
        }
        
        // Statut avec coloration
        statusLabel.setText(currentOrder.getStatus());
        switch (currentOrder.getStatus()) {
            case "EN_ATTENTE":
                statusLabel.setStyle("-fx-text-fill: #f39c12;"); // Orange
                markCompletedBtn.setDisable(false);
                break;
            case "PREPAREE":
                statusLabel.setStyle("-fx-text-fill: #2ecc71;"); // Vert
                markCompletedBtn.setDisable(true);
                break;
            case "ANNULEE":
                statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Rouge
                markCompletedBtn.setDisable(true);
                break;
        }
        
        // Heure de commande formatée
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        orderTimeLabel.setText(currentOrder.getOrderTime().format(formatter));
        
        // Total avec symbole euro
        totalPriceLabel.setText(currentOrder.getTotalPrice() + " €");
        
        // Notes
        notesTextArea.setText(currentOrder.getNotes());
        
        // Items de la commande
        orderItems.clear();
        orderItems.addAll(currentOrder.getItems());
    }
    
    @FXML
    public void handleMarkCompleted() {
        if (currentOrder == null) return;
        
        boolean success = orderService.markOrderAsCompleted(currentOrder.getId());
        
        if (success) {
            statusLabel.setText("PREPAREE");
            statusLabel.setStyle("-fx-text-fill: #2ecc71;"); // Vert
            markCompletedBtn.setDisable(true);
            
            showAlert(Alert.AlertType.INFORMATION, "Statut mis à jour", 
                     "La commande a été marquée comme préparée");
            
            // Récharger la commande pour avoir les détails à jour
            loadOrder(currentOrder.getId());
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Impossible de mettre à jour le statut de la commande");
        }
    }
    
    @FXML
    public void handleClose() {
        closeWindow();
    }
    
    private void closeWindow() {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }
}
