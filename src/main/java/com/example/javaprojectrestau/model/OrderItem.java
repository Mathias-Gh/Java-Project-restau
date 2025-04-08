package com.example.javaprojectrestau.model;

import java.math.BigDecimal;

public class OrderItem {
    private Long id;
    private Long orderId;
    private Long dishId;
    private String dishName;
    private int quantity;
    private BigDecimal price;
    
    public OrderItem() {}
    
    public OrderItem(Long id, Long orderId, Long dishId, String dishName, int quantity, BigDecimal price) {
        this.id = id;
        this.orderId = orderId;
        this.dishId = dishId;
        this.dishName = dishName;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    @Override
    public String toString() {
        return quantity + "x " + dishName + " (" + price + "€)";
    }
}
