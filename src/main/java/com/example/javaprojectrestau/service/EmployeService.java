package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.EmployeDAO;
import com.example.javaprojectrestau.model.Employe;


import java.util.List;

public class EmployeService {

    private final EmployeDAO EmployeDAO;

    public EmployeService() {
        this.EmployeDAO = new EmployeDAO();
        // S'assurer que la table existe
        this.EmployeDAO.createTableIfNotExists();
    }
    
    public List<Employe> getAllEmploye() {
        return EmployeDAO.findAll();
    }

    public Employe saveEmploye(Employe employe) {
        return EmployeDAO.save(employe);
    }
    
    public void deleteEmploye(Long id) {
        EmployeDAO.delete(id);
    }
}
