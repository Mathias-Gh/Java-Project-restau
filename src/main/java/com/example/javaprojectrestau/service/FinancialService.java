package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.OrderDAO;
import com.example.javaprojectrestau.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour gérer les données financières du restaurant
 */
public class FinancialService {
    
    private final OrderDAO orderDAO;
    
    public FinancialService() {
        this.orderDAO = new OrderDAO();
    }
    
    /**
     * Calcule le revenu total pour une période donnée
     */
    public BigDecimal calculateTotalRevenue(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = getOrdersInPeriod(start, end);
        
        return orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calcule le revenu par jour pour une période donnée
     */
    public Map<String, BigDecimal> calculateRevenueByDay(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = getOrdersInPeriod(start, end);
        Map<String, BigDecimal> revenueByDay = new HashMap<>();
        
        for (Order order : orders) {
            String dateKey = order.getOrderTime().toLocalDate().toString();
            BigDecimal orderTotal = order.getTotalPrice();
            
            revenueByDay.merge(dateKey, orderTotal, BigDecimal::add);
        }
        
        return revenueByDay;
    }
    
    /**
     * Calcule le revenu par catégorie de plat pour une période donnée
     */
    public Map<String, BigDecimal> calculateRevenueByCategoryForPeriod(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = getOrdersInPeriod(start, end);
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();
        
        // Dans une implémentation réelle, ces données seraient extraites de la base de données
        // Pour cette démonstration, on utilise un tableau fictif associant la catégorie à chaque plat
        
        return revenueByCategory;
    }
    
    /**
     * Récupère les commandes sur une période donnée
     */
    private List<Order> getOrdersInPeriod(LocalDateTime start, LocalDateTime end) {
        return orderDAO.findAll().stream()
                .filter(order -> {
                    LocalDateTime orderTime = order.getOrderTime();
                    return orderTime != null && 
                           !orderTime.isBefore(start) && 
                           orderTime.isBefore(end);
                })
                .collect(Collectors.toList());
    }
}
