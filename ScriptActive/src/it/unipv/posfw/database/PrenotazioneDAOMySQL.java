package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.unipv.posfw.dao.PrenotazioneDAO;
import it.unipv.posfw.util.DatabaseManager;

public class PrenotazioneDAOMySQL implements PrenotazioneDAO {

    @Override
    public boolean esistePrenotazione(String idCliente, String idCorso) {
        String query = "SELECT COUNT(1) FROM Prenotazione WHERE ID_Cliente = ? AND ID_Corso = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        Connection conn = DatabaseManager.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        Connection conn = DatabaseManager.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idCliente);
            pstmt.setString(2, idCorso);
            
            int righeModificate = pstmt.executeUpdate();
            return righeModificate > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore DAO (eliminaPrenotazione): " + e.getMessage());
            return false;
        }
    }
    
 // --- METODI AGGIUNTIVI PER LISTA D'ATTESA ---

    @Override
    public boolean inserisciPrenotazione(String idCliente, String idCorso, String stato) {
        String query = "INSERT INTO Prenotazione (ID_Cliente, ID_Corso, Stato) VALUES (?, ?, ?)";
        Connection conn = it.unipv.posfw.util.DatabaseManager.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idCliente);
            pstmt.setString(2, idCorso);
            pstmt.setString(3, stato);
            
            int righeModificate = pstmt.executeUpdate();
            return righeModificate > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore DAO (inserisciPrenotazione con stato): " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getPrimoInListaAttesa(String idCorso) {

        String query = "SELECT ID_Cliente FROM Prenotazione WHERE ID_Corso = ? AND Stato = 'InListaAttesa' ORDER BY DataCreazione ASC LIMIT 1";
        
        Connection conn = it.unipv.posfw.util.DatabaseManager.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idCorso);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ID_Cliente"); 
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore DAO (getPrimoInListaAttesa): " + e.getMessage());
        }
        return null; 
    }

    @Override
    public boolean aggiornaStatoPrenotazione(String idCliente, String idCorso, String nuovoStato) {
        String query = "UPDATE Prenotazione SET Stato = ? WHERE ID_Cliente = ? AND ID_Corso = ?";
        Connection conn = it.unipv.posfw.util.DatabaseManager.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nuovoStato);
            pstmt.setString(2, idCliente);
            pstmt.setString(3, idCorso);
            
            int righeModificate = pstmt.executeUpdate();
            return righeModificate > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore DAO (aggiornaStatoPrenotazione): " + e.getMessage());
            return false;
        }
    }
}