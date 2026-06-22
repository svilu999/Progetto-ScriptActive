package it.unipv.poingsfw.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import it.unipv.poingsfw.util.DatabaseManager;

/**
 * Implementazione MySQL dell'interfaccia ServizioRetribuzioni.
 *
 * La classe calcola il costo mensile complessivo dei PersonalTrainer attivi
 * leggendo i dati retributivi dalla tabella PersonalTrainer e, quando necessario,
 * i corsi completati dalla tabella Corso.
 *
 * I dati retributivi considerati sono:
 * - TipoRetribuzione;
 * - StipendioMensile;
 * - CompensoPerLezione.
 */
public class ServizioRetribuzioniMySQL implements ServizioRetribuzioni {

    /**
     * Calcola il totale mensile delle retribuzioni dei PersonalTrainer attivi.
     *
     * Per i trainer con retribuzione fissa viene considerato lo stipendio mensile.
     * Per i trainer retribuiti a lezione vengono contati i corsi completati nel
     * mese corrente e moltiplicati per il compenso previsto per lezione.
     *
     * @return totale mensile delle retribuzioni dei PersonalTrainer attivi
     */
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