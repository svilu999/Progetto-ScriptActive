package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.unipv.posfw.domain.Sede;

public class SedeDAOMySQL {

    // ==========================================
    // 1. METODO PER INSERIRE UNA NUOVA SEDE (CREATE)
    // ==========================================
    public void aggiungiSede(String nomeSede) {
        String query = "INSERT INTO Sede (NomeSede) VALUES (?)";

        try {
            // Prendiamo la connessione FUORI dal try-with-resources per NON farla chiudere!
            Connection conn = DatabaseConnection.getConnection();
            
            // Mettiamo nel try-with-resources solo il PreparedStatement
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, nomeSede);
                pstmt.executeUpdate();
                System.out.println("Sede '" + nomeSede + "' salvata con successo in MySQL!");
            }
            
        } catch (SQLException e) {
            System.out.println("Errore durante l'inserimento della sede: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. METODO PER LEGGERE LE SEDI (READ - TEST IN CONSOLE)
    // ==========================================
    public void stampaTutteLeSedi() {
        String query = "SELECT ID_Sede, NomeSede FROM Sede";

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                System.out.println("--- ELENCO SEDI DAL DATABASE ---");
                while (rs.next()) {
                    int id = rs.getInt("ID_Sede");
                    String nome = rs.getString("NomeSede");
                    System.out.println("ID: " + id + " | Nome: " + nome);
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Errore in stampaTutteLeSedi: " + e.getMessage());
        }
    }
    
    // ==========================================
    // 3. METODO PER LA TUA VIEW (Il Menu a tendina usa questo)
    // ==========================================
    public List<Sede> getTutteLeSedi() {
        List<Sede> listaSedi = new ArrayList<>();
        String query = "SELECT ID_Sede, NomeSede FROM Sede";

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    Sede s = new Sede(rs.getInt("ID_Sede"), rs.getString("NomeSede"));
                    listaSedi.add(s);
                }
            }
        } catch (SQLException e) {
            System.out.println("Errore in getTutteLeSedi: " + e.getMessage());
        }
        return listaSedi;
    }

    // ==========================================
    // TEST AL VOLO
    // ==========================================
    public static void main(String[] args) {
        SedeDAOMySQL dao = new SedeDAOMySQL();
        
        // Fai una prova con un nome sicuramente NUOVO per evitare il Duplicate Entry
        dao.aggiungiSede("Palestra Milano Nord");
        
        // Leggiamo se l'ha salvata
        dao.stampaTutteLeSedi();
    }
}