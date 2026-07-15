package it.unipv.poingsfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.unipv.poingsfw.dao.SwapCorsiDAO;
import it.unipv.poingsfw.util.DatabaseManager;

/**
 * Implementazione MySQL del DAO dedicato allo swap dei corsi.
 *
 * La classe contiene esclusivamente operazioni di persistenza. Le verifiche
 * applicative e le decisioni sul licenziamento restano nei Service.
 */
public class SwapCorsiDAOMySQL implements SwapCorsiDAO {

    /**
     * Verifica se esistono corsi attivi o futuri per un trainer.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se esiste almeno un corso attivo o futuro
     */
    @Override
    public boolean esistonoCorsiAttiviOFuturiPerTrainer(
            int idTrainer) {

        String sql = """
            SELECT COUNT(*) AS totale
            FROM Corso
            WHERE ID_Trainer = ?
              AND (
                    Stato = 'InCorso'
                    OR (
                        Stato = 'Pianificato'
                        AND DataOra >= NOW()
                    )
              )
        """;

        return esisteAlmenoUnRisultato(
                sql,
                idTrainer
        );
    }

    /**
     * Verifica se esistono corsi imminenti per un trainer.
     *
     * Sono considerati imminenti i corsi già in corso e quelli pianificati
     * nelle successive ventiquattro ore.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se esiste almeno un corso imminente
     */
    @Override
    public boolean esistonoCorsiImminentiPerTrainer(
            int idTrainer) {

        String sql = """
            SELECT COUNT(*) AS totale
            FROM Corso
            WHERE ID_Trainer = ?
              AND (
                    Stato = 'InCorso'
                    OR (
                        Stato = 'Pianificato'
                        AND DataOra BETWEEN NOW()
                            AND DATE_ADD(NOW(), INTERVAL 24 HOUR)
                    )
              )
        """;

        return esisteAlmenoUnRisultato(
                sql,
                idTrainer
        );
    }

    /**
     * Verifica se esiste un Personal Trainer attivo con contratto attivo.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se il trainer esiste ed è attivo
     */
    @Override
    public boolean esisteTrainerConContrattoAttivo(
            int idTrainer) {

        String sql = """
            SELECT COUNT(*) AS totale
            FROM PersonalTrainer
            WHERE ID_Trainer = ?
              AND StatoContratto = 'ATTIVO'
              AND Attivo = TRUE
        """;

        return esisteAlmenoUnRisultato(
                sql,
                idTrainer
        );
    }

    /**
     * Verifica se il sostituto possiede corsi sovrapposti rispetto ai corsi
     * da riassegnare.
     *
     * @param idTrainerDaSostituire identificativo del trainer da sostituire
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @return true se esiste almeno una sovrapposizione
     */
    @Override
    public boolean esistonoSovrapposizioniTraCorsi(
            int idTrainerDaSostituire,
            int idTrainerSostituto) {

        String sql = """
            SELECT COUNT(*) AS totale
            FROM Corso corsoDaRiassegnare
            JOIN Corso corsoDelSostituto
                ON corsoDaRiassegnare.DataOra =
                   corsoDelSostituto.DataOra
            WHERE corsoDaRiassegnare.ID_Trainer = ?
              AND corsoDelSostituto.ID_Trainer = ?
              AND (
                    corsoDaRiassegnare.Stato = 'InCorso'
                    OR (
                        corsoDaRiassegnare.Stato = 'Pianificato'
                        AND corsoDaRiassegnare.DataOra >= NOW()
                    )
              )
              AND (
                    corsoDelSostituto.Stato = 'InCorso'
                    OR (
                        corsoDelSostituto.Stato = 'Pianificato'
                        AND corsoDelSostituto.DataOra >= NOW()
                    )
              )
              AND corsoDaRiassegnare.ID_Corso <>
                  corsoDelSostituto.ID_Corso
        """;

        try (
            Connection conn =
                    DatabaseManager
                            .getInstance()
                            .getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    idTrainerDaSostituire
            );

            stmt.setInt(
                    2,
                    idTrainerSostituto
            );

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next()
                        && rs.getInt("totale") > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il controllo "
                    + "delle sovrapposizioni orarie.",
                    e
            );
        }
    }

    /**
     * Riassegna i corsi al sostituto e disattiva il trainer sostituito.
     *
     * Le modifiche alle tabelle Corso, PersonalTrainer e Utente vengono
     * eseguite sulla stessa connessione e nella stessa transazione.
     *
     * In caso di errore tutte le modifiche vengono annullate.
     *
     * @param idTrainerDaSostituire identificativo del trainer da disattivare
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @return numero di corsi riassegnati
     */
    @Override
    public int riassegnaCorsiEDisattivaTrainer(
            int idTrainerDaSostituire,
            int idTrainerSostituto) {

        try (
            Connection conn =
                    DatabaseManager
                            .getInstance()
                            .getConnection()
        ) {
            try {
                conn.setAutoCommit(false);

                int corsiRiassegnati =
                        riassegnaCorsi(
                                conn,
                                idTrainerDaSostituire,
                                idTrainerSostituto
                        );

                int trainerDisattivati =
                        disattivaPersonalTrainer(
                                conn,
                                idTrainerDaSostituire
                        );

                if (trainerDisattivati != 1) {
                    throw new SQLException(
                            "Il Personal Trainer non è stato "
                            + "disattivato correttamente."
                    );
                }

                int utentiDisattivati =
                        disattivaUtente(
                                conn,
                                idTrainerDaSostituire
                        );

                if (utentiDisattivati != 1) {
                    throw new SQLException(
                            "L'account Utente del Personal Trainer "
                            + "non è stato disattivato correttamente."
                    );
                }

                conn.commit();

                return corsiRiassegnati;

            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(
                            rollbackException
                    );
                }

                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Errore durante lo swap dei corsi "
                    + "e la disattivazione del Personal Trainer.",
                    e
            );
        }
    }

    /**
     * Riassegna i corsi attivi o futuri al trainer sostituto.
     *
     * @param conn connessione della transazione corrente
     * @param idTrainerDaSostituire trainer da sostituire
     * @param idTrainerSostituto trainer sostituto
     * @return numero di corsi aggiornati
     * @throws SQLException se l'aggiornamento fallisce
     */
    private int riassegnaCorsi(
            Connection conn,
            int idTrainerDaSostituire,
            int idTrainerSostituto)
            throws SQLException {

        String sql = """
            UPDATE Corso
            SET ID_Trainer = ?
            WHERE ID_Trainer = ?
              AND (
                    Stato = 'InCorso'
                    OR (
                        Stato = 'Pianificato'
                        AND DataOra >= NOW()
                    )
              )
        """;

        try (
            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    idTrainerSostituto
            );

            stmt.setInt(
                    2,
                    idTrainerDaSostituire
            );

            return stmt.executeUpdate();
        }
    }

    /**
     * Disattiva il contratto del Personal Trainer.
     *
     * @param conn connessione della transazione corrente
     * @param idTrainer identificativo del trainer
     * @return numero di righe aggiornate
     * @throws SQLException se l'aggiornamento fallisce
     */
    private int disattivaPersonalTrainer(
            Connection conn,
            int idTrainer)
            throws SQLException {

        String sql = """
            UPDATE PersonalTrainer
            SET StatoContratto = 'LICENZIATO',
                Attivo = FALSE
            WHERE ID_Trainer = ?
              AND StatoContratto = 'ATTIVO'
              AND Attivo = TRUE
        """;

        try (
            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    idTrainer
            );

            return stmt.executeUpdate();
        }
    }

    /**
     * Disattiva l'account Utente collegato al Personal Trainer.
     *
     * @param conn connessione della transazione corrente
     * @param idTrainer identificativo del trainer
     * @return numero di righe aggiornate
     * @throws SQLException se l'aggiornamento fallisce
     */
    private int disattivaUtente(
            Connection conn,
            int idTrainer)
            throws SQLException {

        String sql = """
            UPDATE Utente
            SET Stato = 'Inattivo'
            WHERE ID_Utente = ?
        """;

        try (
            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    idTrainer
            );

            return stmt.executeUpdate();
        }
    }

    /**
     * Esegue una query di conteggio con un parametro numerico.
     *
     * @param sql query SQL da eseguire
     * @param parametro parametro della query
     * @return true se il conteggio è maggiore di zero
     */
    private boolean esisteAlmenoUnRisultato(
            String sql,
            int parametro) {

        try (
            Connection conn =
                    DatabaseManager
                            .getInstance()
                            .getConnection();

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    parametro
            );

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next()
                        && rs.getInt("totale") > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante l'esecuzione "
                    + "della query di controllo.",
                    e
            );
        }
    }
}