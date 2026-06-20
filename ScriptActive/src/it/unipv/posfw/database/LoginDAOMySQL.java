package it.unipv.posfw.database; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.unipv.posfw.dao.LoginDAO; 
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.domain.Utente;
import it.unipv.posfw.util.DatabaseManager;

/**
 * Implementazione concreta dell'interfaccia {@link LoginDAO} per il DBMS MySQL.
 * <p>
 * Applica il pattern <b>Data Access Object (DAO)</b> per incapsulare le operazioni di 
 * estrazione e validazione delle credenziali. La classe maschera la complessità dell'accesso 
 * JDBC e implementa un <b>Object-Relational Mapping (ORM)</b> manuale, traducendo le tuple 
 * relazionali nelle corrispondenti entità polimorfiche di dominio (es. {@link Cliente}).
 * </p>
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Supporta funzionalmente lo Use Case di <b>Login</b>, fornendo il meccanismo di base per 
 * il riconoscimento dell'attore e l'innesco del successivo instradamento (Routing/Double Dispatch).
 * </p>
 * * @author Vilucchi
 * @version 1.1
 * @see it.unipv.posfw.dao.LoginDAO
 * @see it.unipv.posfw.util.DatabaseManager
 */
public class LoginDAOMySQL implements LoginDAO {

    /**
     * Verifica le credenziali di accesso interrogando il database relazionale.
     * <p>
     * Utilizza query precompilate ({@link PreparedStatement}) per mitigare le vulnerabilità 
     * legate alla <b>SQL Injection</b>. Sfrutta il costrutto <i>try-with-resources</i> per garantire 
     * il rilascio deterministico delle risorse di rete e di memoria (connessioni, statement, result set),
     * prevenendo potenziali <i>Memory/Connection Leaks</i>.
     * </p>
     * * @param email    L'indirizzo email fornito dall'utente.
     * @param password La password fornita dall'utente per la validazione.
     * @return L'istanza dell'entità polimorfica {@link Utente} se l'autenticazione ha esito positivo; {@code null} in caso contrario.
     */
    @Override
    public Utente verificaCredenziali(String email, String password) {
        Utente utenteTrovato = null;
        
        /* * Definizione della query parametrica. 
         * Utilizza clausole LEFT JOIN per risolvere le dipendenze strutturali (es. Livello Abbonamento) 
         * in una singola operazione di I/O verso il database, ottimizzando i tempi di fetch.
         */
        String sql = "SELECT u.Nome, u.Cognome, u.Email, u.CodiceFiscale, u.Ruolo, a.Livello AS LivelloAbbonamento " +
                     "FROM Utente u " +
                     "LEFT JOIN Cliente c ON u.ID_Utente = c.ID_Cliente " +
                     "LEFT JOIN Abbonamento a ON c.ID_Cliente = a.ID_Cliente " +
                     "WHERE u.Email = ? AND u.PasswordHash = ? AND u.Stato = 'Attivo'";

        /* * Acquisizione della connessione dal Singleton DatabaseManager e 
         * gestione automatica del ciclo di vita delle risorse (Context Management).
         */
        try (Connection conn = DatabaseManager.getInstance().getConnection();
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

                    /* * Object-Relational Mapping (ORM) logico.
                     * Determina il tipo dinamico dell'entità da istanziare basandosi sul campo discriminante 'Ruolo'.
                     */
                    if ("Cliente".equals(ruolo)) {
                        TipoAbbonamento tipoAbb = TipoAbbonamento.BASE; 
                        String livello = rs.getString("LivelloAbbonamento");
                        
                        if ("Premium".equalsIgnoreCase(livello)) {
                            tipoAbb = TipoAbbonamento.PREMIUM;
                        }
                        
                        utenteTrovato = new Cliente(nome, cognome, mail, codFiscale, tipoAbb);
                    } 
                    /*
                     * Punto di Estensione Architetturale (Open/Closed Principle).
                     * Nel rispetto del pattern di mapping polimorfico, le istanziazioni per le 
                     * sottoclassi Direttore e PersonalTrainer andranno implementate qui come diramazioni aggiuntive.
                     */
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore di connessione o esecuzione query nel layer DAO: " + e.getMessage());
            e.printStackTrace();
        }

        return utenteTrovato; 
    }
}