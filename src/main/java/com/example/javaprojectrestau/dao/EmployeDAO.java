package com.example.javaprojectrestau.dao;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Employe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

 //DAO pour accéder aux données des employés
public class EmployeDAO {

     // Crée les tables si elles n'existent pas
    public void createTablesIfNotExist() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de créer les tables: connexion à la base de données inexistante");
            return;
        }
        
        try (Statement stmt = conn.createStatement()) {
            // Table des employés
            stmt.execute("CREATE TABLE IF NOT EXISTS employes (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "working_hour INT NOT NULL," +
                    "hour_worked INT NOT NULL," +
                    "post VARCHAR(100) NOT NULL" +
                    ")");
            System.out.println("Table 'employes' créée ou déjà existante.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère tous les employés
     * @return liste d'employés
     */
    public List<Employe> findAll() {
        List<Employe> employes = new ArrayList<>();
        String sql = "SELECT * FROM employes ORDER BY name ASC";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer les employés: connexion à la base de données inexistante");
            return employes;
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Employe employe = mapResultSetToEmploye(rs);
                employes.add(employe);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des employés: " + e.getMessage());
            e.printStackTrace();
        }
        
        return employes;
    }
    
    /**
     * Récupère un employé par son id
     * @param id identifiant de l'employé
     * @return employé s'il existe
     */
    public Optional<Employe> findById(Long id) {
        String sql = "SELECT * FROM employes WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de récupérer l'employé: connexion à la base de données inexistante");
            return Optional.empty();
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEmploye(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'employé avec id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Sauvegarde un employé (création ou mise à jour)
     * @param employe l'employé à sauvegarder
     * @return l'employé sauvegardé avec son id
     */
    public Employe save(Employe employe) {
        if (employe.getId() == null) {
            return insert(employe);
        } else {
            return update(employe);
        }
    }

     //Insère un nouvel employé
    private Employe insert(Employe employe) {
        String sql = "INSERT INTO employes (name, working_hour, hour_worked, post) VALUES (?, ?, ?, ?)";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible d'insérer l'employé: connexion à la base de données inexistante");
            return employe;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, employe.getName());
            pstmt.setInt(2, employe.getWorking_hour());
            pstmt.setInt(3, employe.getHour_worked());
            pstmt.setString(4, employe.getPost());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        employe.setId(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de l'employé: " + e.getMessage());
            e.printStackTrace();
        }
        
        return employe;
    }

     // Met à jour un employé existant
    private Employe update(Employe employe) {
        String sql = "UPDATE employes SET name = ?, working_hour = ?, hour_worked = ?, post = ? WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de mettre à jour l'employé: connexion à la base de données inexistante");
            return employe;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employe.getName());
            pstmt.setInt(2, employe.getWorking_hour());
            pstmt.setInt(3, employe.getHour_worked());
            pstmt.setString(4, employe.getPost());
            pstmt.setLong(5, employe.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'employé: " + e.getMessage());
            e.printStackTrace();
        }
        
        return employe;
    }
    
    /**
     * Supprime un employé
     * @param id identifiant de l'employé à supprimer
     */
    public void delete(Long id) {
        String sql = "DELETE FROM employes WHERE id = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de supprimer l'employé: connexion à la base de données inexistante");
            return;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'employé: " + e.getMessage());
            e.printStackTrace();
        }
    }
     // Convertit un ResultSet en objet Employe

    private Employe mapResultSetToEmploye(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        int workingHour = rs.getInt("working_hour");
        int hourWorked = rs.getInt("hour_worked");
        String post = rs.getString("post");
        
        return new Employe(id, name, workingHour, hourWorked, post);
    }
}
