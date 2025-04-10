package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.model.OrderItem;
import com.example.javaprojectrestau.service.OrderService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

    @FXML private ListView<Order> pendingOrdersListView;
    @FXML private ListView<Order> completedOrdersListView;
    
    @FXML private Label customerNameLabel;
    @FXML private Label statusLabel;
    @FXML private Label orderTimeLabel;
    @FXML private Label totalPriceLabel;
    @FXML private TextArea notesTextArea;
    
    @FXML private TableView<OrderItem> orderItemsTableView;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, String> dishNameColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> priceColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> totalColumn;
    
    @FXML private Button completeButton;
    @FXML private Button cancelButton;
    
    private final OrderService orderService = new OrderService();
    private Order selectedOrder;
    
    private ObservableList<Order> pendingOrders = FXCollections.observableArrayList();
    private ObservableList<Order> completedOrders = FXCollections.observableArrayList();
    private ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des listes
        pendingOrdersListView.setItems(pendingOrders);
        completedOrdersListView.setItems(completedOrders);
        
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
        
        // Écouter les sélections dans les listes
        pendingOrdersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedOrder = newVal;
                completedOrdersListView.getSelectionModel().clearSelection();
                showOrderDetails(selectedOrder);
            }
        });
        
        completedOrdersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedOrder = newVal;
                pendingOrdersListView.getSelectionModel().clearSelection();
                showOrderDetails(selectedOrder);
            }
        });
        
        // Désactiver initialement les boutons d'action
        completeButton.setDisable(true);
        cancelButton.setDisable(true);
        
        // Charger les commandes
        refreshOrders();
    }
    
    private void showOrderDetails(Order order) {
        if (order == null) {
            clearOrderDetails();
            return;
        }
        
        customerNameLabel.setText(order.getCustomerName());
        statusLabel.setText(order.getStatus());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        orderTimeLabel.setText(order.getOrderTime().format(formatter));
        
        totalPriceLabel.setText(order.getTotalPrice() + " €");
        notesTextArea.setText(order.getNotes());
        
        // Mettre à jour les items de la commande
        orderItems.clear();
        orderItems.addAll(order.getItems());
        
        // Activer/désactiver les boutons selon le statut
        boolean isPending = "EN_ATTENTE".equals(order.getStatus());
        completeButton.setDisable(!isPending);
        cancelButton.setDisable(!isPending);
    }
    
    private void clearOrderDetails() {
        customerNameLabel.setText("");
        statusLabel.setText("");
        orderTimeLabel.setText("");
        totalPriceLabel.setText("");
        notesTextArea.setText("");
        orderItems.clear();
        
        completeButton.setDisable(true);
        cancelButton.setDisable(true);
    }
    
    @FXML
    public void handleNewOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/add-order-view.fxml"));
            Parent root = loader.load();
            
            AddOrderController controller = loader.getController();
            controller.setOnOrderAddedCallback(this::refreshOrders);
            
            // Appliquer le style sombre à la fenêtre de dialogue
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
            );
            
            Stage stage = new Stage();
            stage.setTitle("Nouvelle commande");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Impossible d'ouvrir la fenêtre de nouvelle commande: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleCompleteOrder() {
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                     "Veuillez sélectionner une commande à marquer comme préparée.");
            return;
        }
        
        if (!"EN_ATTENTE".equals(selectedOrder.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Statut incorrect", 
                     "Seules les commandes en attente peuvent être marquées comme préparées.");
            return;
        }
        
        boolean success = orderService.markOrderAsCompleted(selectedOrder.getId());
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Commande préparée", 
                     "La commande a été marquée comme préparée.");
            refreshOrders();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Impossible de mettre à jour le statut de la commande.");
        }
    }
    
    @FXML
    public void handleCancelOrder() {
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                     "Veuillez sélectionner une commande à annuler.");
            return;
        }
        
        if (!"EN_ATTENTE".equals(selectedOrder.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Statut incorrect", 
                     "Seules les commandes en attente peuvent être annulées.");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer l'annulation");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir annuler cette commande ?");
        confirmation.setContentText("Cette action ne peut pas être annulée.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = orderService.cancelOrder(selectedOrder.getId());
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Commande annulée", 
                             "La commande a été annulée avec succès.");
                    refreshOrders();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                             "Impossible d'annuler la commande.");
                }
            }
        });
    }
    
    public void refreshOrders() {
        // Sauvegarder les sélections actuelles
        Order selectedPendingOrder = pendingOrdersListView.getSelectionModel().getSelectedItem();
        Order selectedCompletedOrder = completedOrdersListView.getSelectionModel().getSelectedItem();
        
        // Rafraîchir les listes
        pendingOrders.clear();
        pendingOrders.addAll(orderService.getPendingOrders());
        
        completedOrders.clear();
        completedOrders.addAll(orderService.getCompletedOrders());
        
        // Restaurer les sélections si possible
        if (selectedPendingOrder != null) {
            for (Order order : pendingOrders) {
                if (order.getId().equals(selectedPendingOrder.getId())) {
                    pendingOrdersListView.getSelectionModel().select(order);
                    break;
                }
            }
        }
        
        if (selectedCompletedOrder != null) {
            for (Order order : completedOrders) {
                if (order.getId().equals(selectedCompletedOrder.getId())) {
                    completedOrdersListView.getSelectionModel().select(order);
                    break;
                }
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Appliquer le thème sombre au dialogue d'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }
}
