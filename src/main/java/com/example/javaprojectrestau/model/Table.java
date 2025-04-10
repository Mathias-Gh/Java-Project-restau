package com.example.javaprojectrestau.model;

/**
 * Modèle pour représenter une table du restaurant
 */
public class Table {
    private Long id;
    private String numero;
    private int capacite;
    private String emplacement;
    private boolean occupee;
    private Long orderId; // ID de la commande associée à cette table (si occupée)
    
    public Table() {
        this.occupee = false;
    }
    
    public Table(Long id, String numero, int capacite, String emplacement) {
        this.id = id;
        this.numero = numero;
        this.capacite = capacite;
        this.emplacement = emplacement;
        this.occupee = false;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    
    public String getEmplacement() { return emplacement; }
    public void setEmplacement(String emplacement) { this.emplacement = emplacement; }
    
    public boolean isOccupee() { return occupee; }
    public void setOccupee(boolean occupee) { this.occupee = occupee; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    @Override
    public String toString() {
        return "Table " + numero + " (" + capacite + " personnes, " + emplacement + ")";
    }
}
