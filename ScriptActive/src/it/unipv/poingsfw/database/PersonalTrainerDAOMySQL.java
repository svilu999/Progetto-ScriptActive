package it.unipv.poingsfw.database;

import it.unipv.poingsfw.dao.PersonalTrainerDAO;
import it.unipv.poingsfw.dto.DatiPersonalTrainer;
import it.unipv.poingsfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione MySQL del DAO dei Personal Trainer.
 *
 * La classe si occupa solo di accesso al database: apertura della connessione,
 * query SQL, scrittura dei dati e lettura dei ResultSet.
 */
public class PersonalTrainerDAOMySQL implements PersonalTrainerDAO {

    /**
     * Salva un nuovo Personal Trainer nel database.
     *
     * Il metodo inserisce prima il record nella tabella Utente e poi il record
     * corrispondente nella tabella PersonalTrainer, usando la stessa transazione.
     *
     * @param datiTrainer dati persistenti del Personal Trainer da salvare
     */
    @Override
    public void salva(DatiPersonalTrainer datiTrainer) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try {
                conn.setAutoCommit(false);

                int idUtente = inserisciUtente(conn, datiTrainer);
                inserisciPersonalTrainer(conn, idUtente, datiTrainer);

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il salvataggio del Personal Trainer su MySQL.", e);
        }
    }

    /**
     * Cerca un Personal Trainer tramite identificativo numerico.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return dati del Personal Trainer trovato, oppure null se non esiste
     */
    @Override
    public DatiPersonalTrainer trovaPerId(Integer idTrainer) {
        String sql = """
            SELECT
                pt.ID_Trainer,
                u.CodiceFiscale,
                u.Nome,
                u.Cognome,
                u.Email,
                u.PasswordHash,
                u.Stato AS StatoUtente,
                pt.Specializzazione,
                pt.TipoContratto,
                pt.StatoContratto,
                pt.Attivo,
                pt.TipoRetribuzione,
                pt.StipendioMensile,
                pt.CompensoPerLezione,
                pt.ID_Direttore
            FROM PersonalTrainer pt
            JOIN Utente u
                ON pt.ID_Trainer = u.ID_Utente
            WHERE pt.ID_Trainer = ?
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idTrainer);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creaDatiPersonalTrainerDaResultSet(rs);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca del Personal Trainer su MySQL.", e);
        }
    }

    /**
     * Cerca un Personal Trainer tramite email.
     *
     * @param email email del Personal Trainer da cercare
     * @return dati del Personal Trainer trovato, oppure null se non esiste
     */
    @Override
    public DatiPersonalTrainer trovaPerEmail(String email) {
        String sql = """
            SELECT
                pt.ID_Trainer,
                u.CodiceFiscale,
                u.Nome,
                u.Cognome,
                u.Email,
                u.PasswordHash,
                u.Stato AS StatoUtente,
                pt.Specializzazione,
                pt.TipoContratto,
                pt.StatoContratto,
                pt.Attivo,
                pt.TipoRetribuzione,
                pt.StipendioMensile,
                pt.CompensoPerLezione,
                pt.ID_Direttore
            FROM PersonalTrainer pt
            JOIN Utente u
                ON pt.ID_Trainer = u.ID_Utente
            WHERE LOWER(u.Email) = LOWER(?)
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creaDatiPersonalTrainerDaResultSet(rs);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca del Personal Trainer tramite email su MySQL.", e);
        }
    }

    /**
     * Aggiorna i dati persistenti di un Personal Trainer.
     *
     * @param datiTrainer dati persistenti aggiornati del Personal Trainer
     */
    @Override
    public void aggiorna(DatiPersonalTrainer datiTrainer) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try {
                conn.setAutoCommit(false);

                aggiornaUtente(conn, datiTrainer);
                aggiornaPersonalTrainer(conn, datiTrainer);

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'aggiornamento del Personal Trainer su MySQL.", e);
        }
    }

    /**
     * Disattiva logicamente un Personal Trainer.
     *
     * @param idTrainer identificativo numerico del Personal Trainer da disattivare
     */
    @Override
    public void elimina(Integer idTrainer) {
        String sqlPersonalTrainer = """
            UPDATE PersonalTrainer
            SET StatoContratto = 'LICENZIATO',
                Attivo = FALSE
            WHERE ID_Trainer = ?
        """;

        String sqlUtente = """
            UPDATE Utente
            SET Stato = 'Inattivo'
            WHERE ID_Utente = ?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try {
                conn.setAutoCommit(false);

                try (PreparedStatement stmt = conn.prepareStatement(sqlPersonalTrainer)) {
                    stmt.setInt(1, idTrainer);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlUtente)) {
                    stmt.setInt(1, idTrainer);
                    stmt.executeUpdate();
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la disattivazione del Personal Trainer su MySQL.", e);
        }
    }

    /**
     * Restituisce tutti i Personal Trainer presenti nel database.
     *
     * @return lista dei dati persistenti dei Personal Trainer
     */
    @Override
    public List<DatiPersonalTrainer> trovaTutti() {
        List<DatiPersonalTrainer> lista = new ArrayList<>();

        String sql = """
            SELECT
                pt.ID_Trainer,
                u.CodiceFiscale,
                u.Nome,
                u.Cognome,
                u.Email,
                u.PasswordHash,
                u.Stato AS StatoUtente,
                pt.Specializzazione,
                pt.TipoContratto,
                pt.StatoContratto,
                pt.Attivo,
                pt.TipoRetribuzione,
                pt.StipendioMensile,
                pt.CompensoPerLezione,
                pt.ID_Direttore
            FROM PersonalTrainer pt
            JOIN Utente u
                ON pt.ID_Trainer = u.ID_Utente
            ORDER BY pt.ID_Trainer
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(creaDatiPersonalTrainerDaResultSet(rs));
            }

            return lista;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la lettura dei Personal Trainer da MySQL.", e);
        }
    }

    /**
     * Inserisce un record nella tabella Utente.
     *
     * @param conn connessione attiva
     * @param datiTrainer dati persistenti del trainer
     * @return ID_Utente generato dal database
     * @throws Exception se si verifica un errore SQL
     */
    private int inserisciUtente(Connection conn, DatiPersonalTrainer datiTrainer) throws Exception {
        String sql = """
            INSERT INTO Utente (
                CodiceFiscale,
                Nome,
                Cognome,
                Email,
                PasswordHash,
                Ruolo,
                Stato
            )
            VALUES (?, ?, ?, ?, ?, 'PersonalTrainer', ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, datiTrainer.getCodiceFiscale());
            stmt.setString(2, datiTrainer.getNome());
            stmt.setString(3, datiTrainer.getCognome());
            stmt.setString(4, datiTrainer.getEmail());
            stmt.setString(5, datiTrainer.getPasswordHash());
            stmt.setString(6, datiTrainer.getStatoUtente());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }

                throw new RuntimeException("Impossibile recuperare ID_Utente generato.");
            }
        }
    }

    /**
     * Inserisce un record nella tabella PersonalTrainer.
     *
     * @param conn connessione attiva
     * @param idUtente identificativo dell'utente collegato al trainer
     * @param datiTrainer dati persistenti del trainer
     * @throws Exception se si verifica un errore SQL
     */
    private void inserisciPersonalTrainer(
            Connection conn,
            int idUtente,
            DatiPersonalTrainer datiTrainer) throws Exception {

        String sql = """
            INSERT INTO PersonalTrainer (
                ID_Trainer,
                Specializzazione,
                TipoContratto,
                StatoContratto,
                Attivo,
                TipoRetribuzione,
                StipendioMensile,
                CompensoPerLezione,
                ID_Direttore
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
            stmt.setString(2, datiTrainer.getSpecializzazione());
            stmt.setString(3, datiTrainer.getTipoContratto());
            stmt.setString(4, datiTrainer.getStatoContratto());
            stmt.setBoolean(5, datiTrainer.isAttivo());
            stmt.setString(6, datiTrainer.getTipoRetribuzione());
            stmt.setDouble(7, datiTrainer.getStipendioMensile());

            if (datiTrainer.getCompensoPerLezione() == null) {
                stmt.setNull(8, java.sql.Types.DECIMAL);
            } else {
                stmt.setDouble(8, datiTrainer.getCompensoPerLezione());
            }

            if (datiTrainer.getIdDirettore() == null) {
                stmt.setNull(9, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(9, datiTrainer.getIdDirettore());
            }

            stmt.executeUpdate();
        }
    }

    /**
     * Aggiorna un record nella tabella Utente.
     *
     * @param conn connessione attiva
     * @param datiTrainer dati persistenti del trainer
     * @throws Exception se si verifica un errore SQL
     */
    private void aggiornaUtente(Connection conn, DatiPersonalTrainer datiTrainer) throws Exception {
        String sql = """
            UPDATE Utente
            SET CodiceFiscale = ?,
                Nome = ?,
                Cognome = ?,
                Email = ?,
                PasswordHash = ?,
                Stato = ?
            WHERE ID_Utente = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, datiTrainer.getCodiceFiscale());
            stmt.setString(2, datiTrainer.getNome());
            stmt.setString(3, datiTrainer.getCognome());
            stmt.setString(4, datiTrainer.getEmail());
            stmt.setString(5, datiTrainer.getPasswordHash());
            stmt.setString(6, datiTrainer.getStatoUtente());
            stmt.setInt(7, datiTrainer.getIdTrainer());

            stmt.executeUpdate();
        }
    }

    /**
     * Aggiorna un record nella tabella PersonalTrainer.
     *
     * @param conn connessione attiva
     * @param datiTrainer dati persistenti del trainer
     * @throws Exception se si verifica un errore SQL
     */
    private void aggiornaPersonalTrainer(Connection conn, DatiPersonalTrainer datiTrainer) throws Exception {
        String sql = """
            UPDATE PersonalTrainer
            SET Specializzazione = ?,
                TipoContratto = ?,
                StatoContratto = ?,
                Attivo = ?,
                TipoRetribuzione = ?,
                StipendioMensile = ?,
                CompensoPerLezione = ?,
                ID_Direttore = ?
            WHERE ID_Trainer = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, datiTrainer.getSpecializzazione());
            stmt.setString(2, datiTrainer.getTipoContratto());
            stmt.setString(3, datiTrainer.getStatoContratto());
            stmt.setBoolean(4, datiTrainer.isAttivo());
            stmt.setString(5, datiTrainer.getTipoRetribuzione());
            stmt.setDouble(6, datiTrainer.getStipendioMensile());

            if (datiTrainer.getCompensoPerLezione() == null) {
                stmt.setNull(7, java.sql.Types.DECIMAL);
            } else {
                stmt.setDouble(7, datiTrainer.getCompensoPerLezione());
            }

            if (datiTrainer.getIdDirettore() == null) {
                stmt.setNull(8, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(8, datiTrainer.getIdDirettore());
            }

            stmt.setInt(9, datiTrainer.getIdTrainer());

            stmt.executeUpdate();
        }
    }

    /**
     * Costruisce un DTO leggendo una riga del ResultSet.
     *
     * @param rs ResultSet posizionato sulla riga corrente
     * @return DTO con i dati persistenti del Personal Trainer
     * @throws Exception se si verifica un errore di lettura
     */
    private DatiPersonalTrainer creaDatiPersonalTrainerDaResultSet(ResultSet rs) throws Exception {
        Double compensoPerLezione = rs.getDouble("CompensoPerLezione");

        if (rs.wasNull()) {
            compensoPerLezione = null;
        }

        Integer idDirettore = rs.getInt("ID_Direttore");

        if (rs.wasNull()) {
            idDirettore = null;
        }

        return new DatiPersonalTrainer(
                rs.getInt("ID_Trainer"),
                rs.getString("CodiceFiscale"),
                rs.getString("Nome"),
                rs.getString("Cognome"),
                rs.getString("Email"),
                rs.getString("PasswordHash"),
                rs.getString("StatoUtente"),
                rs.getString("Specializzazione"),
                rs.getString("TipoContratto"),
                rs.getString("StatoContratto"),
                rs.getBoolean("Attivo"),
                rs.getString("TipoRetribuzione"),
                rs.getDouble("StipendioMensile"),
                compensoPerLezione,
                idDirettore
        );
    }
}