package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.db.DatabaseConnection;
import com.example.javaprojectrestau.model.TableResto;
import com.example.javaprojectrestau.service.OrderService;
import com.example.javaprojectrestau.service.TableRestoService;

import java.sql.Connection;
import java.util.List;

public class TableRestoController {
    private TableRestoService service;

    public TableRestoController(Connection connection) {
        this.service = new TableRestoService(connection);
    }

    public List<TableResto> getAllTables() {
        return service.getAllTables();
    }

    public void assignTableToClient(int idTable, int clientId, String commande) {
        boolean success = service.assignTableToClient(idTable, clientId, commande);

        if (success) {
            // Création de la commande associée
            OrderService orderService = new OrderService();
            orderService.createCommande(clientId, idTable, commande);

            System.out.println("✅ Table " + idTable + " assignée au client " + clientId + " avec commande : " + commande);
        } else {
            System.out.println("❌ La table " + idTable + " n’a pas pu être assignée.");
        }
    }
}
