package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Dish;
import com.example.javaprojectrestau.service.DishService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DishController implements Initializable {

    private final DishService dishService = new DishService();
    
    // Champs existants pour les tables et les colonnes
    @FXML private TableView<Dish> dishTable;
    @FXML private TableColumn<Dish, Long> idColumn;
    @FXML private TableColumn<Dish, String> nameColumn;
    @FXML private TableColumn<Dish, BigDecimal> priceColumn;
    @FXML private TableColumn<Dish, String> categoryColumn;
    
    // Champs pour les formulaires
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    
    // Nouveaux champs pour les images et la liste
    @FXML private ListView<Dish> dishListView;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView dishImageView;
    @FXML private ImageView dishDetailsImageView;
    @FXML private Label dishDetailsName;
    @FXML private Label dishDetailsPrice;
    @FXML private TextArea dishDetailsDescription;
    @FXML private Button deleteButton;
    
    private Dish selectedDish;
    private ObservableList<Dish> dishList = FXCollections.observableArrayList();
    private File selectedImageFile;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration existante
        if (dishTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            
            dishTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedDish = newSelection;
                    showDishDetails(selectedDish);
                }
            });
        }
        
        // Configuration pour la ListView
        if (dishListView != null) {
            dishListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedDish = newSelection;
                    showDishDetails(selectedDish);
                    // Activer le bouton de suppression
                    if (deleteButton != null) {
                        deleteButton.setDisable(false);
                    }
                } else {
                    // Désactiver le bouton de suppression si aucun plat n'est sélectionné
                    if (deleteButton != null) {
                        deleteButton.setDisable(true);
                    }
                }
            });
        }
        
        // Initialiser les catégories
        if (categoryComboBox != null) {
            categoryComboBox.getItems().addAll("Entrée", "Plat principal", "Dessert", "Boisson");
        }
        
        // Charger tous les plats
        refreshDishList();
    }
    
    private void refreshDishList() {
        dishList.clear();
        dishList.addAll(dishService.getAllDishes());
        
        if (dishTable != null) {
            dishTable.setItems(dishList);
        }
        
        if (dishListView != null) {
            dishListView.setItems(dishList);
        }
    }
    
    private void showDishDetails(Dish dish) {
        if (nameField != null) {
            nameField.setText(dish.getName());
        }
        
        if (priceField != null) {
            priceField.setText(dish.getPrice().toString());
        }
        
        if (descriptionField != null) {
            descriptionField.setText(dish.getDescription());
        }
        
        if (descriptionArea != null) {
            descriptionArea.setText(dish.getDescription());
        }
        
        if (categoryComboBox != null) {
            categoryComboBox.setValue(dish.getCategory());
        }
        
        // Afficher les détails dans les champs de détails
        if (dishDetailsName != null) {
            dishDetailsName.setText(dish.getName());
        }
        
        if (dishDetailsPrice != null) {
            dishDetailsPrice.setText(dish.getPrice() + " €");
        }
        
        if (dishDetailsDescription != null) {
            dishDetailsDescription.setText(dish.getDescription());
        }
        
        // Charger l'image du plat depuis la base de données
        if (dishDetailsImageView != null && dish.getId() != null) {
            try {
                java.sql.Blob imageBlob = com.example.javaprojectrestau.db.DatabaseConnection.getImage(dish.getId());
                if (imageBlob != null) {
                    try (java.io.InputStream is = imageBlob.getBinaryStream()) {
                        Image image = new Image(is);
                        dishDetailsImageView.setImage(image);
                    } catch (java.io.IOException e) {
                        System.err.println("Erreur lors de la lecture de l'image: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    // Pas d'image trouvée
                    dishDetailsImageView.setImage(null);
                }
            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de la récupération de l'image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleSaveDish() {
        try {
            String name = nameField.getText().trim();
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            String description = descriptionField != null ? 
                                descriptionField.getText().trim() : 
                                (descriptionArea != null ? descriptionArea.getText().trim() : "");
            
            // Vérifier si categoryComboBox est null et fournir une valeur par défaut
            String category = "Non catégorisé";
            if (categoryComboBox != null && categoryComboBox.getValue() != null) {
                category = categoryComboBox.getValue();
            }
            
            if (name.isEmpty()) {
                showAlert("Validation Error", "Le nom est obligatoire.");
                return;
            }
            
            Dish dish;
            if (selectedDish != null) {
                // Mise à jour du plat existant
                dish = selectedDish;
                dish.setName(name);
                dish.setPrice(price);
                dish.setDescription(description);
                dish.setCategory(category);
            } else {
                // Nouveau plat
                dish = new Dish(null, name, price, description, category);
            }
            
            dishService.saveDish(dish);
            clearFields();
            selectedDish = null;
            refreshDishList();
            
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Le prix doit être un nombre valide.");
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Méthode manquante pour gérer la sélection d'image
    @FXML
    public void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(dishImageView.getScene().getWindow());
        
        if (selectedImageFile != null) {
            Image image = new Image(selectedImageFile.toURI().toString());
            dishImageView.setImage(image);
        }
    }
    
    // Modifier la méthode pour ouvrir une nouvelle fenêtre d'ajout de plat
    @FXML
    public void handleAddDish() {
        try {
            // Charger la vue pour ajouter un plat
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaprojectrestau/add-dish-view.fxml"));
            Parent root = loader.load();
            
            // Obtenir le contrôleur
            AddDishController controller = loader.getController();
            
            // Définir le callback à exécuter après l'ajout d'un plat
            controller.setOnDishAddedCallback(this::refreshDishList);
            
            // Créer une nouvelle scène et fenêtre
            Scene scene = new Scene(root);
            // Appliquer le style sombre à la fenêtre modale
            scene.getStylesheets().add(
                getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
            );
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau plat");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Empêcher l'interaction avec la fenêtre principale
            stage.initStyle(StageStyle.UTILITY); // Style plus compacte pour une fenêtre de dialogue
            
            // Afficher la fenêtre
            stage.showAndWait();
            
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'ouverture de la fenêtre d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleNewDish() {
        clearFields();
        selectedDish = null;
    }
    
    @FXML
    public void handleDeleteDish() {
        if (selectedDish != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmer la suppression");
            confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer ce plat ?");
            confirmation.setContentText("Le plat \"" + selectedDish.getName() + "\" sera définitivement supprimé.");
            
            // Style pour le dialogue de suppression
            DialogPane dialogPane = confirmation.getDialogPane();
            dialogPane.getStylesheets().add(
                getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
            );
            dialogPane.getStyleClass().add("dialog-pane");
            dialogPane.setStyle("-fx-background-color: #1e1e1e;"); // Fond légèrement rouge
            
            // Personnaliser les boutons
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.getStyleClass().add("button-danger");
            okButton.setText("Supprimer");
            
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            cancelButton.setText("Annuler");
            
            // Ajouter une icône d'avertissement
            dialogPane.getStyleClass().add("alert");
            dialogPane.getStyleClass().add("warning");
            
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = dishService.deleteDish(selectedDish.getId());
                    
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Suppression réussie", 
                                "Le plat a été supprimé avec succès.");
                                
                        // Réinitialiser les détails et désactiver le bouton
                        if (dishDetailsName != null) dishDetailsName.setText("");
                        if (dishDetailsPrice != null) dishDetailsPrice.setText("");
                        if (dishDetailsDescription != null) dishDetailsDescription.setText("");
                        if (dishDetailsImageView != null) dishDetailsImageView.setImage(null);
                        if (deleteButton != null) deleteButton.setDisable(true);
                        
                        selectedDish = null;
                        refreshDishList();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Impossible de supprimer le plat. Il est peut-être utilisé dans des commandes.");
                    }
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                    "Veuillez sélectionner un plat à supprimer.");
        }
    }
    
    private void clearFields() {
        if (nameField != null) nameField.clear();
        if (priceField != null) priceField.clear();
        if (descriptionField != null) descriptionField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (categoryComboBox != null) categoryComboBox.getSelectionModel().clearSelection();
        if (dishImageView != null) dishImageView.setImage(null);
        selectedImageFile = null;
    }
    
    private void showAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Appliquer le style au dialogue
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }
}
