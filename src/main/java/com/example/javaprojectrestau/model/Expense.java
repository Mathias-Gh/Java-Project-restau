package com.example.javaprojectrestau.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modèle pour représenter une dépense du restaurant
 */
public class Expense {
    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDateTime date;
    private String category;
    
    public Expense() {
        this.date = LocalDateTime.now();
    }
    
    public Expense(Long id, String name, BigDecimal amount, LocalDateTime date, String category) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.date = date != null ? date : LocalDateTime.now();
        this.category = category;
    }
    
    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    @Override
    public String toString() {
        return name + " (" + amount + "€)";
    }
}
