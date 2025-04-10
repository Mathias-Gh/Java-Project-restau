package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.DishDAO;
import com.example.javaprojectrestau.model.Dish;

import java.util.List;
import java.util.Optional;

public class DishService {
    
    private final DishDAO dishDAO;
    
    public DishService() {
        this.dishDAO = new DishDAO();
        // S'assurer que la table existe
        this.dishDAO.createTableIfNotExists();
    }
    
    public List<Dish> getAllDishes() {
        return dishDAO.findAll();
    }
    
    public Optional<Dish> getDishById(Long id) {
        return dishDAO.findById(id);
    }
    
    public List<Dish> getDishesByCategory(String category) {
        return dishDAO.findByCategory(category);
    }
    
    public Dish saveDish(Dish dish) {
        return dishDAO.save(dish);
    }
    
    public boolean deleteDish(Long id) {
        return dishDAO.delete(id);
    }
}
