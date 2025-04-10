package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.EmployeDAO;
import com.example.javaprojectrestau.model.Employe;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les employés
 */
public class EmployeService {
    
    private final EmployeDAO employeDAO;
    
    public EmployeService() {
        this.employeDAO = new EmployeDAO();
        this.employeDAO.createTablesIfNotExist();
    }
    
    /**
     * Récupère tous les employés
     * @return liste des employés
     */
    public List<Employe> getAllEmployes() {
        return employeDAO.findAll();
    }
    
    /**
     * Récupère un employé par son id
     * @param id identifiant de l'employé
     * @return employé s'il existe
     */
    public Optional<Employe> getEmployeById(Long id) {
        return employeDAO.findById(id);
    }
    
    /**
     * Sauvegarde un employé (création ou mise à jour)
     * @param employe l'employé à sauvegarder
     * @return l'employé sauvegardé avec son id
     */
    public Employe saveEmploye(Employe employe) {
        return employeDAO.save(employe);
    }
    
    /**
     * Met à jour un employé existant
     * @param employe l'employé à mettre à jour
     */
    public void updateEmploye(Employe employe) {
        if (employe.getId() != null) {
            employeDAO.save(employe);
        }
    }
    
    /**
     * Supprime un employé par son id
     * @param id identifiant de l'employé à supprimer
     */
    public void deleteEmploye(Long id) {
        employeDAO.delete(id);
    }
    
    /**
     * Ajoute des heures travaillées à un employé
     * @param employeId identifiant de l'employé
     * @param hours nombre d'heures à ajouter
     */
    public void addHoursWorked(Long employeId, int hours) {
        Optional<Employe> employeOpt = employeDAO.findById(employeId);
        if (employeOpt.isPresent()) {
            Employe employe = employeOpt.get();
            int currentHours = employe.getHour_worked();
            employe.setHour_worked(currentHours + hours);
            employeDAO.save(employe);
        }
    }
}
