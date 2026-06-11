package it.unipv.posfw.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestore centralizzato della connessione al database.
 *
 *
 * questo DatabaseManager NON mantiene una singola Connection aperta.
 *
 * Ogni chiamata a getConnection() crea una nuova connessione valida.
 * In questo modo i DAO possono usare correttamente il try-with-resources:
 *
 * try (Connection conn = DatabaseManager.getInstance().getConnection()) {
 *     ...
 * }
 *
 * Alla fine del try la connessione viene chiusa, ma alla chiamata successiva
 * ne viene aperta una nuova. Così si evita l'errore:
 * "No operations allowed after connection closed".
 */
public class DatabaseManager {

    private static DatabaseManager instance;

    private final Properties prop = new Properties();

    private DatabaseManager() {
        try (InputStream input = getClass().getResourceAsStream("/db.properties")) {

            if (input == null) {
                throw new IllegalStateException("File db.properties non trovato nella cartella src.");
            }

            prop.load(input);

            Class.forName("com.mysql.cj.jdbc.Driver");

            System.out.println("Configurazione DB caricata correttamente.");

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il caricamento della configurazione DB.", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Restituisce una nuova connessione ogni volta.
     * Non riutilizza connessioni già chiuse.
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    prop.getProperty("db.url"),
                    prop.getProperty("db.user"),
                    prop.getProperty("db.password")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'apertura della connessione al DB.", e);
        }
    }
}