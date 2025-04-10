package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.TableRestoDAO;
import com.example.javaprojectrestau.model.TableResto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TableRestoService {
    private final TableRestoDAO tableRestoDAO;

    public TableRestoService(Connection connection) {
        this.tableRestoDAO = new TableRestoDAO(connection);
    }

    public List<TableResto> getAllTables() {
        try {
            return tableRestoDAO.getAllTables();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean assignTableToClient(int tableId, int clientId, String commande) {
        try {
            TableResto table = tableRestoDAO.getTableById(tableId);
            if (table != null && table.isDisponible()) {
                boolean success = tableRestoDAO.assignerTable(tableId);

                if (success) {
                    OrderService orderService = new OrderService();
                    orderService.createCommande(clientId, tableId, commande);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Ajoute cette méthode pour assigner une table sans commande
    public boolean assignTableToClient(int tableId, int clientId) {
        try {
            TableResto table = tableRestoDAO.getTableById(tableId);
            if (table != null && table.isDisponible()) {
                return tableRestoDAO.assignerTable(tableId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
