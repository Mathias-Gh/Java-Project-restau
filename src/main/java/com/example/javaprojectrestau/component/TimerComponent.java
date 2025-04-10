package com.example.javaprojectrestau.component;

import com.example.javaprojectrestau.service.TimerService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Composant d'interface affichant le chronomètre de service
 */
public class TimerComponent extends VBox {
    
    private final TimerService timerService;
    private final Label timerLabel;
    private final Label statusLabel;
    
    public TimerComponent(TimerService timerService) {
        super(10); // Espacement vertical de 10px
        this.timerService = timerService;
        
        // Configuration du composant
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);
        getStyleClass().add("timer-component");
        
        // Titre du composant
        Label titleLabel = new Label("Temps de Service");
        titleLabel.getStyleClass().add("timer-title");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Affichage du temps
        timerLabel = new Label(timerService.getTimeDisplay());
        timerLabel.getStyleClass().add("timer-display");
        timerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        // Affichage du statut (prise de commandes)
        statusLabel = new Label("Commandes : Autorisées");
        statusLabel.getStyleClass().add("status-allowed");
        
        // Boutons de contrôle
        Button startButton = new Button("Démarrer");
        startButton.setOnAction(e -> timerService.startTimer());
        startButton.getStyleClass().add("button-primary");
        
        Button stopButton = new Button("Arrêter");
        stopButton.setOnAction(e -> timerService.stopTimer());
        stopButton.getStyleClass().add("button-secondary");
        
        Button resetButton = new Button("Réinitialiser");
        resetButton.setOnAction(e -> timerService.resetTimer());
        resetButton.getStyleClass().add("button-warning");
        
        // Conteneur pour les boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, stopButton, resetButton);
        
        // Ajouter tous les éléments au composant
        getChildren().addAll(titleLabel, timerLabel, statusLabel, buttonBox);
        
        // Binding pour mettre à jour l'affichage
        timerService.timeDisplayProperty().addListener((obs, oldVal, newVal) -> 
            timerLabel.setText(newVal));
            
        timerService.canTakeOrdersProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                statusLabel.setText("Commandes : Autorisées");
                statusLabel.getStyleClass().remove("status-forbidden");
                statusLabel.getStyleClass().add("status-allowed");
            } else {
                statusLabel.setText("Commandes : FERMÉES");
                statusLabel.getStyleClass().remove("status-allowed");
                statusLabel.getStyleClass().add("status-forbidden");
            }
        });
        
        timerService.isRunningProperty().addListener((obs, oldVal, newVal) -> {
            startButton.setDisable(newVal);
            stopButton.setDisable(!newVal);
        });
    }
}
