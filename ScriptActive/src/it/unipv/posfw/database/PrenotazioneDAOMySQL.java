package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.unipv.posfw.dao.PrenotazioneDAO;
import it.unipv.posfw.util.DatabaseManager; // Usiamo la connessione che abbiamo testato insieme!

public class PrenotazioneDAOMySQL implements PrenotazioneDAO {

    @Override
    public boolean esistePrenotazione(String idCliente, String idCorso) {
        String query = "SELECT COUNT(1) FROM Prenotazione WHERE ID_Cliente = ? AND ID_Corso = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, idCliente);
            pstmt.setString(2, idCorso);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore DAO (esistePrenotazione): " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean inserisciPrenotazione(String idCliente, String idCorso) {
        String query = "INSERT INTO Prenotazione (ID_Cliente, ID_Corso) VALUES (?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, idCliente);
            pstmt.setString(2, idCorso);
            
            int righeModificate = pstmt.executeUpdate();
            return righeModificate > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore DAO (inserisciPrenotazione): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminaPrenotazione(String idCliente, String idCorso) {
        String query = "DELETE FROM Prenotazione WHERE ID_Cliente = ? AND ID_Corso = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, idCliente);
            pstmt.setString(2, idCorso);
            
            int righeModificate = pstmt.executeUpdate();
            return righeModificate > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore DAO (eliminaPrenotazione): " + e.getMessage());
            return false;
        }
    }
}