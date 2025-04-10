package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.OrderDAO;
import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Dish;
import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class OrderService {
    
    private final OrderDAO orderDAO;
    private final DishService dishService;
    
    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.dishService = new DishService();
        
        // S'assurer que les tables existent
        this.orderDAO.createTablesIfNotExist();
    }
    
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }
    
    public List<Order> getPendingOrders() {
        return orderDAO.findByStatus("EN_ATTENTE");
    }
    
    public List<Order> getCompletedOrders() {
        return orderDAO.findByStatus("PREPAREE");
    }
    
    public List<Order> getCancelledOrders() {
        return orderDAO.findByStatus("ANNULEE");
    }
    
    public Optional<Order> getOrderById(Long id) {
        return orderDAO.findById(id);
    }
    
    public Order saveOrder(Order order) {
        return orderDAO.save(order);
    }
    
    public boolean markOrderAsCompleted(Long id) {
        return orderDAO.updateStatus(id, "PREPAREE");
    }
    
    public boolean cancelOrder(Long id) {
        return orderDAO.updateStatus(id, "ANNULEE");
    }
    
    public boolean deleteOrder(Long id) {
        return orderDAO.delete(id);
    }
    
    public OrderItem createOrderItem(Long dishId, int quantity) {
        Optional<Dish> dishOptional = dishService.getDishById(dishId);
        
        if (dishOptional.isPresent()) {
            Dish dish = dishOptional.get();
            return new OrderItem(null, null, dishId, dish.getName(), quantity, dish.getPrice());
        }
        
        return null;
    }
    public void createCommande(int clientId, int tableId, String commande) {
        String sql = "INSERT INTO commande (client_id, table_id, date_commande, commande) VALUES (?, ?, NOW(), ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, tableId);
            stmt.setString(3, commande);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
