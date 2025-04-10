package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Dish;
import com.example.javaprojectrestau.service.DishService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MenuGalleryController implements Initializable {

    @FXML private TilePane dishTilePane;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField searchField;

    private final DishService dishService = new DishService();
    private List<Dish> allDishes;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser le TilePane
        dishTilePane.setPrefColumns(3);
        
        // Charger toutes les catégories
        allDishes = dishService.getAllDishes();
        List<String> categories = allDishes.stream()
                .map(Dish::getCategory)
                .distinct()
                .collect(Collectors.toList());
        
        // Ajouter l'option "Toutes" en premier
        categoryComboBox.getItems().add("Toutes");
        categoryComboBox.getItems().addAll(categories);
        categoryComboBox.setValue("Toutes");
        
        // Ajouter des écouteurs pour les filtres
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterDishes();
            }
        });
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterDishes();
        });
        
        // Afficher tous les plats
        displayDishes(allDishes);
    }

    private void filterDishes() {
        String category = categoryComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();
        
        List<Dish> filteredDishes = allDishes.stream()
                .filter(dish -> "Toutes".equals(category) || category.equals(dish.getCategory()))
                .filter(dish -> searchText.isEmpty() || 
                        dish.getName().toLowerCase().contains(searchText) ||
                        (dish.getDescription() != null && dish.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
        
        displayDishes(filteredDishes);
    }

    private void displayDishes(List<Dish> dishes) {
        dishTilePane.getChildren().clear();
        
        for (Dish dish : dishes) {
            VBox dishCard = createDishCard(dish);
            dishTilePane.getChildren().add(dishCard);
        }
    }

    private VBox createDishCard(Dish dish) {
        // Créer la carte de plat
        VBox card = new VBox();
        card.setPrefWidth(250);
        card.setPrefHeight(350);
        card.setAlignment(Pos.TOP_CENTER);
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("dish-card"); // Utiliser la classe CSS du thème sombre
        
        // Ajouter un effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(3.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        card.setEffect(shadow);
        
        // Créer et configurer l'ImageView pour le plat
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        
        // Chargement de l'image depuis la base de données
        loadDishImage(dish, imageView);
        
        // Créer le label pour le nom du plat
        Label nameLabel = new Label(dish.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setWrapText(true);
        nameLabel.setPrefWidth(220);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.getStyleClass().add("dish-name"); // Ajouter classe CSS
        
        // Créer un conteneur pour la catégorie avec un style distinctif
        Label categoryLabel = new Label(dish.getCategory());
        categoryLabel.setPadding(new Insets(5, 10, 5, 10));
        categoryLabel.getStyleClass().add("dish-category"); // Utiliser classe CSS
        categoryLabel.setStyle("-fx-background-radius: 15;");
        
        // Créer le label pour le prix
        Label priceLabel = new Label(dish.getPrice() + " €");
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        priceLabel.getStyleClass().add("dish-price"); // Utiliser classe CSS
        
        // Créer la zone de texte pour la description (limitée)
        String shortDesc = dish.getDescription();
        if (shortDesc != null && shortDesc.length() > 100) {
            shortDesc = shortDesc.substring(0, 100) + "...";
        }
        
        TextArea descArea = new TextArea(shortDesc);
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefHeight(80);
        descArea.setPrefWidth(220);
        descArea.getStyleClass().add("dish-description"); // Utiliser classe CSS
        
        // Ajouter tous les composants à la carte
        card.getChildren().addAll(imageView, nameLabel, categoryLabel, priceLabel, descArea);
        
        // Ajouter des effets hover avec les classes CSS du thème sombre
        card.setOnMouseEntered(e -> {
            card.getStyleClass().add("dish-card-hover");
        });
        
        card.setOnMouseExited(e -> {
            card.getStyleClass().remove("dish-card-hover");
        });
        
        return card;
    }

    private void loadDishImage(Dish dish, ImageView imageView) {
        try {
            if (dish.getId() != null) {
                Blob imageBlob = DatabaseConnection.getImage(dish.getId());
                if (imageBlob != null) {
                    try (InputStream is = imageBlob.getBinaryStream()) {
                        Image image = new Image(is);
                        imageView.setImage(image);
                    } catch (IOException e) {
                        setDefaultImage(imageView);
                    }
                } else {
                    setDefaultImage(imageView);
                }
            } else {
                setDefaultImage(imageView);
            }
        } catch (SQLException e) {
            setDefaultImage(imageView);
        }
    }

    private void setDefaultImage(ImageView imageView) {
        // Créer un fond de couleur par défaut
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setPrefSize(220, 150);
        imagePlaceholder.getStyleClass().add("image-placeholder"); // Utiliser classe CSS
        
        Label placeholderText = new Label("Image non disponible");
        placeholderText.getStyleClass().add("placeholder-text"); // Utiliser classe CSS
        
        imagePlaceholder.getChildren().add(placeholderText);
        
        // Créer un visuel de l'image par défaut
        BackgroundFill backgroundFill = new BackgroundFill(Color.rgb(60, 60, 60), new CornerRadii(5), Insets.EMPTY); // Couleur plus sombre
        Background background = new Background(backgroundFill);
        Pane pane = new Pane();
        pane.setPrefSize(220, 150);
        pane.setBackground(background);
        
        imageView.setImage(null);
    }
}
