package com.example.javaprojectrestau.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private String customerName;
    private LocalDateTime orderTime;
    private String status; // "EN_ATTENTE", "PREPAREE", "ANNULEE"
    private List<OrderItem> items;
    private String notes;
    private Long tableId; // Nouvel attribut pour lier une table à une commande
    
    public Order() {
        this.orderTime = LocalDateTime.now();
        this.status = "EN_ATTENTE";
        this.items = new ArrayList<>();
    }
    
    public Order(Long id, String customerName, LocalDateTime orderTime, String status, String notes) {
        this.id = id;
        this.customerName = customerName;
        this.orderTime = orderTime != null ? orderTime : LocalDateTime.now();
        this.status = status != null ? status : "EN_ATTENTE";
        this.notes = notes;
        this.items = new ArrayList<>();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Nouveau getter et setter pour tableId
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    
    // Méthodes utilitaires
    public void addItem(OrderItem item) {
        this.items.add(item);
    }
    
    public void removeItem(OrderItem item) {
        this.items.remove(item);
    }
    
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public String toString() {
        return "Commande #" + id + " - " + customerName + " (" + status + ")";
    }
}
