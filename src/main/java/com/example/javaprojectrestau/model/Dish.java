package com.example.javaprojectrestau.model;

import java.math.BigDecimal;
import javafx.scene.image.Image;

public class Dish {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String category;
    private Image image; // Pour l'affichage dans JavaFX
    
    public Dish() {}
    
    public Dish(Long id, String name, BigDecimal price, String description, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
    }
    
    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
    
    @Override
    public String toString() {
        return name + " - " + price + "€";
    }
}
