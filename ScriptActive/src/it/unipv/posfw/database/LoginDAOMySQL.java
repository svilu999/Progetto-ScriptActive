package it.unipv.posfw.database; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Importiamo l'interfaccia
import it.unipv.posfw.dao.LoginDAO; 

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.domain.Utente;

public class LoginDAOMySQL implements LoginDAO {

    

    @Override
    public Utente verificaCredenziali(String email, String password) {
        Utente utenteTrovato = null;
        
        String sql = "SELECT u.Nome, u.Cognome, u.Email, u.CodiceFiscale, u.Ruolo, a.Livello AS LivelloAbbonamento " +
                     "FROM Utente u " +
                     "LEFT JOIN Cliente c ON u.ID_Utente = c.ID_Cliente " +
                     "LEFT JOIN Abbonamento a ON c.ID_Cliente = a.ID_Cliente " +
                     "WHERE u.Email = ? AND u.PasswordHash = ? AND u.Stato = 'Attivo'";

      // Chiamiamo DatabaseConnection.getConnection() che legge dal tuo file properties!
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String ruolo = rs.getString("Ruolo");
                    String nome = rs.getString("Nome");
                    String cognome = rs.getString("Cognome");
                    String codFiscale = rs.getString("CodiceFiscale");
                    String mail = rs.getString("Email");

                    // In base al ruolo, costruiamo l'oggetto corretto
                    if ("Cliente".equals(ruolo)) {
                        TipoAbbonamento tipoAbb = TipoAbbonamento.BASE; // Default
                        String livello = rs.getString("LivelloAbbonamento");
                        
                        if ("Premium".equalsIgnoreCase(livello)) {
                            tipoAbb = TipoAbbonamento.PREMIUM;
                        }
                        
                        utenteTrovato = new Cliente(nome, cognome, mail, codFiscale, tipoAbb);
                    } 
                    // Qui in futuro potrai aggiungere gli "else if" per Direttore e PersonalTrainer!
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore di connessione o query nel login: " + e.getMessage());
            e.printStackTrace();
        }

        return utenteTrovato; 
    }
}