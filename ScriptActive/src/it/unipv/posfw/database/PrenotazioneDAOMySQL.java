package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import it.unipv.posfw.dao.PrenotazioneDAO;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.PersonalTrainer;
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
        Connection conn = DatabaseManager.getInstance().getConnection();
        
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
        Connection conn = DatabaseManager.getInstance().getConnection();
        
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
        Connection conn = DatabaseManager.getInstance().getConnection();
        
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

    // ========================================================
    // NUOVO METODO AGGIUNTO PER LA DASHBOARD CLIENTE
    // ========================================================
    @Override
    public List<Corso> getCorsiPerCliente(String idCliente) {
        List<Corso> corsiPrenotati = new ArrayList<>();
        
        // Fai attenzione ai nomi delle colonne: se nel tuo DB si chiamano diversamente (es. data_ora), modificali qui!
        String query = "SELECT c.ID_Corso, c.Nome, c.DataOra, c.PostiDisponibili, c.ID_Trainer " +
                     "FROM Corso c " +
                     "JOIN Prenotazione p ON c.ID_Corso = p.ID_Corso " +
                     "WHERE p.ID_Cliente = ? AND p.Stato = 'Confermata'"; 

        Connection conn = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idCliente);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String idCorso = rs.getString("ID_Corso");
                    String nomeCorso = rs.getString("Nome");
                    java.time.LocalDateTime dataOra = rs.getTimestamp("DataOra").toLocalDateTime(); 
                    int posti = rs.getInt("PostiDisponibili");
                    String idTrainer = rs.getString("ID_Trainer");

                    // Creiamo un oggetto finto del Trainer per associarlo al corso senza fare query pesanti
                    PersonalTrainer trainer = new PersonalTrainer("Trainer", "", "", idTrainer);
                    
                    Corso corso = new Corso(idCorso, nomeCorso, dataOra, posti, trainer);
                    corsiPrenotati.add(corso);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore DAO (getCorsiPerCliente): " + e.getMessage());
            e.printStackTrace();
        }

        return corsiPrenotati;
    }
}
