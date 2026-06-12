package it.unipv.posfw.database; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 2. Devi importare l'interfaccia LoginDAO perché si trova nel package "dao"
import it.unipv.posfw.dao.LoginDAO; 

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.domain.Utente;

public class LoginDAOSQL implements LoginDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/scriptactive_db";
    private static final String USER = "root";
    private static final String PASS = "Enomis23*"; // Metti la tua vera password!

    public LoginDAOSQL() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trovato!");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    @Override
    public Utente verificaCredenziali(String email, String password) {
        Utente utenteTrovato = null;
        
        // Cerca l'utente e, se è un cliente, prova a prendere anche i dati dell'abbonamento
        String sql = "SELECT u.Nome, u.Cognome, u.Email, u.CodiceFiscale, u.Ruolo, a.Livello AS LivelloAbbonamento " +
                     "FROM Utente u " +
                     "LEFT JOIN Cliente c ON u.ID_Utente = c.ID_Cliente " +
                     "LEFT JOIN Abbonamento a ON c.ID_Cliente = a.ID_Cliente " +
                     "WHERE u.Email = ? AND u.PasswordHash = ? AND u.Stato = 'Attivo'";

        try (Connection conn = getConnection();
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
                        
                        // Creiamo l'oggetto Cliente che abbiamo unificato prima
                        utenteTrovato = new Cliente(nome, cognome, mail, codFiscale, tipoAbb);
                        // (Se Utente non ha un metodo setRuolo, ricordati che il ruolo potresti doverlo gestire nella superclasse)
                        
                    } 
                    // Qui in futuro potrai aggiungere gli "else if" per Direttore e PersonalTrainer!
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return utenteTrovato; // Se non ha trovato nulla, restituisce null
    }
}