package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.TableDAO;
import com.example.javaprojectrestau.model.Table;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les tables du restaurant
 */
public class TableService {
    
    private final TableDAO tableDAO;
    
    public TableService() {
        this.tableDAO = new TableDAO();
        this.tableDAO.createTableIfNotExists();
    }
    
    /**
     * Récupère toutes les tables
     */
    public List<Table> getAllTables() {
        return tableDAO.findAll();
    }
    
    /**
     * Récupère une table par son ID
     */
    public Optional<Table> getTableById(Long id) {
        return tableDAO.findById(id);
    }
    
    /**
     * Récupère toutes les tables disponibles
     */
    public List<Table> getAvailableTables() {
        return tableDAO.findAvailable();
    }
    
    /**
     * Sauvegarde une table (création ou mise à jour)
     */
    public Table saveTable(Table table) {
        return tableDAO.save(table);
    }
    
    /**
     * Supprime une table
     */
    public boolean deleteTable(Long id) {
        return tableDAO.delete(id);
    }
    
    /**
     * Assigne une commande à une table
     */
    public boolean assignOrderToTable(Long tableId, Long orderId) {
        return tableDAO.assignOrderToTable(tableId, orderId);
    }
    
    /**
     * Libère une table
     */
    public boolean releaseTable(Long tableId) {
        return tableDAO.releaseTable(tableId);
    }
    
    /**
     * Vérifie si une table est disponible
     */
    public boolean isTableAvailable(Long tableId) {
        Optional<Table> tableOpt = tableDAO.findById(tableId);
        return tableOpt.map(table -> !table.isOccupee()).orElse(false);
    }
}
