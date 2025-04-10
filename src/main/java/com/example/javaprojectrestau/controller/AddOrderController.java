package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Dish;
import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.model.OrderItem;
import com.example.javaprojectrestau.model.Table;
import com.example.javaprojectrestau.service.DishService;
import com.example.javaprojectrestau.service.OrderService;
import com.example.javaprojectrestau.service.TableService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AddOrderController implements Initializable {

    @FXML private TextField customerNameField;
    @FXML private TextArea notesTextArea;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ListView<Dish> dishesListView;
    @FXML private Spinner<Integer> quantitySpinner;
    
    @FXML private TableView<OrderItem> orderItemsTableView;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, String> dishNameColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> priceColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> totalColumn;
    
    @FXML private Label totalPriceLabel;
    @FXML private Label tableInfoLabel; // Nouveau label pour afficher la table
    
    private final DishService dishService = new DishService();
    private final OrderService orderService = new OrderService();
    private final TableService tableService = new TableService();
    
    private ObservableList<Dish> dishes = FXCollections.observableArrayList();
    private ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    
    private Order currentOrder = new Order();
    private Runnable onOrderAddedCallback;
    private Long tableId; // ID de la table associée à la commande
    private Table selectedTable; // Table associée à la commande
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les catégories
        categoryFilterComboBox.getItems().addAll("Toutes", "Entrée", "Plat principal", "Dessert", "Boisson");
        categoryFilterComboBox.setValue("Toutes");
        
        // Écouter les changements de catégorie
        categoryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                refreshDishes(newVal);
            }
        });
        
        // Configurer la liste des plats
        dishesListView.setItems(dishes);
        
        // Configurer le tableau des items de commande
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dishNameColumn.setCellValueFactory(new PropertyValueFactory<>("dishName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(cellData -> {
            OrderItem item = cellData.getValue();
            BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new SimpleObjectProperty<>(total);
        });
        
        orderItemsTableView.setItems(orderItems);
        
        // Mettre à jour le total quand les items changent
        orderItems.addListener((javafx.collections.ListChangeListener.Change<? extends OrderItem> c) -> {
            updateTotalPrice();
        });
        
        // Charger les plats
        refreshDishes("Toutes");
        
        // Initialiser le total
        updateTotalPrice();
        
        // Appliquer le thème sombre à cette fenêtre si ce n'est pas déjà fait par le parent
        Scene currentScene = customerNameField.getScene();
        if (currentScene != null && !currentScene.getStylesheets().contains(
                getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm())) {
            currentScene.getStylesheets().add(
                getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
            );
        }
    }
    
    private void refreshDishes(String category) {
        dishes.clear();
        List<Dish> dishList;
        
        if ("Toutes".equals(category)) {
            dishList = dishService.getAllDishes();
        } else {
            dishList = dishService.getDishesByCategory(category);
        }
        
        dishes.addAll(dishList);
    }
    
    private void updateTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItem item : orderItems) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        totalPriceLabel.setText(total + " €");
    }
    
    @FXML
    public void handleAddToOrder() {
        Dish selectedDish = dishesListView.getSelectionModel().getSelectedItem();
        
        if (selectedDish == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                     "Veuillez sélectionner un plat à ajouter à la commande.");
            return;
        }
        
        int quantity = quantitySpinner.getValue();
        
        // Vérifier si le plat est déjà dans la commande
        for (OrderItem item : orderItems) {
            if (item.getDishId().equals(selectedDish.getId())) {
                // Mettre à jour la quantité
                item.setQuantity(item.getQuantity() + quantity);
                orderItemsTableView.refresh();
                updateTotalPrice();
                return;
            }
        }
        
        // Sinon, ajouter un nouveau item
        OrderItem item = new OrderItem(
            null, null, selectedDish.getId(), selectedDish.getName(), 
            quantity, selectedDish.getPrice()
        );
        
        orderItems.add(item);
    }
    
    @FXML
    public void handleRemoveItem() {
        OrderItem selectedItem = orderItemsTableView.getSelectionModel().getSelectedItem();
        
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                     "Veuillez sélectionner un élément à supprimer.");
            return;
        }
        
        orderItems.remove(selectedItem);
    }
    
    @FXML
    public void handleSaveOrder() {
        String customerName = customerNameField.getText().trim();
        
        if (customerName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Données manquantes", 
                     "Veuillez saisir le nom du client.");
            return;
        }
        
        if (orderItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Commande vide", 
                     "Veuillez ajouter au moins un plat à la commande.");
            return;
        }
        
        // Préparer la commande
        currentOrder.setCustomerName(customerName);
        currentOrder.setNotes(notesTextArea.getText());
        
        // Associer la table à la commande si spécifiée
        if (tableId != null) {
            currentOrder.setTableId(tableId);
        }
        
        // Ajouter les items à la commande
        currentOrder.getItems().clear();
        currentOrder.getItems().addAll(orderItems);
        
        // Sauvegarder la commande
        Order savedOrder = orderService.saveOrder(currentOrder);
        
        if (savedOrder.getId() != null) {
            showAlert(Alert.AlertType.INFORMATION, "Commande enregistrée", 
                     "La commande a été enregistrée avec succès.");
            
            // Appeler le callback si défini
            if (onOrderAddedCallback != null) {
                onOrderAddedCallback.run();
            }
            
            // Fermer la fenêtre
            ((Stage) customerNameField.getScene().getWindow()).close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Impossible d'enregistrer la commande.");
        }
    }
    
    @FXML
    public void handleCancel() {
        // Fermer la fenêtre sans sauvegarder
        ((Stage) customerNameField.getScene().getWindow()).close();
    }
    
    public void setOnOrderAddedCallback(Runnable callback) {
        this.onOrderAddedCallback = callback;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Appliquer le thème sombre au dialogue
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }
    
    // Setter pour l'ID de la table
    public void setTableId(Long tableId) {
        this.tableId = tableId;
        
        // Si une table est associée, ajouter un label pour indiquer la table sélectionnée
        if (tableId != null) {
            // Chargement des informations de la table
            Optional<Table> tableOpt = tableService.getTableById(tableId);
            if (tableOpt.isPresent()) {
                selectedTable = tableOpt.get();
                
                // Afficher les informations de la table
                if (tableInfoLabel == null) {
                    // Si le label n'existe pas, nous devons le créer et l'ajouter à la vue
                    tableInfoLabel = new Label("Table sélectionnée: " + selectedTable.getNumero() + 
                                              " (" + selectedTable.getCapacite() + " pers.)");
                    tableInfoLabel.getStyleClass().add("table-info-label");
                    tableInfoLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #2c3e50; -fx-padding: 5px;");
                    
                    // Insérer le label après le label du client
                    VBox parent = (VBox) customerNameField.getParent().getParent();
                    if (parent != null) {
                        // Chercher où insérer le label (après le GridPane des informations client)
                        int gridPaneIndex = -1;
                        for (int i = 0; i < parent.getChildren().size(); i++) {
                            if (parent.getChildren().get(i) instanceof GridPane) {
                                gridPaneIndex = i;
                                break;
                            }
                        }
                        
                        if (gridPaneIndex >= 0) {
                            parent.getChildren().add(gridPaneIndex + 1, tableInfoLabel);
                        }
                    }
                } else {
                    tableInfoLabel.setText("Table sélectionnée: " + selectedTable.getNumero() + 
                                          " (" + selectedTable.getCapacite() + " pers.)");
                }
            }
        }
    }
}
