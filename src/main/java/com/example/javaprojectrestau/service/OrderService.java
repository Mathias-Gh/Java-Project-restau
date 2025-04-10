package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.OrderDAO;
import com.example.javaprojectrestau.model.Dish;
import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.model.OrderItem;

import java.util.List;
import java.util.Optional;

public class OrderService {
    
    private final OrderDAO orderDAO;
    private final DishService dishService;
    private final TableService tableService;
    
    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.dishService = new DishService();
        this.tableService = new TableService();
        
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
        Order savedOrder = orderDAO.save(order);
        
        // Si une table est associée, mettre à jour son statut
        if (savedOrder.getTableId() != null) {
            tableService.assignOrderToTable(savedOrder.getTableId(), savedOrder.getId());
        }
        
        return savedOrder;
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
    
    // Nouvelle méthode pour libérer une table
    public boolean releaseTable(Long orderId) {
        Optional<Order> orderOpt = getOrderById(orderId);
        if (orderOpt.isPresent() && orderOpt.get().getTableId() != null) {
            Long tableId = orderOpt.get().getTableId();
            return tableService.releaseTable(tableId);
        }
        return false;
    }
    
    // Nouvelle méthode pour trouver les commandes par table
    public Optional<Order> getOrderByTableId(Long tableId) {
        return getAllOrders().stream()
                .filter(order -> tableId.equals(order.getTableId()))
                .findFirst();
    }
}
