package it.unipv.posfw.service;

import it.unipv.posfw.exceptions.SostitutoNonValidoException;
import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServizioSwapCorsiMySQL implements ServizioSwapCorsi {

    @Override
    public boolean haCorsiAttiviOFuturi(String idTrainer) {
        String sql = """
            SELECT COUNT(*) AS totale
            FROM corso
            WHERE id_trainer_assegnato = ?
              AND stato = 'ATTIVO'
              AND data_ora >= NOW()
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, idTrainer);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totale") > 0;
                }
            }

            return false;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il controllo dei corsi attivi/futuri.", e);
        }
    }

    @Override
    public boolean haCorsiImminenti(String idTrainer) {
        String sql = """
            SELECT COUNT(*) AS totale
            FROM corso
            WHERE id_trainer_assegnato = ?
              AND stato = 'ATTIVO'
              AND data_ora BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 24 HOUR)
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, idTrainer);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totale") > 0;
                }
            }

            return false;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il controllo dei corsi imminenti.", e);
        }
    }

    @Override
    public int sostituisciTrainerNeiCorsi(String idTrainerDaSostituire, String idTrainerSostituto)
            throws SostitutoNonValidoException {

        String sqlSwap = """
            UPDATE corso
            SET id_trainer_assegnato = ?
            WHERE id_trainer_assegnato = ?
              AND stato = 'ATTIVO'
              AND data_ora >= NOW()
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!esisteTrainerAttivo(conn, idTrainerSostituto)) {
                    throw new SostitutoNonValidoException("Il sostituto non esiste o non è attivo.");
                }

                if (sostitutoHaSovrapposizioni(conn, idTrainerDaSostituire, idTrainerSostituto)) {
                    throw new SostitutoNonValidoException(
                            "OPERAZIONE ANNULLATA: il sostituto ha già un corso assegnato nello stesso orario."
                    );
                }

                int righeAggiornate;

                try (PreparedStatement stmt = conn.prepareStatement(sqlSwap)) {
                    stmt.setString(1, idTrainerSostituto);
                    stmt.setString(2, idTrainerDaSostituire);

                    righeAggiornate = stmt.executeUpdate();
                }

                conn.commit();
                return righeAggiornate;

            } catch (SostitutoNonValidoException e) {
                conn.rollback();
                throw e;

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Errore durante lo swap dei corsi.", e);
            }

        } catch (SostitutoNonValidoException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Errore di connessione durante lo swap dei corsi.", e);
        }
    }

    private boolean esisteTrainerAttivo(Connection conn, String idTrainer) throws Exception {
        String sql = """
            SELECT COUNT(*) AS totale
            FROM personal_trainer
            WHERE id_trainer = ?
              AND stato_contratto = 'ATTIVO'
              AND attivo = TRUE
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idTrainer);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totale") > 0;
                }
            }
        }

        return false;
    }

    private boolean sostitutoHaSovrapposizioni(
            Connection conn,
            String idTrainerDaSostituire,
            String idTrainerSostituto) throws Exception {

        String sql = """
            SELECT COUNT(*) AS totale
            FROM corso corsoDaRiassegnare
            JOIN corso corsoDelSostituto
                ON corsoDaRiassegnare.data_ora = corsoDelSostituto.data_ora
            WHERE corsoDaRiassegnare.id_trainer_assegnato = ?
              AND corsoDelSostituto.id_trainer_assegnato = ?
              AND corsoDaRiassegnare.stato = 'ATTIVO'
              AND corsoDelSostituto.stato = 'ATTIVO'
              AND corsoDaRiassegnare.data_ora >= NOW()
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idTrainerDaSostituire);
            stmt.setString(2, idTrainerSostituto);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totale") > 0;
                }
            }
        }

        return false;
    }
}