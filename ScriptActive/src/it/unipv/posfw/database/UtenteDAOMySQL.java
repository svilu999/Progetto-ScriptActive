package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UtenteDAOMySQL implements UtenteDAO{

    // ==========================================
    // METODO PER REGISTRARE UN NUOVO CLIENTE
    // ==========================================
	@Override
    public void registraCliente(String cf, String nome, String cognome, String email, String passwordHash) {
        
        String insertUtente = "INSERT INTO Utente (CodiceFiscale, Nome, Cognome, Email, PasswordHash, Ruolo) VALUES (?, ?, ?, ?, ?, 'Cliente')";
        String insertCliente = "INSERT INTO Cliente (ID_Cliente) VALUES (?)";
        
        Connection conn = DatabaseConnection.getConnection();

        try {
            // 1. INIZIO TRANSAZIONE: Disabilitiamo l'autosalvataggio. 
            // Se qualcosa va storto, annulliamo tutto.
            conn.setAutoCommit(false); 

            // 2. Prepariamo la query Utente chiedendo a MySQL di restituirci l'ID generato
            PreparedStatement pstmtUtente = conn.prepareStatement(insertUtente, Statement.RETURN_GENERATED_KEYS);
            pstmtUtente.setString(1, cf);
            pstmtUtente.setString(2, nome);
            pstmtUtente.setString(3, cognome);
            pstmtUtente.setString(4, email);
            pstmtUtente.setString(5, passwordHash);
            
            // Eseguiamo l'inserimento dell'Utente
            pstmtUtente.executeUpdate();

            // 3. RECUPERIAMO L'ID GENERATO
            ResultSet rs = pstmtUtente.getGeneratedKeys();
            int idGenerato = -1;
            if (rs.next()) {
                idGenerato = rs.getInt(1); // Prendiamo il primo valore restituito
            }
            
            // 4. INSERIAMO IL CLIENTE USANDO L'ID APPENA RECUPERATO
            PreparedStatement pstmtCliente = conn.prepareStatement(insertCliente);
            pstmtCliente.setInt(1, idGenerato);
            pstmtCliente.executeUpdate();

            // 5. FINE TRANSAZIONE: Tutto è andato bene, salviamo definitivamente!
            conn.commit();
            System.out.println("Perfetto! Cliente '" + nome + " " + cognome + "' registrato con ID: " + idGenerato);

            // Pulizia
            rs.close();
            pstmtUtente.close();
            pstmtCliente.close();

        } catch (SQLException e) {
            // PIANO DI EMERGENZA: Se c'è un errore, annulliamo tutte le modifiche a metà
            System.out.println("Errore durante la registrazione. Rollback in corso...");
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            // Riattiviamo l'autocommit per le query normali future
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ==========================================
    // TEST AL VOLO
    // ==========================================
    public static void main(String[] args) {
        UtenteDAOMySQL dao = new UtenteDAOMySQL();
        
        // Simulo la registrazione di un cliente dalla tua schermata di registrazione
        dao.registraCliente("VRNLRN99M21F205W", "Lorenzo", "Varano", "lorenzo@studenti.unipv.it", "hash_finto_per_ora");
    }
}