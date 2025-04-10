package com.example.javaprojectrestau.model;

public class Client {
    private int id;
    private String nom;

    public Client(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public Client(String nom) {
        this.nom = nom;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public String toString() {
        return nom; // Important pour l'affichage dans ComboBox
    }
}
