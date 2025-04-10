package com.example.javaprojectrestau.model;

public class TableResto {
    private int id;
    private int taille;
    private int emplacement;  // Ajouter un attribut String pour l'emplacement
    private boolean disponible;

    // Constructeur
    public TableResto(int id, int taille, int emplacement, boolean disponible) {
        this.id = id;
        this.taille = taille;
        this.emplacement = emplacement;  // Initialiser l'emplacement
        this.disponible = disponible;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaille() {
        return taille;
    }

    public void setTaille(int taille) {
        this.taille = taille;
    }

    public int getEmplacement() {
        return emplacement;
    }

    public void setEmplacement(int emplacement) {
        this.emplacement = emplacement;  // Méthode setter pour l'emplacement
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
