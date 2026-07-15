package it.unipv.poingsfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.unipv.poingsfw.dao.PersonalTrainerDAO;
import it.unipv.poingsfw.dto.DatiPersonalTrainer;
import it.unipv.poingsfw.util.DatabaseManager;

/**
 * Implementazione MySQL del DAO dei Personal Trainer.
 *
 * La classe contiene esclusivamente operazioni di persistenza:
 * apertura delle connessioni, esecuzione delle query, gestione delle
 * transazioni e conversione dei risultati in DTO.
 */
public class PersonalTrainerDAOMySQL
        implements PersonalTrainerDAO {

    /**
     * Salva un nuovo Personal Trainer.
     *
     * L'inserimento nelle tabelle Utente e PersonalTrainer viene eseguito
     * nella stessa transazione.
     *
     * @param datiTrainer dati persistenti del trainer
     */
    @Override
    public void salva(DatiPersonalTrainer datiTrainer) {
        Objects.requireNonNull(
                datiTrainer,
                "I dati del Personal Trainer non possono essere null."
        );

        try (Connection conn =
                DatabaseManager.getInstance().getConnection()) {

            try {
                conn.setAutoCommit(false);

                int idUtente =
                        inserisciUtente(
                                conn,
                                datiTrainer
                        );

                inserisciPersonalTrainer(
                        conn,
                        idUtente,
                        datiTrainer
                );

                conn.commit();

            } catch (Exception e) {
                eseguiRollback(conn, e);
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Errore durante il salvataggio "
                    + "del Personal Trainer su MySQL.",
                    e
            );
        }
    }

    /**
     * Cerca un Personal Trainer tramite identificativo.
     *
     * @param idTrainer identificativo numerico del trainer
     * @return dati del trainer oppure null
     */
    @Override
    public DatiPersonalTrainer trovaPerId(
            Integer idTrainer) {

        if (idTrainer == null || idTrainer <= 0) {
            return null;
        }

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
            Connection conn =
                    DatabaseManager
                            .getInstance()
                            .getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idTrainer);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creaDatiPersonalTrainerDaResultSet(
                            rs
                    );
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante la ricerca "
                    + "del Personal Trainer su MySQL.",
                    e
            );
        }
    }

    /**
     * Cerca un Personal Trainer tramite email.
     *
     * @param email email del trainer
     * @return dati del trainer oppure null
     */
    @Override
    public DatiPersonalTrainer trovaPerEmail(
            String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

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
            Connection conn =
                    DatabaseManager
                            .getInstance()
                            .getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {
            stmt.setString(
                    1,
                    email.trim()
            );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creaDatiPersonalTrainerDaResultSet(
                            rs
                    );
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante la ricerca del Personal Trainer "
                    + "tramite email su MySQL.",
                    e
            );
        }
    }

    /**
     * Aggiorna i dati di un Personal Trainer.
     *
     * Le modifiche alle tabelle Utente e PersonalTrainer vengono eseguite
     * nella stessa transazione.
     *
     * @param datiTrainer dati persistenti aggiornati
     */
    @Override
    public void aggiorna(
            DatiPersonalTrainer datiTrainer) {

        Objects.requireNonNull(
                datiTrainer,
                "I dati del Personal Trainer non possono essere null."
        );

        if (datiTrainer.getIdTrainer() == null) {
            throw new IllegalArgumentException(
                    "L'identificativo del Personal Trainer "
                    + "non può essere null."
            );
        }

        try (Connection conn =
                DatabaseManager.getInstance().getConnection()) {

            try {
                conn.setAutoCommit(false);

                int utentiAggiornati =
                        aggiornaUtente(
                                conn,
                                datiTrainer
                        );

                if (utentiAggiornati != 1) {
                    throw new SQLException(
                            "L'Utente collegato al Personal Trainer "
                            + "non è stato aggiornato."
                    );
                }

                int trainerAggiornati =
                        aggiornaPersonalTrainer(
                                conn,
                                datiTrainer
                        );

                if (trainerAggiornati != 1) {
                    throw new SQLException(
                            "Il Personal Trainer "
                            + "non è stato aggiornato."
                    );
                }

                conn.commit();

            } catch (Exception e) {
                eseguiRollback(conn, e);
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Errore durante l'aggiornamento "
                    + "del Personal Trainer su MySQL.",
                    e
            );
        }
    }

    /**
     * Disattiva logicamente un Personal Trainer.
     *
     * Il record non viene eliminato fisicamente. Il contratto viene impostato
     * come licenziato e l'account Utente viene disattivato.
     *
     * @param idTrainer identificativo numerico del trainer
     */
    @Override
    public void disattiva(
            Integer idTrainer) {

        if (idTrainer == null || idTrainer <= 0) {
            throw new IllegalArgumentException(
                    "L'identificativo del Personal Trainer "
                    + "non è valido."
            );
        }

        String sqlPersonalTrainer = """
            UPDATE PersonalTrainer
            SET StatoContratto = 'LICENZIATO',
                Attivo = FALSE
            WHERE ID_Trainer = ?
              AND StatoContratto = 'ATTIVO'
              AND Attivo = TRUE
        """;

        String sqlUtente = """
            UPDATE Utente
            SET Stato = 'Inattivo'
            WHERE ID_Utente = ?
              AND Stato = 'Attivo'
        """;

        try (Connection conn =
                DatabaseManager.getInstance().getConnection()) {

            try {
                conn.setAutoCommit(false);

                int trainerDisattivati;

                try (PreparedStatement stmt =
                        conn.prepareStatement(
                                sqlPersonalTrainer
                        )) {

                    stmt.setInt(
                            1,
                            idTrainer
                    );

                    trainerDisattivati =
                            stmt.executeUpdate();
                }

                if (trainerDisattivati != 1) {
                    throw new SQLException(
                            "Il Personal Trainer non esiste "
                            + "oppure risulta già disattivato."
                    );
                }

                int utentiDisattivati;

                try (PreparedStatement stmt =
                        conn.prepareStatement(
                                sqlUtente
                        )) {

                    stmt.setInt(
                            1,
                            idTrainer
                    );

                    utentiDisattivati =
                            stmt.executeUpdate();
                }

                if (utentiDisattivati != 1) {
                    throw new SQLException(
                            "L'account Utente del Personal Trainer "
                            + "non è stato disattivato."
                    );
                }

                conn.commit();

            } catch (Exception e) {
                eseguiRollback(conn, e);
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Errore durante la disattivazione "
                    + "del Personal Trainer su MySQL.",
                    e
            );
        }
    }

    /**
     * Restituisce tutti i Personal Trainer.
     *
     * @return lista dei dati persistenti
     */
    @Override
    public List<DatiPersonalTrainer> trovaTutti() {
        List<DatiPersonalTrainer> lista =
                new ArrayList<>();

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
            Connection conn =
                    DatabaseManager
                            .getInstance()
                            .getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    stmt.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(
                        creaDatiPersonalTrainerDaResultSet(
                                rs
                        )
                );
            }

            return lista;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante la lettura "
                    + "dei Personal Trainer da MySQL.",
                    e
            );
        }
    }

    /**
     * Inserisce l'account Utente del Personal Trainer.
     *
     * @param conn connessione della transazione
     * @param datiTrainer dati persistenti del trainer
     * @return identificativo generato
     * @throws SQLException se l'inserimento fallisce
     */
    private int inserisciUtente(
            Connection conn,
            DatiPersonalTrainer datiTrainer)
            throws SQLException {

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

        try (PreparedStatement stmt =
                conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )) {

            stmt.setString(
                    1,
                    datiTrainer.getCodiceFiscale()
            );

            stmt.setString(
                    2,
                    datiTrainer.getNome()
            );

            stmt.setString(
                    3,
                    datiTrainer.getCognome()
            );

            stmt.setString(
                    4,
                    datiTrainer.getEmail()
            );

            stmt.setString(
                    5,
                    datiTrainer.getPasswordHash()
            );

            stmt.setString(
                    6,
                    datiTrainer.getStatoUtente()
            );

            int righeInserite =
                    stmt.executeUpdate();

            if (righeInserite != 1) {
                throw new SQLException(
                        "L'account Utente non è stato inserito."
                );
            }

            try (ResultSet rs =
                    stmt.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new SQLException(
                    "Impossibile recuperare "
                    + "l'identificativo Utente generato."
            );
        }
    }

    /**
     * Inserisce il record PersonalTrainer.
     *
     * @param conn connessione della transazione
     * @param idUtente identificativo dell'utente
     * @param datiTrainer dati persistenti del trainer
     * @throws SQLException se l'inserimento fallisce
     */
    private void inserisciPersonalTrainer(
            Connection conn,
            int idUtente,
            DatiPersonalTrainer datiTrainer)
            throws SQLException {

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

        try (PreparedStatement stmt =
                conn.prepareStatement(sql)) {

            stmt.setInt(
                    1,
                    idUtente
            );

            stmt.setString(
                    2,
                    datiTrainer.getSpecializzazione()
            );

            stmt.setString(
                    3,
                    datiTrainer.getTipoContratto()
            );

            stmt.setString(
                    4,
                    datiTrainer.getStatoContratto()
            );

            stmt.setBoolean(
                    5,
                    datiTrainer.isAttivo()
            );

            stmt.setString(
                    6,
                    datiTrainer.getTipoRetribuzione()
            );

            stmt.setDouble(
                    7,
                    datiTrainer.getStipendioMensile()
            );

            if (datiTrainer.getCompensoPerLezione()
                    == null) {

                stmt.setNull(
                        8,
                        Types.DECIMAL
                );

            } else {
                stmt.setDouble(
                        8,
                        datiTrainer.getCompensoPerLezione()
                );
            }

            if (datiTrainer.getIdDirettore()
                    == null) {

                stmt.setNull(
                        9,
                        Types.INTEGER
                );

            } else {
                stmt.setInt(
                        9,
                        datiTrainer.getIdDirettore()
                );
            }

            int righeInserite =
                    stmt.executeUpdate();

            if (righeInserite != 1) {
                throw new SQLException(
                        "Il Personal Trainer non è stato inserito."
                );
            }
        }
    }

    /**
     * Aggiorna l'account Utente collegato.
     *
     * @param conn connessione della transazione
     * @param datiTrainer dati persistenti aggiornati
     * @return numero di righe aggiornate
     * @throws SQLException se l'aggiornamento fallisce
     */
    private int aggiornaUtente(
            Connection conn,
            DatiPersonalTrainer datiTrainer)
            throws SQLException {

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

        try (PreparedStatement stmt =
                conn.prepareStatement(sql)) {

            stmt.setString(
                    1,
                    datiTrainer.getCodiceFiscale()
            );

            stmt.setString(
                    2,
                    datiTrainer.getNome()
            );

            stmt.setString(
                    3,
                    datiTrainer.getCognome()
            );

            stmt.setString(
                    4,
                    datiTrainer.getEmail()
            );

            stmt.setString(
                    5,
                    datiTrainer.getPasswordHash()
            );

            stmt.setString(
                    6,
                    datiTrainer.getStatoUtente()
            );

            stmt.setInt(
                    7,
                    datiTrainer.getIdTrainer()
            );

            return stmt.executeUpdate();
        }
    }

    /**
     * Aggiorna il record PersonalTrainer.
     *
     * @param conn connessione della transazione
     * @param datiTrainer dati persistenti aggiornati
     * @return numero di righe aggiornate
     * @throws SQLException se l'aggiornamento fallisce
     */
    private int aggiornaPersonalTrainer(
            Connection conn,
            DatiPersonalTrainer datiTrainer)
            throws SQLException {

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

        try (PreparedStatement stmt =
                conn.prepareStatement(sql)) {

            stmt.setString(
                    1,
                    datiTrainer.getSpecializzazione()
            );

            stmt.setString(
                    2,
                    datiTrainer.getTipoContratto()
            );

            stmt.setString(
                    3,
                    datiTrainer.getStatoContratto()
            );

            stmt.setBoolean(
                    4,
                    datiTrainer.isAttivo()
            );

            stmt.setString(
                    5,
                    datiTrainer.getTipoRetribuzione()
            );

            stmt.setDouble(
                    6,
                    datiTrainer.getStipendioMensile()
            );

            if (datiTrainer.getCompensoPerLezione()
                    == null) {

                stmt.setNull(
                        7,
                        Types.DECIMAL
                );

            } else {
                stmt.setDouble(
                        7,
                        datiTrainer.getCompensoPerLezione()
                );
            }

            if (datiTrainer.getIdDirettore()
                    == null) {

                stmt.setNull(
                        8,
                        Types.INTEGER
                );

            } else {
                stmt.setInt(
                        8,
                        datiTrainer.getIdDirettore()
                );
            }

            stmt.setInt(
                    9,
                    datiTrainer.getIdTrainer()
            );

            return stmt.executeUpdate();
        }
    }

    /**
     * Costruisce un DTO leggendo la riga corrente del ResultSet.
     *
     * @param rs risultato della query
     * @return dati persistenti del trainer
     * @throws SQLException se la lettura fallisce
     */
    private DatiPersonalTrainer
            creaDatiPersonalTrainerDaResultSet(
                    ResultSet rs)
                    throws SQLException {

        Double compensoPerLezione =
                rs.getDouble(
                        "CompensoPerLezione"
                );

        if (rs.wasNull()) {
            compensoPerLezione = null;
        }

        Integer idDirettore =
                rs.getInt(
                        "ID_Direttore"
                );

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

    /**
     * Esegue il rollback preservando eventuali errori aggiuntivi.
     *
     * @param conn connessione della transazione
     * @param causa errore originale
     */
    private void eseguiRollback(
            Connection conn,
            Exception causa) {

        try {
            conn.rollback();

        } catch (SQLException rollbackException) {
            causa.addSuppressed(
                    rollbackException
            );
        }
    }
}