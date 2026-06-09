package it.unipv.posfw.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("Errore: File db.properties non trovato!");
                return;
            }
            prop.load(input);
            // Carica driver e connetti
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                prop.getProperty("db.url"), 
                prop.getProperty("db.user"), 
                prop.getProperty("db.password")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public Connection getConnection() { return connection; }
}
