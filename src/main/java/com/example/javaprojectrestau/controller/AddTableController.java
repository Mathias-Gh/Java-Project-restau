package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Table;
import com.example.javaprojectrestau.service.TableService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddTableController implements Initializable {
    
    public enum Mode {
        ADD,
        EDIT
    }
    
    @FXML private Label dialogTitleLabel;
    @FXML private TextField numeroField;
    @FXML private Spinner<Integer> capaciteSpinner;
    @FXML private ComboBox<String> emplacementComboBox;
    @FXML private Button saveButton;
    
    private final TableService tableService = new TableService();
    private Mode mode = Mode.ADD;
    private Table table;
    private Runnable onTableSavedCallback;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurer le spinner de capacité
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 4);
        capaciteSpinner.setValueFactory(valueFactory);
        
        // Remplir les emplacements programmatiquement au lieu de le faire via FXML
        ObservableList<String> emplacements = FXCollections.observableArrayList(
            "Intérieur", "Terrasse", "Étage", "Fenêtre", "Bar"
        );
        emplacementComboBox.setItems(emplacements);
        emplacementComboBox.setEditable(true);
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
        
        if (mode == Mode.EDIT) {
            dialogTitleLabel.setText("Modifier une table");
            saveButton.setText("Enregistrer");
        } else {
            dialogTitleLabel.setText("Ajouter une nouvelle table");
            saveButton.setText("Ajouter");
        }
    }
    
    public void setTable(Table table) {
        this.table = table;
        
        if (table != null) {
            numeroField.setText(table.getNumero());
            capaciteSpinner.getValueFactory().setValue(table.getCapacite());
            emplacementComboBox.setValue(table.getEmplacement());
        }
    }
    
    @FXML
    public void handleSave() {
        if (!validateInputs()) {
            return;
        }
        
        String numero = numeroField.getText().trim();
        int capacite = capaciteSpinner.getValue();
        String emplacement = emplacementComboBox.getValue();
        
        if (mode == Mode.ADD) {
            table = new Table(null, numero, capacite, emplacement);
        } else {
            table.setNumero(numero);
            table.setCapacite(capacite);
            table.setEmplacement(emplacement);
        }
        
        Table savedTable = tableService.saveTable(table);
        
        if (savedTable.getId() != null) {
            if (onTableSavedCallback != null) {
                onTableSavedCallback.run();
            }
            
            // Fermer la fenêtre
            ((Stage) numeroField.getScene().getWindow()).close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer la table.");
        }
    }
    
    @FXML
    public void handleCancel() {
        // Fermer la fenêtre sans sauvegarder
        ((Stage) numeroField.getScene().getWindow()).close();
    }
    
    private boolean validateInputs() {
        String numero = numeroField.getText().trim();
        if (numero.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le numéro de la table est obligatoire.");
            return false;
        }
        
        String emplacement = emplacementComboBox.getValue();
        if (emplacement == null || emplacement.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "L'emplacement est obligatoire.");
            return false;
        }
        
        return true;
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
    
    public void setOnTableSavedCallback(Runnable callback) {
        this.onTableSavedCallback = callback;
    }
}
