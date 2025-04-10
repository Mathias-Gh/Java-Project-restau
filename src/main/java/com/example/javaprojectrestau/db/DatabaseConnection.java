package com.example.javaprojectrestau.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.InputStream;
import java.sql.Blob;

public class DatabaseConnection {
    // Configuration MySQL pour macOS pas de mot de passe par défaut
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";  // Utilisateur par défaut sur macOS
    private static final String PASSWORD = "";  // Mot de passe vide par défaut sur macOS
    
    private static Connection connection;
    
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Charger explicitement le pilote MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Essayer d'abord sans mots de passe (configuration macOS)
                try {
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("Connexion à la base de données établie avec succès.");
                    initializeDatabase(); // Initialiser la structure de la base de données
                } catch (SQLException e) {
                    System.out.println("Tentative de connexion avec mot de passe alternatif...");
                    // Si ça échoue, essayer avec 'root' comme mot de passe
                    connection = DriverManager.getConnection(URL, USER, "root");
                    System.out.println("Connexion à la base de données établie avec mot de passe alternatif.");
                    initializeDatabase(); // Initialiser la structure de la base de données
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
    
    // Initialiser la structure de la base de données
    private static void initializeDatabase() {
        try {
            // Vérifier si la table existe, sinon la créer
            try (var stmt = connection.createStatement()) {
                // Créer la table dishes si elle n'existe pas
                stmt.execute("CREATE TABLE IF NOT EXISTS dishes (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(100) NOT NULL," +
                        "price DECIMAL(10,2) NOT NULL," +
                        "description TEXT," +
                        "category VARCHAR(50)," +
                        "image LONGBLOB" +
                        ")");

                // Vérifier si la colonne image existe déjà
                ResultSet rs = connection.getMetaData().getColumns(null, null, "dishes", "image");
                if (!rs.next()) {
                    // La colonne n'existe pas, l'ajouter
                    stmt.execute("ALTER TABLE dishes ADD COLUMN image LONGBLOB");
                    System.out.println("Colonne 'image' ajoutée à la table 'dishes'.");
                }
                stmt.execute("CREATE TABLE IF NOT EXISTS employes (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(100) NOT NULL UNIQUE," +
                        "working_hour INT NOT NULL," +
                        "hour_worked INT," +
                        "post VARCHAR(50)" +
                        ")");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        }
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
                        "category VARCHAR(50)," +
                        "image BLOB" +
                        ")");
                System.out.println("Table 'dishes' créée dans la base de données en mémoire avec support d'images.");
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
    
    // Méthodes utilitaires pour gérer les images
    
    // Sauvegarder une image pour un plat
    public static boolean saveImage(long dishId, InputStream imageData) {
        String sql = "UPDATE dishes SET image = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBinaryStream(1, imageData);
            pstmt.setLong(2, dishId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de l'image: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Récupérer une image pour un plat
    public static Blob getImage(long dishId) {
        String sql = "SELECT image FROM dishes WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, dishId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBlob("image");
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
