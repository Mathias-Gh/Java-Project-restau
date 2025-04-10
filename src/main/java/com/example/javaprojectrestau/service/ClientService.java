package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.ClientDAO;
import com.example.javaprojectrestau.model.Client;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ClientService {
    private final ClientDAO clientDAO;

    public ClientService(Connection connection) {
        this.clientDAO = new ClientDAO(connection);
    }

    public Client createClient(String nom) {
        try {
            return clientDAO.createClient(nom);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Client> getAllClients() {
        try {
            return clientDAO.getAllClients();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
