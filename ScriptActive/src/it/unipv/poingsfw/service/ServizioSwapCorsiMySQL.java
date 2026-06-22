package it.unipv.poingsfw.service;

import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Implementazione MySQL dell'interfaccia ServizioSwapCorsi.
 *
 * La classe contiene la logica concreta per verificare i corsi assegnati a un
 * PersonalTrainer e per sostituire il trainer nei corsi attivi o futuri.
 *
 * Viene usata da GestorePersonale per evitare che un PersonalTrainer venga
 * disattivato lasciando corsi senza istruttore.
 */
public class ServizioSwapCorsiMySQL implements ServizioSwapCorsi {

    /**
     * Verifica se il PersonalTrainer indicato ha corsi attivi o futuri.
     *
     * Il metodo controlla la tabella Corso e considera solo i corsi con stato
     * compatibile e data successiva o uguale al momento corrente.
     *
     * @param idTrainer identificativo del PersonalTrainer da controllare
     * @return true se esiste almeno un corso attivo o futuro, false altrimenti
     */
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

    /**
     * Verifica se il PersonalTrainer indicato ha corsi imminenti.
     *
     * Un corso viene considerato imminente se è programmato tra il momento
     * corrente e le successive 24 ore.
     *
     * @param idTrainer identificativo del PersonalTrainer da controllare
     * @return true se esiste almeno un corso imminente, false altrimenti
     */
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

    /**
     * Sostituisce un PersonalTrainer nei corsi attivi o futuri.
     *
     * Il metodo verifica la validità del sostituto, controlla eventuali
     * sovrapposizioni orarie e aggiorna i corsi assegnandoli al nuovo trainer.
     * I corsi non vengono cancellati.
     *
     * @param idTrainerDaSostituire identificativo del PersonalTrainer da sostituire
     * @param idTrainerSostituto identificativo del PersonalTrainer sostituto
     * @return numero di corsi aggiornati
     * @throws SostitutoNonValidoException se il sostituto non è valido o ha sovrapposizioni
     */
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
         * Swap:
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

    /**
     * Verifica se esiste un PersonalTrainer attivo con contratto attivo.
     *
     * @param conn connessione database attiva
     * @param idTrainer identificativo numerico del PersonalTrainer
     * @return true se il trainer esiste ed è attivo, false altrimenti
     * @throws Exception se si verifica un errore durante la query
     */
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

    /**
     * Verifica se il PersonalTrainer sostituto ha corsi sovrapposti.
     *
     * Il metodo confronta gli orari dei corsi del trainer da sostituire con gli
     * orari dei corsi già assegnati al sostituto. Se trova una coincidenza, lo
     * swap viene bloccato.
     *
     * @param conn connessione database attiva
     * @param idTrainerDaSostituire identificativo del trainer da sostituire
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @return true se esiste almeno una sovrapposizione, false altrimenti
     * @throws Exception se si verifica un errore durante la query
     */
    private boolean sostitutoHaSovrapposizioni(
            Connection conn,
            int idTrainerDaSostituire,
            int idTrainerSostituto) throws Exception {

        /*
         * Controllo di sovrapposizione dei corsi:
         *
         * Il sostituto non può prendere i corsi del PT licenziato
         * se ha già un altro corso attivo/futuro nello stesso identico orario.
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
              AND corsoDelSostituto.DataOra >= NOW()
              AND corsoDaRiassegnare.ID_Corso <> corsoDelSostituto.ID_Corso
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

    /**
     * Estrae la parte numerica da un identificativo testuale.
     *
     * @param id identificativo da convertire
     * @return identificativo numerico, oppure null se il valore non è valido
     */
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