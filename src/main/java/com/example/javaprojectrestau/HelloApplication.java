package com.example.javaprojectrestau;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.service.DishService;
import com.example.javaprojectrestau.service.OrderService;
import com.example.javaprojectrestau.service.TimerService;
import com.example.javaprojectrestau.component.TimerComponent;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class HelloApplication extends Application {
    
    private BorderPane mainLayout;
    private VBox sidebarLayout;
    private Button activeSidebarButton;
    
    // Ajouter le service de chronomètre
    private TimerComponent timerComponent;
    
    // Singleton instance pour l'accès global
    private static HelloApplication instance;

    public static TimerService timerService;
    
    @Override
    public void start(Stage stage) throws IOException {
        instance = this;
        
        // connexion à la base de données
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            showDatabaseAlert();
        }
        
        // Initialiser les services et créer les tables si nécessaire
        DishService dishService = new DishService();
        OrderService orderService = new OrderService();
        
        // Initialisation du service de chronomètre ici
        timerService = new TimerService();
        
        // On créer la mise en page principale
        mainLayout = new BorderPane();
        
        // On créer le composant de chronomètre
        timerComponent = new TimerComponent(timerService);
        
        // barre latérale (sidebar)
        createSidebar();
        
        // Charger la vue par défaut (dishes)
        loadContent("dish-view.fxml");
        
        // Créer la scène avec thème sombre
        Scene scene = new Scene(mainLayout, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("styles/dark-theme.css").toExternalForm());
        
        // Configurer la fenêtre principale
        stage.setTitle("Système de Gestion de Restaurant");
        stage.setScene(scene);
        stage.show();
    }
    
    public static HelloApplication getInstance() {
        return instance;
    }
    
    private void createSidebar() {
        sidebarLayout = new VBox();
        sidebarLayout.getStyleClass().add("sidebar");
        sidebarLayout.setPrefWidth(250);
        
        // Titre de l'appli
        Label titleLabel = new Label("Restaurant Manager");
        titleLabel.getStyleClass().add("sidebar-header");
        
        // Ajouter le composant de chronomètre
        TimerComponent timerComponent = new TimerComponent(timerService);
        
        // Séparateur
        Separator separator = new Separator();
        separator.setOpacity(0.3);
        
        // Boutons de navigation présents dans la sidebar de navigation
        Button dishesButton = createSidebarButton("Gestion des plats", "dish-view.fxml");
        Button ordersButton = createSidebarButton("Gestion des commandes", "order-view.fxml");
        Button tablesButton = createSidebarButton("Gestion des tables", "table-view.fxml"); // Nouveau bouton
        Button menuGalleryButton = createSidebarButton("Affichage du Menu", "menu-gallery-view.fxml");
        Button employeButton = createSidebarButton("Gestion des employés", "employe-view.fxml");
        Button financialButton = createSidebarButton("Rapport Financier", "financial-view.fxml");
        
        // Activer initialement le bouton des plats
        activateSidebarButton(dishesButton);
        
        // propriété canTakeOrders pour désactiver le bouton des commandes
        timerService.canTakeOrdersProperty().addListener((obs, oldVal, newVal) -> {
            ordersButton.setDisable(!newVal);
            if (!newVal) {
                ordersButton.setText("Commandes (FERMÉES)");
            } else {
                ordersButton.setText("Gestion des commandes");
            }
        });
        
        // Ajouter tous les éléments à la sidebar
        sidebarLayout.getChildren().addAll(
            titleLabel,
            timerComponent,
            separator,
            dishesButton,
            ordersButton,
            tablesButton, // Ajouter le bouton des tables
            menuGalleryButton,
            employeButton,
            financialButton
        );
        
        VBox.setVgrow(separator, Priority.ALWAYS); // Push the separator to take all available space
        
        // Ajouter la sidebar au layout principal
        mainLayout.setLeft(sidebarLayout);
    }
    
    private Button createSidebarButton(String text, String fxmlFile) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        
        button.setOnAction(e -> {
            activateSidebarButton(button);
            loadContent(fxmlFile);
        });
        
        return button;
    }
    
    private void activateSidebarButton(Button button) {
        if (activeSidebarButton != null) {
            activeSidebarButton.getStyleClass().remove("sidebar-button-active");
        }
        button.getStyleClass().add("sidebar-button-active");
        activeSidebarButton = button;
    }
    
    private void loadContent(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node content = loader.load();
            content.getStyleClass().add("content-pane");
            mainLayout.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        // Ferme proprement la connexion à la base de données
        DatabaseConnection.closeConnection();
    }

    private void showDatabaseAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Problème de connexion à la base de données");
        alert.setHeaderText("Impossible de se connecter à MySQL");
        alert.setContentText("L'application fonctionnera en mode limité sans accès à la base de données.\n\n" +
                "Vérifiez que:\n" +
                "1. MySQL est installé et en cours d'exécution\n" +
                "2. Les identifiants dans DatabaseConnection.java sont corrects");
        
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}