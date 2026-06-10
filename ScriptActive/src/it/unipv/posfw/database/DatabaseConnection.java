package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // Parametri di configurazione
    private static final String URL = "jdbc:mysql://localhost:3306/scriptactive_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Caterina@75"; 

    // Variabile che memorizza l'unica connessione attiva
    private static Connection connection = null;

    // Metodo pubblico per ottenere la connessione
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Instauriamo la connessione fisica col server
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("VITTORIA! Connessione al database stabilita con successo.");
            } catch (SQLException e) {
                System.out.println("ERRORE: Impossibile connettersi al database.");
                e.printStackTrace();
            }
        }
        return connection;
    }
 // Test veloce per verificare la connessione
    public static void main(String[] args) {
        Connection testConn = DatabaseConnection.getConnection();
        if (testConn != null) {
            System.out.println("Tutto pronto, Ingegnere! Puoi iniziare a mandare query.");
        }
    }
}