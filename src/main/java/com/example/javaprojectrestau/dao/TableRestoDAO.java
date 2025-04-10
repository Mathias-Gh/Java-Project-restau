package com.example.javaprojectrestau.dao;

import com.example.javaprojectrestau.model.TableResto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableRestoDAO {
    private final Connection connection;

    public TableRestoDAO(Connection connection) {
        this.connection = connection;
    }

    // ✅ Récupère toutes les tables
    public List<TableResto> getAllTables() throws SQLException {
        List<TableResto> tables = new ArrayList<>();
        String query = "SELECT * FROM table_resto";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tables.add(new TableResto(
                        rs.getInt("id"),
                        rs.getInt("taille"),
                        rs.getInt("emplacement"),
                        rs.getBoolean("disponible")
                ));
            }
        }

        return tables;
    }

    // ✅ Liste des tables disponibles uniquement
    public List<TableResto> getTablesDisponibles() throws SQLException {
        List<TableResto> tables = new ArrayList<>();
        String query = "SELECT * FROM table_resto WHERE disponible = TRUE";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tables.add(new TableResto(
                        rs.getInt("id"),
                        rs.getInt("taille"),
                        rs.getInt("emplacement"),
                        rs.getBoolean("disponible")
                ));
            }
        }

        return tables;
    }

    // ✅ Récupère une table par son ID
    public TableResto getTableById(int id) throws SQLException {
        String query = "SELECT * FROM table_resto WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new TableResto(
                        rs.getInt("id"),
                        rs.getInt("taille"),
                        rs.getInt("emplacement"),
                        rs.getBoolean("disponible")
                );
            }
        }

        return null;
    }

    // ✅ Assigner une table à un client (rendre indisponible)
    public boolean assignerTable(int id) throws SQLException {
        String query = "UPDATE table_resto SET disponible = FALSE WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0; // true si la mise à jour a été faite
        }
    }
}
