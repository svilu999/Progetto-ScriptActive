package it.unipv.poingsfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import it.unipv.poingsfw.dao.SwapCorsiDAO;
import it.unipv.poingsfw.util.DatabaseManager;

/**
 * Implementazione MySQL del DAO dedicato allo swap dei corsi.
 *
 * La classe contiene solo operazioni di persistenza: esegue query e update
 * sulla tabella Corso e sulla tabella PersonalTrainer. Le decisioni
 * applicative restano nel Service.
 */
public class SwapCorsiDAOMySQL implements SwapCorsiDAO {

    /**
     * Verifica se esistono corsi attivi o futuri per un trainer.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se esiste almeno un corso attivo o futuro, false altrimenti
     */
    @Override
    public boolean esistonoCorsiAttiviOFuturiPerTrainer(int idTrainer) {
        String sql = """
            SELECT COUNT(*) AS totale
            FROM Corso
            WHERE ID_Trainer = ?
              AND Stato IN ('Pianificato', 'InCorso')
              AND DataOra >= NOW()
        """;

        return esisteAlmenoUnRisultato(sql, idTrainer);
    }

    /**
     * Verifica se esistono corsi imminenti per un trainer.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se esiste almeno un corso imminente, false altrimenti
     */
    @Override
    public boolean esistonoCorsiImminentiPerTrainer(int idTrainer) {
        String sql = """
            SELECT COUNT(*) AS totale
            FROM Corso
            WHERE ID_Trainer = ?
              AND Stato IN ('Pianificato', 'InCorso')
              AND DataOra BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 24 HOUR)
        """;

        return esisteAlmenoUnRisultato(sql, idTrainer);
    }

    /**
     * Verifica se esiste un Personal Trainer attivo con contratto attivo.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se il trainer esiste ed è attivo, false altrimenti
     */
    @Override
    public boolean esisteTrainerConContrattoAttivo(int idTrainer) {
        String sql = """
            SELECT COUNT(*) AS totale
            FROM PersonalTrainer
            WHERE ID_Trainer = ?
              AND StatoContratto = 'ATTIVO'
              AND Attivo = TRUE
        """;

        return esisteAlmenoUnRisultato(sql, idTrainer);
    }

    /**
     * Verifica se il sostituto ha corsi sovrapposti rispetto ai corsi da riassegnare.
     *
     * @param idTrainerDaSostituire identificativo numerico del trainer da sostituire
     * @param idTrainerSostituto identificativo numerico del trainer sostituto
     * @return true se esiste almeno una sovrapposizione oraria, false altrimenti
     */
    @Override
    public boolean esistonoSovrapposizioniTraCorsi(
            int idTrainerDaSostituire,
            int idTrainerSostituto) {

        String sql = """
            SELECT COUNT(*) AS totale
            FROM Corso corsoDaRiassegnare
            JOIN Corso corsoDelSostituto
                ON corsoDaRiassegnare.DataOra = corsoDelSostituto.DataOra
            WHERE corsoDaRiassegnare.ID_Trainer = ?
              AND corsoDelSostituto.ID_Trainer = ?
              AND corsoDaRiassegnare.Stato IN ('Pianificato', 'InCorso')
              AND corsoDelSostituto.Stato IN ('Pianificato', 'InCorso')
              AND corsoDaRiassegnare.DataOra >= NOW()
              AND corsoDelSostituto.DataOra >= NOW()
              AND corsoDaRiassegnare.ID_Corso <> corsoDelSostituto.ID_Corso
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idTrainerDaSostituire);
            stmt.setInt(2, idTrainerSostituto);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt("totale") > 0;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Errore durante il controllo delle sovrapposizioni orarie.",
                    e
            );
        }
    }

    /**
     * Aggiorna i corsi attivi o futuri assegnandoli al trainer sostituto.
     *
     * @param idTrainerDaSostituire identificativo numerico del trainer da sostituire
     * @param idTrainerSostituto identificativo numerico del trainer sostituto
     * @return numero di corsi aggiornati
     */
    @Override
    public int riassegnaCorsiAttiviOFuturi(
            int idTrainerDaSostituire,
            int idTrainerSostituto) {

        String sql = """
            UPDATE Corso
            SET ID_Trainer = ?
            WHERE ID_Trainer = ?
              AND Stato IN ('Pianificato', 'InCorso')
              AND DataOra >= NOW()
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idTrainerSostituto);
            stmt.setInt(2, idTrainerDaSostituire);

            return stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Errore durante l'aggiornamento del trainer nei corsi.",
                    e
            );
        }
    }

    /**
     * Esegue una query di conteggio con un solo parametro numerico.
     *
     * @param sql query SQL da eseguire
     * @param parametro valore da impostare nel PreparedStatement
     * @return true se il conteggio restituito è maggiore di zero, false altrimenti
     */
    private boolean esisteAlmenoUnRisultato(String sql, int parametro) {
        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt("totale") > 0;
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'esecuzione della query di controllo.", e);
        }
    }
}