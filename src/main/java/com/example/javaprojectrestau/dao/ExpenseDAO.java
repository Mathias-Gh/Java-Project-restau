package com.example.javaprojectrestau.dao;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Expense;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour accéder aux données des dépenses
 */
public class ExpenseDAO {

    /**
     * Crée les tables si elles n'existent pas
     */
    public void createTablesIfNotExist() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de créer les tables: connexion à la base de données inexistante");
            return;
        }
        
        try (Statement stmt = conn.createStatement()) {
            // Table des dépenses
            stmt.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "amount DECIMAL(10,2) NOT NULL," +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "category VARCHAR(50)" +
                    ")");
            System.out.println("Table 'expenses' créée ou déjà existante.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère toutes les dépenses
     * @return liste de dépenses
     */
    public List<Expense> findAll() {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses ORDER BY date DESC";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les dépenses: connexion à la base de données inexistante");
            return expenses;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Expense expense = mapResultSetToExpense(rs);
                expenses.add(expense);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des dépenses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return expenses;
    }
    
    /**
     * Récupère les dépenses pour une période donnée
     */
    public List<Expense> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les dépenses: connexion à la base de données inexistante");
            return expenses;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = mapResultSetToExpense(rs);
                    expenses.add(expense);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des dépenses par période: " + e.getMessage());
            e.printStackTrace();
        }
        
        return expenses;
    }
    
    /**
     * Récupère une dépense par son id
     */
    public Optional<Expense> findById(Long id) {
        String sql = "SELECT * FROM expenses WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer la dépense: connexion à la base de données inexistante");
            return Optional.empty();
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExpense(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la dépense avec id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Sauvegarde une dépense (création ou mise à jour)
     */
    public Expense save(Expense expense) {
        if (expense.getId() == null) {
            return insert(expense);
        } else {
            return update(expense);
        }
    }
    
    /**
     * Insère une nouvelle dépense
     */
    private Expense insert(Expense expense) {
        String sql = "INSERT INTO expenses (name, amount, date, category) VALUES (?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible d'insérer la dépense: connexion à la base de données inexistante");
            return expense;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, expense.getName());
            pstmt.setBigDecimal(2, expense.getAmount());
            pstmt.setTimestamp(3, Timestamp.valueOf(expense.getDate()));
            pstmt.setString(4, expense.getCategory());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        expense.setId(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de la dépense: " + e.getMessage());
            e.printStackTrace();
        }
        
        return expense;
    }
    
    /**
     * Met à jour une dépense existante
     */
    private Expense update(Expense expense) {
        String sql = "UPDATE expenses SET name = ?, amount = ?, date = ?, category = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de mettre à jour la dépense: connexion à la base de données inexistante");
            return expense;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, expense.getName());
            pstmt.setBigDecimal(2, expense.getAmount());
            pstmt.setTimestamp(3, Timestamp.valueOf(expense.getDate()));
            pstmt.setString(4, expense.getCategory());
            pstmt.setLong(5, expense.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la dépense: " + e.getMessage());
            e.printStackTrace();
        }
        
        return expense;
    }
    
    /**
     * Supprime une dépense
     */
    public boolean delete(Long id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de supprimer la dépense: connexion à la base de données inexistante");
            return false;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la dépense: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Convertit un ResultSet en objet Expense
     */
    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        java.math.BigDecimal amount = rs.getBigDecimal("amount");
        Timestamp timestamp = rs.getTimestamp("date");
        LocalDateTime date = timestamp != null ? timestamp.toLocalDateTime() : null;
        String category = rs.getString("category");
        
        return new Expense(id, name, amount, date, category);
    }
}
