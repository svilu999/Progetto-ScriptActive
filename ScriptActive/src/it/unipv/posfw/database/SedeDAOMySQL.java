package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SedeDAOMySQL {

    // ==========================================
    // 1. METODO PER INSERIRE UNA NUOVA SEDE (CREATE)
    // ==========================================
    public void aggiungiSede(String nomeSede) {
        // La query SQL. Usiamo il "?" per sicurezza (evita la SQL Injection!)
        String query = "INSERT INTO Sede (NomeSede) VALUES (?)";

        // Otteniamo la connessione dal nostro Singleton
        Connection conn = DatabaseConnection.getConnection();

        try {
            // PreparedStatement prepara la query e la blinda
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            // Sostituiamo il primo (e unico) "?" con il nome della sede
            pstmt.setString(1, nomeSede);
            
            // Eseguiamo la modifica nel database
            pstmt.executeUpdate();
            System.out.println("Sede '" + nomeSede + "' salvata con successo in MySQL!");
            
            pstmt.close(); // Buona pratica: chiudere sempre lo statement
        } catch (SQLException e) {
            System.out.println("Errore durante l'inserimento della sede.");
            e.printStackTrace();
        }
    }

    // ==========================================
    // 2. METODO PER LEGGERE LE SEDI (READ)
    // ==========================================
    public void stampaTutteLeSedi() {
        String query = "SELECT ID_Sede, NomeSede FROM Sede";
        Connection conn = DatabaseConnection.getConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            // executeQuery() si usa per le SELECT e restituisce una "tabella" di risultati (ResultSet)
            ResultSet rs = pstmt.executeQuery();

            System.out.println("--- ELENCO SEDI DAL DATABASE ---");
            // Scorriamo le righe del risultato finché ce ne sono
            while (rs.next()) {
                int id = rs.getInt("ID_Sede");
                String nome = rs.getString("NomeSede");
                System.out.println("ID: " + id + " | Nome: " + nome);
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // TEST AL VOLO
    // ==========================================
    public static void main(String[] args) {
        SedeDAOMySQL dao = new SedeDAOMySQL();
        
        // 1. Inseriamo un paio di sedi di prova
        dao.aggiungiSede("Palestra Rozzano");
        dao.aggiungiSede("Palestra Lodi");
        
        // 2. Leggiamole per vedere se le ha salvate davvero!
        dao.stampaTutteLeSedi();
    }
}