package com.example.javaprojectrestau.dao;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableDAO {
    
    /**
     * Crée la table 'tables' si elle n'existe pas déjà
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS tables (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "numero VARCHAR(10) NOT NULL," +
                "capacite INT NOT NULL," +
                "emplacement VARCHAR(50)," +
                "occupee BOOLEAN DEFAULT FALSE," +
                "order_id BIGINT NULL" +
                ")";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de créer la table: connexion à la base de données inexistante");
            return;
        }
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'tables' créée ou déjà existante.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table 'tables': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère toutes les tables
     */
    public List<Table> findAll() {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables ORDER BY numero";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les tables: connexion à la base de données inexistante");
            return tables;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Table table = mapResultSetToTable(rs);
                tables.add(table);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des tables: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tables;
    }
    
    /**
     * Récupère une table par son ID
     */
    public Optional<Table> findById(Long id) {
        String sql = "SELECT * FROM tables WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer la table: connexion à la base de données inexistante");
            return Optional.empty();
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTable(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la table ID=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Récupère toutes les tables disponibles (non occupées)
     */
    public List<Table> findAvailable() {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE occupee = FALSE ORDER BY capacite";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les tables disponibles: connexion à la base de données inexistante");
            return tables;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Table table = mapResultSetToTable(rs);
                tables.add(table);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des tables disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tables;
    }
    
    /**
     * Enregistre ou met à jour une table
     */
    public Table save(Table table) {
        if (table.getId() == null) {
            return insert(table);
        } else {
            return update(table);
        }
    }
    
    /**
     * Insère une nouvelle table
     */
    private Table insert(Table table) {
        String sql = "INSERT INTO tables (numero, capacite, emplacement, occupee, order_id) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible d'insérer la table: connexion à la base de données inexistante");
            return table;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, table.getNumero());
            pstmt.setInt(2, table.getCapacite());
            pstmt.setString(3, table.getEmplacement());
            pstmt.setBoolean(4, table.isOccupee());
            
            if (table.getOrderId() != null) {
                pstmt.setLong(5, table.getOrderId());
            } else {
                pstmt.setNull(5, Types.BIGINT);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        table.setId(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de la table: " + e.getMessage());
            e.printStackTrace();
        }
        
        return table;
    }
    
    /**
     * Met à jour une table existante
     */
    private Table update(Table table) {
        String sql = "UPDATE tables SET numero = ?, capacite = ?, emplacement = ?, occupee = ?, order_id = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de mettre à jour la table: connexion à la base de données inexistante");
            return table;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, table.getNumero());
            pstmt.setInt(2, table.getCapacite());
            pstmt.setString(3, table.getEmplacement());
            pstmt.setBoolean(4, table.isOccupee());
            
            if (table.getOrderId() != null) {
                pstmt.setLong(5, table.getOrderId());
            } else {
                pstmt.setNull(5, Types.BIGINT);
            }
            
            pstmt.setLong(6, table.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la table: " + e.getMessage());
            e.printStackTrace();
        }
        
        return table;
    }
    
    /**
     * Assigne une commande à une table
     */
    public boolean assignOrderToTable(Long tableId, Long orderId) {
        String sql = "UPDATE tables SET occupee = TRUE, order_id = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible d'assigner la commande à la table: connexion à la base de données inexistante");
            return false;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, orderId);
            pstmt.setLong(2, tableId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'assignation de la commande à la table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Libère une table
     */
    public boolean releaseTable(Long tableId) {
        String sql = "UPDATE tables SET occupee = FALSE, order_id = NULL WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de libérer la table: connexion à la base de données inexistante");
            return false;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, tableId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la libération de la table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime une table
     */
    public boolean delete(Long id) {
        String sql = "DELETE FROM tables WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de supprimer la table: connexion à la base de données inexistante");
            return false;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Convertit un ResultSet en objet Table
     */
    private Table mapResultSetToTable(ResultSet rs) throws SQLException {
        Table table = new Table();
        table.setId(rs.getLong("id"));
        table.setNumero(rs.getString("numero"));
        table.setCapacite(rs.getInt("capacite"));
        table.setEmplacement(rs.getString("emplacement"));
        table.setOccupee(rs.getBoolean("occupee"));
        
        long orderId = rs.getLong("order_id");
        if (!rs.wasNull()) {
            table.setOrderId(orderId);
        }
        
        return table;
    }
}
