module com.example.javaprojectrestau {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.pdfbox;

    // Autres requires existants...

    opens com.example.javaprojectrestau to javafx.fxml;
    opens com.example.javaprojectrestau.controller to javafx.fxml;
    opens com.example.javaprojectrestau.model to javafx.base;

    exports com.example.javaprojectrestau;
    exports com.example.javaprojectrestau.controller;
    exports com.example.javaprojectrestau.model;
    exports com.example.javaprojectrestau.service;
    exports com.example.javaprojectrestau.dao;
    exports com.example.javaprojectrestau.util;
    exports com.example.javaprojectrestau.db;
}