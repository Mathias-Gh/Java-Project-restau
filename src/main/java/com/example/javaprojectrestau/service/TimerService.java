package com.example.javaprojectrestau.service;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

/**
 * Service gérant le chronomètre pour la durée du service
 */
public class TimerService {
    
    // Durée totale du service en secondes (25 minutes = 1500 secondes)
    private static final int SERVICE_DURATION = 25 * 60;
    
    // Seuil d'alerte en secondes (15 minutes = 900 secondes)
    private static final int WARNING_THRESHOLD = 15 * 60;
    
    // Propriétés observables
    private final IntegerProperty secondsRemaining = new SimpleIntegerProperty(SERVICE_DURATION);
    private final StringProperty timeDisplay = new SimpleStringProperty("25:00");
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty canTakeOrders = new SimpleBooleanProperty(true);
    
    private Timeline timeline;
    
    public TimerService() {
        // Initialiser le timeline pour décrémenter le temps chaque seconde
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int remaining = secondsRemaining.get();
            if (remaining > 0) {
                remaining--;
                secondsRemaining.set(remaining);
                updateTimeDisplay(remaining);
                
                // Vérifier si on a atteint le seuil d'alerte
                if (remaining <= WARNING_THRESHOLD && canTakeOrders.get()) {
                    canTakeOrders.set(false);
                }
            } else {
                // Le temps est écoulé
                stopTimer();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }
    
    /**
     * Démarre le chronomètre
     */
    public void startTimer() {
        if (!isRunning.get()) {
            timeline.play();
            isRunning.set(true);
        }
    }
    
    /**
     * Arrête le chronomètre
     */
    public void stopTimer() {
        if (isRunning.get()) {
            timeline.stop();
            isRunning.set(false);
        }
    }
    
    /**
     * Réinitialise le chronomètre à la durée par défaut
     */
    public void resetTimer() {
        stopTimer();
        secondsRemaining.set(SERVICE_DURATION);
        updateTimeDisplay(SERVICE_DURATION);
        canTakeOrders.set(true);
    }
    
    /**
     * Définit une durée personnalisée pour le service (en minutes)
     */
    public void setServiceDuration(int minutes) {
        stopTimer();
        int seconds = minutes * 60;
        secondsRemaining.set(seconds);
        updateTimeDisplay(seconds);
        canTakeOrders.set(seconds > WARNING_THRESHOLD);
    }
    
    /**
     * Met à jour l'affichage du temps au format MM:SS
     */
    private void updateTimeDisplay(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        timeDisplay.set(String.format("%02d:%02d", minutes, secs));
    }
    
    // Getters pour les propriétés
    public IntegerProperty secondsRemainingProperty() {
        return secondsRemaining;
    }
    
    public StringProperty timeDisplayProperty() {
        return timeDisplay;
    }
    
    public BooleanProperty isRunningProperty() {
        return isRunning;
    }
    
    public BooleanProperty canTakeOrdersProperty() {
        return canTakeOrders;
    }
    
    public String getTimeDisplay() {
        return timeDisplay.get();
    }
    
    public boolean getIsRunning() {
        return isRunning.get();
    }
    
    public boolean getCanTakeOrders() {
        return canTakeOrders.get();
    }
    
    public int getSecondsRemaining() {
        return secondsRemaining.get();
    }
}
