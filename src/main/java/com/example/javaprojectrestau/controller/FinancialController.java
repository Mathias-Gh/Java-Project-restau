package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Expense;
import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.service.FinanceService;
import com.example.javaprojectrestau.util.DialogStyler;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class FinancialController implements Initializable {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    @FXML private Label revenueLabel;
    @FXML private Label expensesLabel;
    @FXML private Label profitLabel;
    
    @FXML private TableView<Map.Entry<String, BigDecimal>> categoryTableView;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> categoryNameColumn;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> categoryTotalColumn;
    
    @FXML private TableView<Expense> expensesTableView;
    @FXML private TableColumn<Expense, Long> expenseIdColumn;
    @FXML private TableColumn<Expense, String> expenseNameColumn;
    @FXML private TableColumn<Expense, BigDecimal> expenseAmountColumn;
    @FXML private TableColumn<Expense, String> expenseDateColumn;
    @FXML private TableColumn<Expense, String> expenseCategoryColumn;
    
    @FXML private TableView<Order> revenueTableView;
    @FXML private TableColumn<Order, Long> orderIdColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, BigDecimal> orderAmountColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    
    @FXML private Button deleteButton;
    
    private final FinanceService financeService = new FinanceService();
    private final ObservableList<Expense> expensesList = FXCollections.observableArrayList();
    private final ObservableList<Map.Entry<String, BigDecimal>> categoriesList = FXCollections.observableArrayList();
    private final ObservableList<Order> revenuesList = FXCollections.observableArrayList();
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les dates par défaut (dernier mois)
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.minusMonths(1));
        endDatePicker.setValue(today);
        
        // Configurer les colonnes pour les dépenses
        expenseIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        expenseNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        expenseAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        expenseDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getDate().format(formatter));
        });
        expenseCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Configurer les colonnes pour les catégories
        categoryNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        categoryTotalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue() + " €"));
        
        // Configurer les colonnes pour les recettes
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getOrderTime().format(formatter));
        });
        orderAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Ajouter un listener pour la sélection d'une dépense
        expensesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteButton.setDisable(newVal == null);
        });
        
        // Lier les données aux tableaux
        expensesTableView.setItems(expensesList);
        categoryTableView.setItems(categoriesList);
        revenueTableView.setItems(revenuesList);
        
        // Charger les données initiales
        updateDateRange();
    }
    
    @FXML
    public void handleFilterByDate() {
        updateDateRange();
    }
    
    @FXML
    public void handleGeneratePdf() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport financier");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("rapport_financier_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            
            File selectedFile = fileChooser.showSaveDialog(startDatePicker.getScene().getWindow());
            
            if (selectedFile != null) {
                boolean success = financeService.generateFinancialReport(
                        selectedFile.getAbsolutePath(), startDate, endDate);
                
                if (success) {
                    DialogStyler.showAlert(Alert.AlertType.INFORMATION, 
                                           "Rapport généré", 
                                           "Le rapport financier a été généré avec succès.");
                } else {
                    DialogStyler.showAlert(Alert.AlertType.ERROR, 
                                           "Erreur", 
                                           "Impossible de générer le rapport financier.");
                }
            }
        } catch (Exception e) {
            DialogStyler.showAlert(Alert.AlertType.ERROR, 
                                   "Erreur", 
                                   "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleAddExpense() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/add-expense-view.fxml"));
            Parent root = loader.load();
            
            AddExpenseController controller = loader.getController();
            controller.setOnExpenseAddedCallback(this::refreshData);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
            );
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une dépense");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.showAndWait();
            
        } catch (Exception e) {
            DialogStyler.showAlert(Alert.AlertType.ERROR, 
                                  "Erreur", 
                                  "Impossible d'ouvrir la fenêtre d'ajout de dépense: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleDeleteExpense() {
        Expense selectedExpense = expensesTableView.getSelectionModel().getSelectedItem();
        
        if (selectedExpense == null) {
            DialogStyler.showAlert(Alert.AlertType.WARNING, 
                                  "Aucune sélection", 
                                  "Veuillez sélectionner une dépense à supprimer.");
            return;
        }
        
        boolean confirmed = DialogStyler.showConfirmation(
                "Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer cette dépense ?",
                "Dépense: " + selectedExpense.getName() + " (" + selectedExpense.getAmount() + " €)"
        );
        
        if (confirmed) {
            boolean success = financeService.deleteExpense(selectedExpense.getId());
            
            if (success) {
                DialogStyler.showAlert(Alert.AlertType.INFORMATION, 
                                      "Suppression réussie", 
                                      "La dépense a été supprimée avec succès.");
                refreshData();
            } else {
                DialogStyler.showAlert(Alert.AlertType.ERROR, 
                                      "Erreur", 
                                      "Impossible de supprimer la dépense.");
            }
        }
    }
    
    private void updateDateRange() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        
        if (start == null || end == null) {
            DialogStyler.showAlert(Alert.AlertType.WARNING, 
                                  "Dates manquantes", 
                                  "Veuillez sélectionner une date de début et une date de fin.");
            return;
        }
        
        if (start.isAfter(end)) {
            DialogStyler.showAlert(Alert.AlertType.WARNING, 
                                  "Dates invalides", 
                                  "La date de début doit être antérieure à la date de fin.");
            return;
        }
        
        // Convertir les LocalDate en LocalDateTime
        startDate = start.atStartOfDay();
        endDate = end.atTime(LocalTime.MAX);
        
        refreshData();
    }
    
    private void refreshData() {
        // Récupérer les dépenses et les commandes terminées pour la période
        List<Expense> expenses = financeService.getExpensesByDateRange(startDate, endDate);
        List<Order> completedOrders = financeService.getCompletedOrdersByDateRange(startDate, endDate);
        
        // Calculer les totaux
        BigDecimal totalRevenue = financeService.calculateTotalRevenue(completedOrders);
        BigDecimal totalExpenses = financeService.calculateTotalExpenses(expenses);
        BigDecimal profit = financeService.calculateProfit(totalRevenue, totalExpenses);
        
        // Mettre à jour les labels
        revenueLabel.setText(totalRevenue + " €");
        expensesLabel.setText(totalExpenses + " €");
        profitLabel.setText(profit + " €");
        
        // Appliquer un style au profit selon s'il est positif ou négatif
        if (profit.compareTo(BigDecimal.ZERO) >= 0) {
            profitLabel.setStyle("-fx-text-fill: #2ecc71;"); // Vert pour positif
        } else {
            profitLabel.setStyle("-fx-text-fill: #e74c3c;"); // Rouge pour négatif
        }
        
        // Mettre à jour la table des dépenses
        expensesList.clear();
        expensesList.addAll(expenses);
        
        // Mettre à jour la table des recettes
        revenuesList.clear();
        revenuesList.addAll(completedOrders);
        
        // Regrouper les dépenses par catégorie
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        
        for (Expense expense : expenses) {
            String category = expense.getCategory() != null ? expense.getCategory() : "Non catégorisé";
            BigDecimal currentTotal = expensesByCategory.getOrDefault(category, BigDecimal.ZERO);
            expensesByCategory.put(category, currentTotal.add(expense.getAmount()));
        }
        
        // Mettre à jour la table des catégories
        categoriesList.clear();
        categoriesList.addAll(expensesByCategory.entrySet());
    }
}
