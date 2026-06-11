package it.unipv.posfw.service;

import it.unipv.posfw.exceptions.SostitutoNonValidoException;
import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServizioSwapCorsiMySQL implements ServizioSwapCorsi {

    @Override
    public boolean haCorsiAttiviOFuturi(String idTrainer) {
        Integer idTrainerNumerico = estraiIdNumerico(idTrainer);

        if (idTrainerNumerico == null) {
            return false;
        }

        String sql = """
            SELECT COUNT(*) AS totale
            FROM Corso
            WHERE ID_Trainer = ?
              AND Stato IN ('Pianificato', 'InCorso')
              AND DataOra >= NOW()
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idTrainerNumerico);

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
        Integer idTrainerNumerico = estraiIdNumerico(idTrainer);

        if (idTrainerNumerico == null) {
            return false;
        }

        String sql = """
            SELECT COUNT(*) AS totale
            FROM Corso
            WHERE ID_Trainer = ?
              AND Stato IN ('Pianificato', 'InCorso')
              AND DataOra BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 24 HOUR)
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idTrainerNumerico);

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

        Integer idVecchioTrainer = estraiIdNumerico(idTrainerDaSostituire);
        Integer idNuovoTrainer = estraiIdNumerico(idTrainerSostituto);

        if (idVecchioTrainer == null || idNuovoTrainer == null) {
            throw new SostitutoNonValidoException("ID trainer non valido.");
        }

        if (idVecchioTrainer.equals(idNuovoTrainer)) {
            throw new SostitutoNonValidoException("Il sostituto non può coincidere con il PT da sostituire.");
        }

        /*
         * Swap UC5:
         * i corsi NON vengono cancellati.
         * Viene aggiornato solo ID_Trainer nella tabella Corso.
         */
        String sqlSwap = """
            UPDATE Corso
            SET ID_Trainer = ?
            WHERE ID_Trainer = ?
              AND Stato IN ('Pianificato', 'InCorso')
              AND DataOra >= NOW()
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!esisteTrainerAttivo(conn, idNuovoTrainer)) {
                    throw new SostitutoNonValidoException("Il sostituto non esiste o non è attivo.");
                }

                if (sostitutoHaSovrapposizioni(conn, idVecchioTrainer, idNuovoTrainer)) {
                    throw new SostitutoNonValidoException(
                            "OPERAZIONE ANNULLATA: il sostituto ha già un corso assegnato nello stesso orario."
                    );
                }

                int righeAggiornate;

                try (PreparedStatement stmt = conn.prepareStatement(sqlSwap)) {
                    stmt.setInt(1, idNuovoTrainer);
                    stmt.setInt(2, idVecchioTrainer);

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

    private boolean esisteTrainerAttivo(Connection conn, int idTrainer) throws Exception {
        String sql = """
            SELECT COUNT(*) AS totale
            FROM PersonalTrainer
            WHERE ID_Trainer = ?
              AND StatoContratto = 'ATTIVO'
              AND Attivo = TRUE
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTrainer);

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
            int idTrainerDaSostituire,
            int idTrainerSostituto) throws Exception {

        /*
         * Controlla se il sostituto ha già un corso nello stesso orario
         * di uno dei corsi futuri/attivi del PT da sostituire.
         */
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
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTrainerDaSostituire);
            stmt.setInt(2, idTrainerSostituto);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totale") > 0;
                }
            }
        }

        return false;
    }

    private Integer estraiIdNumerico(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        String soloNumeri = id.replaceAll("[^0-9]", "");

        if (soloNumeri.isBlank()) {
            return null;
        }

        return Integer.parseInt(soloNumeri);
    }
}