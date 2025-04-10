package com.example.javaprojectrestau.dao;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.model.OrderItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OrderDAO {

    /**
     * S'assure que la table orders et la colonne table_id existent
     */
    public void createTablesIfNotExist() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de créer les tables: connexion à la base de données inexistante");
            return;
        }
        
        try {
            boolean addTableIdColumn = false;
            
            // Créer la table orders si elle n'existe pas
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "customer_name VARCHAR(100) NOT NULL," +
                        "order_time TIMESTAMP," +
                        "status VARCHAR(20) NOT NULL," +
                        "notes TEXT" +
                        ")");
                System.out.println("Table 'orders' créée ou déjà existante.");
                
                // Vérifier si la colonne table_id existe
                try {
                    ResultSet rs = stmt.executeQuery("SELECT table_id FROM orders LIMIT 1");
                    rs.close();
                    // Si on arrive ici, la colonne table_id existe
                    System.out.println("La colonne 'table_id' existe déjà dans la table 'orders'.");
                } catch (SQLException e) {
                    // La colonne n'existe pas, on va l'ajouter plus tard
                    addTableIdColumn = true;
                }
                
                // Ajouter la colonne table_id si nécessaire
                if (addTableIdColumn) {
                    try {
                        stmt.execute("ALTER TABLE orders ADD COLUMN table_id BIGINT");
                        System.out.println("Colonne 'table_id' ajoutée à la table 'orders'.");
                    } catch (SQLException e) {
                        // Ignorer si la colonne existe déjà
                        System.err.println("Erreur lors de l'ajout de la colonne table_id: " + e.getMessage());
                    }
                }
                
                // Table des items de commande
                stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "order_id BIGINT NOT NULL," +
                        "dish_id BIGINT NOT NULL," +
                        "dish_name VARCHAR(100) NOT NULL," +
                        "quantity INT NOT NULL," +
                        "price DECIMAL(10,2) NOT NULL," +
                        "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE" +
                        ")");
                System.out.println("Table 'order_items' créée ou déjà existante.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        Map<Long, Order> orderMap = new HashMap<>();
        
        String orderSql = "SELECT * FROM orders ORDER BY order_time DESC";
        String itemsSql = "SELECT * FROM order_items WHERE order_id IN " +
                          "(SELECT id FROM orders)";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les commandes: connexion à la base de données inexistante");
            return orders;
        }
        
        try {
            // Récupérer toutes les commandes
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(orderSql)) {
                
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    orders.add(order);
                    orderMap.put(order.getId(), order);
                }
            }
            
            // Récupérer tous les items et les associer aux commandes
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(itemsSql)) {
                
                while (rs.next()) {
                    OrderItem item = mapResultSetToOrderItem(rs);
                    if (orderMap.containsKey(item.getOrderId())) {
                        orderMap.get(item.getOrderId()).addItem(item);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return orders;
    }
    
    public List<Order> findByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        Map<Long, Order> orderMap = new HashMap<>();
        
        String orderSql = "SELECT * FROM orders WHERE status = ? ORDER BY order_time DESC";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les commandes: connexion à la base de données inexistante");
            return orders;
        }
        
        try {
            // Récupérer les commandes avec le statut spécifié
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                pstmt.setString(1, status);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    orders.add(order);
                    orderMap.put(order.getId(), order);
                }
            }
            
            // Si nous avons des commandes, récupérer leurs items
            if (!orderMap.isEmpty()) {
                StringBuilder inClause = new StringBuilder("(");
                for (Long id : orderMap.keySet()) {
                    if (inClause.length() > 1) {
                        inClause.append(",");
                    }
                    inClause.append(id);
                }
                inClause.append(")");
                
                String itemsSql = "SELECT * FROM order_items WHERE order_id IN " + inClause;
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(itemsSql)) {
                    
                    while (rs.next()) {
                        OrderItem item = mapResultSetToOrderItem(rs);
                        if (orderMap.containsKey(item.getOrderId())) {
                            orderMap.get(item.getOrderId()).addItem(item);
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes par statut: " + e.getMessage());
            e.printStackTrace();
        }
        
        return orders;
    }
    
    public Optional<Order> findById(Long id) {
        String orderSql = "SELECT * FROM orders WHERE id = ?";
        String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer la commande: connexion à la base de données inexistante");
            return Optional.empty();
        }
        
        try {
            Order order = null;
            
            // Récupérer la commande
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    order = mapResultSetToOrder(rs);
                } else {
                    return Optional.empty();
                }
            }
            
            // Récupérer les items de la commande
            try (PreparedStatement pstmt = conn.prepareStatement(itemsSql)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    OrderItem item = mapResultSetToOrderItem(rs);
                    order.addItem(item);
                }
            }
            
            return Optional.of(order);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la commande avec id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    public Order save(Order order) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de sauvegarder la commande: connexion à la base de données inexistante");
            return order;
        }
        
        try {
            conn.setAutoCommit(false);
            
            // On vérifie d'abord si la colonne table_id existe dans la table orders
            boolean tableIdExists = false;
            try (Statement stmt = conn.createStatement()) {
                try {
                    ResultSet rs = stmt.executeQuery("SELECT table_id FROM orders LIMIT 1");
                    rs.close();
                    tableIdExists = true;
                } catch (SQLException e) {
                    // La colonne n'existe pas, on va l'ajouter
                    try {
                        stmt.execute("ALTER TABLE orders ADD COLUMN table_id BIGINT");
                        System.out.println("Colonne 'table_id' ajoutée à la table 'orders'.");
                        tableIdExists = true;
                    } catch (SQLException ex) {
                        System.err.println("Impossible d'ajouter la colonne table_id: " + ex.getMessage());
                    }
                }
            }
            
            if (order.getId() == null) {
                // Nouvelle commande
                String sql;
                
                if (tableIdExists) {
                    if (order.getTableId() == null) {
                        // Si tableId est null mais la colonne existe, utiliser NULL dans SQL
                        sql = "INSERT INTO orders (customer_name, order_time, status, notes, table_id) VALUES (?, ?, ?, ?, NULL)";
                        
                        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setString(1, order.getCustomerName());
                            pstmt.setTimestamp(2, Timestamp.valueOf(order.getOrderTime()));
                            pstmt.setString(3, order.getStatus());
                            pstmt.setString(4, order.getNotes());
                            
                            int affectedRows = pstmt.executeUpdate();
                            if (affectedRows > 0) {
                                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                                    if (generatedKeys.next()) {
                                        order.setId(generatedKeys.getLong(1));
                                    }
                                }
                            }
                        }
                    } else {
                        // Si tableId a une valeur et la colonne existe
                        sql = "INSERT INTO orders (customer_name, order_time, status, notes, table_id) VALUES (?, ?, ?, ?, ?)";
                        
                        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setString(1, order.getCustomerName());
                            pstmt.setTimestamp(2, Timestamp.valueOf(order.getOrderTime()));
                            pstmt.setString(3, order.getStatus());
                            pstmt.setString(4, order.getNotes());
                            pstmt.setLong(5, order.getTableId());
                            
                            int affectedRows = pstmt.executeUpdate();
                            if (affectedRows > 0) {
                                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                                    if (generatedKeys.next()) {
                                        order.setId(generatedKeys.getLong(1));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Si la colonne n'existe pas
                    sql = "INSERT INTO orders (customer_name, order_time, status, notes) VALUES (?, ?, ?, ?)";
                    
                    try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, order.getCustomerName());
                        pstmt.setTimestamp(2, Timestamp.valueOf(order.getOrderTime()));
                        pstmt.setString(3, order.getStatus());
                        pstmt.setString(4, order.getNotes());
                        
                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows > 0) {
                            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    order.setId(generatedKeys.getLong(1));
                                }
                            }
                        }
                    }
                }
                
                // Sauvegarder les items
                if (order.getId() != null && !order.getItems().isEmpty()) {
                    String itemSql = "INSERT INTO order_items (order_id, dish_id, dish_name, quantity, price) " +
                                     "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                        for (OrderItem item : order.getItems()) {
                            pstmt.setLong(1, order.getId());
                            pstmt.setLong(2, item.getDishId());
                            pstmt.setString(3, item.getDishName());
                            pstmt.setInt(4, item.getQuantity());
                            pstmt.setBigDecimal(5, item.getPrice());
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }
            } else {
                // Mise à jour d'une commande existante
                String sql;
                if (tableIdExists) {
                    sql = "UPDATE orders SET customer_name = ?, status = ?, notes = ?, table_id = ? WHERE id = ?";
                    
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, order.getCustomerName());
                        pstmt.setString(2, order.getStatus());
                        pstmt.setString(3, order.getNotes());
                        
                        if (order.getTableId() != null) {
                            pstmt.setLong(4, order.getTableId());
                        } else {
                            pstmt.setNull(4, Types.BIGINT);
                        }
                        
                        pstmt.setLong(5, order.getId());
                        pstmt.executeUpdate();
                    }
                } else {
                    sql = "UPDATE orders SET customer_name = ?, status = ?, notes = ? WHERE id = ?";
                    
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, order.getCustomerName());
                        pstmt.setString(2, order.getStatus());
                        pstmt.setString(3, order.getNotes());
                        pstmt.setLong(4, order.getId());
                        pstmt.executeUpdate();
                    }
                }
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la commande: " + e.getMessage());
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erreur lors de la restauration de l'autoCommit: " + e.getMessage());
            }
        }
        
        return order;
    }
    
    public boolean updateStatus(Long id, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de mettre à jour le statut: connexion à la base de données inexistante");
            return false;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setLong(2, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut de la commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de supprimer la commande: connexion à la base de données inexistante");
            return false;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String customerName = rs.getString("customer_name");
        Timestamp timestamp = rs.getTimestamp("order_time");
        LocalDateTime orderTime = timestamp != null ? timestamp.toLocalDateTime() : null;
        String status = rs.getString("status");
        String notes = rs.getString("notes");
        
        Order order = new Order(id, customerName, orderTime, status, notes);
        
        // Vérifier si la colonne table_id existe avant d'essayer de la lire
        try {
            long tableId = rs.getLong("table_id");
            if (!rs.wasNull()) {
                order.setTableId(tableId);
            }
        } catch (SQLException e) {
            // Ignorer silencieusement si la colonne n'existe pas encore
        }
        
        return order;
    }
    
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long orderId = rs.getLong("order_id");
        Long dishId = rs.getLong("dish_id");
        String dishName = rs.getString("dish_name");
        int quantity = rs.getInt("quantity");
        java.math.BigDecimal price = rs.getBigDecimal("price");
        
        return new OrderItem(id, orderId, dishId, dishName, quantity, price);
    }
}
