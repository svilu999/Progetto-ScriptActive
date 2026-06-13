package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseConnection {
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Leggiamo il file db.properties
                Properties prop = new Properties();
                InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties");
                
                if (input == null) {
                    System.out.println("ERRORE: File db.properties non trovato!");
                    return null;
                }
                prop.load(input);

                // Carichiamo le impostazioni dal file
                String url = prop.getProperty("db.url");
                String user = prop.getProperty("db.user");
                String pass = prop.getProperty("db.password");

                connection = DriverManager.getConnection(url, user, pass);
                System.out.println(" Connessione stabilita tramite db.properties.");
            } catch (Exception e) {
                System.out.println("ERRORE: Impossibile connettersi.");
                e.printStackTrace();
            }
        }
        return connection;
    }
}