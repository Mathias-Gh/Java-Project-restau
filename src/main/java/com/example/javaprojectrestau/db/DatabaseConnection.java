package com.example.javaprojectrestau.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Configuration MySQL pour macOS - pas de mot de passe par défaut
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";  // Utilisateur par défaut sur macOS
    private static final String PASSWORD = "";  // Mot de passe vide par défaut sur macOS
    
    private static Connection connection;
    
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Charger explicitement le pilote MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Essayer d'abord sans mot de passe (configuration macOS)
                try {
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("Connexion à la base de données établie avec succès.");
                } catch (SQLException e) {
                    System.out.println("Tentative de connexion avec mot de passe alternatif...");
                    // Si ça échoue, essayer avec 'root' comme mot de passe
                    connection = DriverManager.getConnection(URL, USER, "root");
                    System.out.println("Connexion à la base de données établie avec mot de passe alternatif.");
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
                e.printStackTrace();
                createMockConnection();
            } catch (ClassNotFoundException e) {
                System.err.println("Pilote MySQL non trouvé: " + e.getMessage());
                e.printStackTrace();
                createMockConnection();
            }
        }
        return connection;
    }
    
    // Créer une connexion simulée pour permettre à l'application de fonctionner en mode démo
    private static void createMockConnection() {
        System.out.println("Création d'une connexion de secours en mémoire pour permettre à l'application de fonctionner.");
        try {
            // Utiliser H2 en mémoire comme solution de secours
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:mem:restaurant_db;DB_CLOSE_DELAY=-1", "sa", "");
            System.out.println("Base de données H2 en mémoire créée pour le mode démo.");
            
            // Créer une table de secours pour les tests
            try (var stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS dishes (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(100) NOT NULL," +
                        "price DECIMAL(10,2) NOT NULL," +
                        "description TEXT," +
                        "category VARCHAR(50)" +
                        ")");
                System.out.println("Table 'dishes' créée dans la base de données en mémoire.");
            }
        } catch (Exception e) {
            System.err.println("Impossible de créer une base de données de secours: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Connexion à la base de données fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
