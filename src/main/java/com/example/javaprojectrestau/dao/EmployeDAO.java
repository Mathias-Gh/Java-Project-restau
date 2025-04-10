package com.example.javaprojectrestau.dao;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.Employe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class EmployeDAO { // on crée la table si elle n'existe pas déja
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS employes (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL UNIQUE," +
                "working_hour INT NOT NULL," +
                "hour_worked INT," +
                "post VARCHAR(50)" +
                ")";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de créer la table: connexion à la base de données inexistante");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'employes' créée ou déjà existante.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table 'employes': " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ici, on va Récupérer tous les employés
    public List<Employe> findAll() {
        List<Employe> employes = new ArrayList<>();
        String sql = "SELECT * FROM employes";

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

    // Sauvegarder un employé
    public Employe save(Employe employe) {
        String insertSql = "INSERT INTO employes (name, working_hour, hour_worked, post) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE employes SET working_hour = ?, hour_worked = ?, post = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();

        if (conn == null) {
            System.err.println("Impossible de sauvegarder l'employé : connexion à la base de données inexistante");
            return employe;
        }

        try {
            if (employe.getId() == null) {
                // Insérer un nouvel employé
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
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
                }
            } else {
                // Mettre à jour un employé existant
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, employe.getWorking_hour());
                    pstmt.setInt(2, employe.getHour_worked());
                    pstmt.setString(3, employe.getPost());
                    pstmt.setLong(4, employe.getId());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de l'employé : " + e.getMessage());
            e.printStackTrace();
        }

        return employe;
    }
    // Supprimer un employé
    public boolean delete(Long id) {
        String sql = "DELETE FROM employes WHERE id = ?";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Impossible de supprimer l'employé: connexion à la base de données inexistante");
            return false;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'employé avec id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Transformer un ResultSet en objet Employe
    private Employe mapResultSetToEmploye(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        int working_hour = rs.getInt("working_hour");
        int hour_worked = rs.getInt("hour_worked");
        String post = rs.getString("post");

        return new Employe(id, name, working_hour, hour_worked, post);
    }
}
