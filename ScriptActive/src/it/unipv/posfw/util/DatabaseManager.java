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
        // Nota il "/" all'inizio: cerca il file nella radice di src
        try (InputStream input = getClass().getResourceAsStream("/db.properties")) {
            if (input == null) {
                System.out.println("Errore: File db.properties non trovato nella cartella src!");
                return;
            }
            prop.load(input);
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                prop.getProperty("db.url"), 
                prop.getProperty("db.user"), 
                prop.getProperty("db.password")
            );
            System.out.println("Connessione DB stabilita!");
        } catch (Exception e) {
            System.err.println("Errore durante la connessione al DB:");
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}