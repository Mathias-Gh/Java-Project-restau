package com.example.javaprojectrestau.dao;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Dish;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DishDAO {

    // Créer la table si elle n'existe pas
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS dishes (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "price DECIMAL(10,2) NOT NULL," +
                "description TEXT," +
                "category VARCHAR(50)," +
                "image LONGBLOB" +
                ")";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de créer la table: connexion à la base de données inexistante");
            return;
        }
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'dishes' créée ou déjà existante.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table 'dishes': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Récupérer tous les plats
    public List<Dish> findAll() {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT * FROM dishes";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les plats: connexion à la base de données inexistante");
            return dishes;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Dish dish = mapResultSetToDish(rs);
                dishes.add(dish);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des plats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dishes;
    }
    
    // Récupérer un plat par son ID
    public Optional<Dish> findById(Long id) {
        String sql = "SELECT * FROM dishes WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer le plat: connexion à la base de données inexistante");
            return Optional.empty();
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToDish(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du plat avec id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    // Sauvegarder ou mettre à jour un plat
    public Dish save(Dish dish) {
        String insertSql = "INSERT INTO dishes (name, price, description, category) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE dishes SET name = ?, price = ?, description = ?, category = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de sauvegarder le plat: connexion à la base de données inexistante");
            return dish;
        }
        
        try {
            if (dish.getId() == null) {
                // Insert nouveau plat
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, dish.getName());
                    pstmt.setBigDecimal(2, dish.getPrice());
                    pstmt.setString(3, dish.getDescription());
                    pstmt.setString(4, dish.getCategory());
                    
                    int affectedRows = pstmt.executeUpdate();
                    
                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                dish.setId(generatedKeys.getLong(1));
                            }
                        }
                    }
                }
            } else {
                // Update plat existant
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, dish.getName());
                    pstmt.setBigDecimal(2, dish.getPrice());
                    pstmt.setString(3, dish.getDescription());
                    pstmt.setString(4, dish.getCategory());
                    pstmt.setLong(5, dish.getId());
                    
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du plat: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dish;
    }
    
    // Supprimer un plat
    public boolean delete(Long id) {
        String sql = "DELETE FROM dishes WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de supprimer le plat: connexion à la base de données inexistante");
            return false;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du plat avec id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Récupérer les plats par catégorie
    public List<Dish> findByCategory(String category) {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT * FROM dishes WHERE category = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les plats par catégorie: connexion à la base de données inexistante");
            return dishes;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Dish dish = mapResultSetToDish(rs);
                dishes.add(dish);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des plats de catégorie " + category + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return dishes;
    }
    
    // Utilitaire pour mapper un ResultSet en objet Dish
    private Dish mapResultSetToDish(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        BigDecimal price = rs.getBigDecimal("price");
        String description = rs.getString("description");
        String category = rs.getString("category");
        
        return new Dish(id, name, price, description, category);
    }
}
