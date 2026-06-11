package it.unipv.posfw.service;

import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Implementazione MySQL del servizio per il calcolo delle retribuzioni.
 *
 * Versione allineata allo schema comune scriptactive_db.
 *
 * I dati retributivi sono salvati direttamente nella tabella PersonalTrainer:
 * - TipoRetribuzione
 * - StipendioMensile
 * - CompensoPerLezione
 *
 * Non viene usata la vecchia tabella contratto_personale.
 */
public class ServizioRetribuzioniMySQL implements ServizioRetribuzioni {

    @Override
    public double calcolaTotaleRetribuzioniMensili() {

        /*
         * Regole:
         *
         * FISSA_MENSILE:
         * usa direttamente PersonalTrainer.StipendioMensile.
         *
         * A_LEZIONE:
         * conta i corsi completati nel mese corrente e moltiplica
         * per PersonalTrainer.CompensoPerLezione.
         *
         * Sono considerati solo i PT attivi e con contratto ATTIVO.
         */
        String sql = """
            SELECT
                COALESCE(SUM(costo_mensile), 0) AS totale_retribuzioni
            FROM (
                SELECT
                    pt.ID_Trainer,
                    CASE
                        WHEN pt.TipoRetribuzione = 'FISSA_MENSILE'
                            THEN pt.StipendioMensile

                        WHEN pt.TipoRetribuzione = 'A_LEZIONE'
                            THEN COUNT(c.ID_Corso) * COALESCE(pt.CompensoPerLezione, 0)

                        ELSE 0
                    END AS costo_mensile
                FROM PersonalTrainer pt
                LEFT JOIN Corso c
                    ON pt.ID_Trainer = c.ID_Trainer
                   AND c.Stato = 'Completato'
                   AND MONTH(c.DataOra) = MONTH(CURRENT_DATE)
                   AND YEAR(c.DataOra) = YEAR(CURRENT_DATE)
                WHERE pt.StatoContratto = 'ATTIVO'
                  AND pt.Attivo = TRUE
                GROUP BY
                    pt.ID_Trainer,
                    pt.TipoRetribuzione,
                    pt.StipendioMensile,
                    pt.CompensoPerLezione
            ) AS costi
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getDouble("totale_retribuzioni");
            }

            return 0.0;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il calcolo delle retribuzioni mensili da MySQL.", e);
        }
    }
}