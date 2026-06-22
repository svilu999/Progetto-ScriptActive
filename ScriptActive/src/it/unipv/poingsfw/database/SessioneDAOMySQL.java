package it.unipv.poingsfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.unipv.poingsfw.dao.SessioneDAO;
import it.unipv.poingsfw.domain.DatiFormPojo;
import it.unipv.poingsfw.domain.SessioneAllenamento;
import it.unipv.poingsfw.util.DatabaseManager; 

/**
 * Implementazione concreta dell'interfaccia {@link SessioneDAO} per il DBMS MySQL.
 * <p>
 * Sfrutta il pattern <b>Data Access Object (DAO)</b> per astrarre la logica di memorizzazione persistente 
 * dello storico allenamenti. La classe incapsula le istruzioni SQL (DML e DQL) e si appoggia 
 * al Singleton {@link DatabaseManager} per l'acquisizione delle connessioni JDBC.
 * </p>
 * <p>
 * Integra ottimizzazioni architetturali quali le operazioni di <b>Batch Execution</b> per l'inserimento 
 * multiplo degli esercizi e l'uso di {@link LinkedHashMap} per la preservazione dell'ordinamento 
 * cronologico in fase di data-retrieval.
 * </p>
 * * @author Vilucchi
 * @version 1.1
 * @see it.unipv.poingsfw.dao.SessioneDAO
 */
public class SessioneDAOMySQL implements SessioneDAO {

    
    public SessioneDAOMySQL() {
        
    }

    /**
     * Persiste in maniera transazionale un aggregato di dominio (Sessione e relativi Esercizi).
     * <p>
     * Garantisce l'<b>Atomicità</b> disabilitando l'autocommit ({@code conn.setAutoCommit(false)}).
     * Utilizza la tecnica del <i>Batch Insert</i> ({@code addBatch()} / {@code executeBatch()}) per minimizzare 
     * il numero di round-trip di rete verso il database durante la registrazione di schede voluminose.
     * </p>
     * * @param sessione L'entità aggregata da persistere.
     * @return {@code true} se la transazione viene committata con successo, {@code false} in caso di rollback.
     */
    @Override
    public boolean salvaSessione(SessioneAllenamento sessione) {
        String sqlSessione = "INSERT INTO SessioneAllenamento (Data, ID_Cliente) VALUES (?, (SELECT ID_Utente FROM Utente WHERE CodiceFiscale = ?))";
        String sqlEsercizio = "INSERT INTO EsercizioRegistrato (GruppoMuscolare, Macchinario, Serie, Ripetizioni, Carico, ID_Sessione) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtSess = conn.prepareStatement(sqlSessione, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmtSess.setDate(1, new java.sql.Date(sessione.getData().getTime()));
                pstmtSess.setString(2, sessione.getIdCliente()); 
                pstmtSess.executeUpdate();

                int idSessioneGenerato = -1;
                /* Recupero della Primary Key generata dal DBMS per stabilire la relazione (Foreign Key) */
                try (ResultSet generatedKeys = pstmtSess.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idSessioneGenerato = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione sessione fallita, nessun ID ottenuto.");
                    }
                }

                /* Ottimizzazione: Batch Execution per le entità deboli (Esercizi) */
                try (PreparedStatement pstmtEsercizio = conn.prepareStatement(sqlEsercizio)) {
                    for (DatiFormPojo esercizio : sessione.getEsercizi()) {
                        pstmtEsercizio.setString(1, "Generico"); 
                        pstmtEsercizio.setString(2, esercizio.getNomeEsercizio()); 
                        pstmtEsercizio.setInt(3, 1); 
                        pstmtEsercizio.setInt(4, esercizio.getRipetizioni());
                        pstmtEsercizio.setDouble(5, esercizio.getCarichi());
                        pstmtEsercizio.setInt(6, idSessioneGenerato); 
                        pstmtEsercizio.addBatch(); 
                    }
                    pstmtEsercizio.executeBatch(); 
                }

                conn.commit(); 
                return true;

            } catch (SQLException ex) {
                conn.rollback(); 
                System.err.println("Errore SQL, eseguo ROLLBACK durante il salvataggio della sessione: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Estrae dal database lo storico degli allenamenti ricostruendo la gerarchia degli oggetti.
     * <p>
     * Effettua un'unica interrogazione relazionale (JOIN) delegando al motore SQL il joinning e 
     * l'ordinamento (ORDER BY). Successivamente applica l'<b>Object-Relational Mapping (ORM)</b> manuale, 
     * impiegando una {@link LinkedHashMap} per ricomporre la relazione One-to-Many (1 Sessione -> N Esercizi) 
     * senza perdere l'ordine cronologico richiesto dalla UI.
     * </p>
     * * @param idCliente Il Codice Fiscale dell'utente target.
     * @return La lista delle sessioni valorizzate, pronta per il rendering visivo.
     */
    @Override
    public List<SessioneAllenamento> getStorico(String idCliente) {
        Map<Integer, SessioneAllenamento> mappaSessioni = new LinkedHashMap<>();

        String sql = "SELECT s.ID_Sessione, s.Data, u.CodiceFiscale, e.Macchinario, e.Carico, e.Ripetizioni " +
                     "FROM SessioneAllenamento s " +
                     "JOIN Utente u ON s.ID_Cliente = u.ID_Utente " +
                     "LEFT JOIN EsercizioRegistrato e ON s.ID_Sessione = e.ID_Sessione " +
                     "WHERE u.CodiceFiscale = ? ORDER BY s.Data DESC, s.ID_Sessione DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idCliente); 

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int idSess = rs.getInt("ID_Sessione");

                    /* Istanziazione ritardata (Lazy) del contenitore principale se non presente in mappa */
                    if (!mappaSessioni.containsKey(idSess)) {
                        SessioneAllenamento sess = new SessioneAllenamento(rs.getDate("Data"), rs.getString("CodiceFiscale"));
                        sess.setIdSessione(idSess); 
                        mappaSessioni.put(idSess, sess);
                    }

                    /* Popolamento dell'aggregato con le entità deboli (DatiFormPojo) */
                    String macchinario = rs.getString("Macchinario");
                    if (macchinario != null) { 
                        DatiFormPojo esercizio = new DatiFormPojo(macchinario, rs.getDouble("Carico"), rs.getInt("Ripetizioni"));
                        mappaSessioni.get(idSess).aggiungiEsercizio(esercizio);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(mappaSessioni.values());
    }

    /**
     * Rimuove una sessione di allenamento (e in cascata i relativi esercizi, se previsto dai vincoli FK del DB).
     * <p>
     * Sfrutta il blocco <i>try-with-resources</i> per automatizzare la chiusura dei flussi JDBC (Connection e Statement).
     * </p>
     * * @param sessione L'oggetto di dominio contenente l'ID della sessione da eliminare.
     * @return {@code true} se la query intercetta e cancella il record corrispondente.
     */
    @Override
    public boolean eliminaSessioneSpecifica(SessioneAllenamento sessione) {
        String sql = "DELETE FROM SessioneAllenamento WHERE ID_Sessione = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessione.getIdSessione());
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}