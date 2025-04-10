package com.example.javaprojectrestau.dao;

import com.example.javaprojectrestau.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {
    private final Connection connection;

    public ClientDAO(Connection connection) {
        this.connection = connection;
    }

    public Client createClient(String nom) throws SQLException {
        String query = "INSERT INTO client (nom) VALUES (?)";
        PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, nom);
        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        if (keys.next()) {
            return new Client(keys.getInt(1), nom);
        }
        return null;
    }

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM client";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            clients.add(new Client(rs.getInt("id"), rs.getString("nom")));
        }
        return clients;
    }
}
