package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Expense;
import com.example.javaprojectrestau.service.FinanceService;
import com.example.javaprojectrestau.util.DialogStyler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class AddExpenseController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryComboBox;
    
    private final FinanceService financeService = new FinanceService();
    private Runnable onExpenseAddedCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les catégories prédéfinies
        categoryComboBox.getItems().addAll(
                "Approvisionnement", 
                "Équipement", 
                "Salaires", 
                "Loyer", 
                "Charges", 
                "Publicité", 
                "Maintenance",
                "Autre"
        );
        
        // Date par défaut aujourd'hui
        datePicker.setValue(LocalDate.now());
        
        // Vérifier que c'est bien un nombre pour le montant
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.,]\\d*)?")) {
                amountField.setText(oldValue);
            }
        });
    }

    @FXML
    public void handleAddExpense() {
        try {
            // Validation des champs obligatoires
            String name = nameField.getText().trim();
            String amountText = amountField.getText().trim();
            LocalDate date = datePicker.getValue();
            
            if (name.isEmpty()) {
                DialogStyler.showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le nom de la dépense est obligatoire.");
                return;
            }
            
            if (amountText.isEmpty()) {
                DialogStyler.showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le montant de la dépense est obligatoire.");
                return;
            }
            
            if (date == null) {
                DialogStyler.showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La date de la dépense est obligatoire.");
                return;
            }

            // Convertir le montant en BigDecimal
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountText.replace(",", "."));
            } catch (NumberFormatException e) {
                DialogStyler.showAlert(Alert.AlertType.ERROR, "Erreur de format", "Le montant doit être un nombre valide.");
                return;
            }
            
            // Créer une nouvelle dépense
            Expense newExpense = new Expense();
            newExpense.setName(name);
            newExpense.setAmount(amount);
            newExpense.setDate(date.atTime(LocalTime.now()));
            newExpense.setCategory(categoryComboBox.getValue());
            
            // Sauvegarder la dépense
            Expense savedExpense = financeService.saveExpense(newExpense);
            
            if (savedExpense.getId() != null) {
                DialogStyler.showAlert(Alert.AlertType.INFORMATION, "Dépense ajoutée", 
                        "La dépense a été ajoutée avec succès.");
                
                // Appeler le callback si défini
                if (onExpenseAddedCallback != null) {
                    onExpenseAddedCallback.run();
                }
                
                // Fermer la fenêtre
                ((Stage) nameField.getScene().getWindow()).close();
            } else {
                DialogStyler.showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Impossible d'ajouter la dépense.");
            }

        } catch (Exception e) {
            DialogStyler.showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        // Fermer la fenêtre sans sauvegarder
        ((Stage) nameField.getScene().getWindow()).close();
    }
    
    // Définir un callback à exécuter quand une dépense est ajoutée
    public void setOnExpenseAddedCallback(Runnable callback) {
        this.onExpenseAddedCallback = callback;
    }
}
