package it.unipv.posfw.util;

import java.sql.Connection;

public class TestConnessione {
    public static void main(String[] args) {
        // Richiama il DatabaseManager
        DatabaseManager db = DatabaseManager.getInstance();
        Connection conn = db.getConnection();

        if (conn != null) {
            System.out.println(" Connessione al database stabilita.");
        } else {
            System.out.println("Errore: Connessione fallita. Controlla db.properties.");
        }
    }
}
