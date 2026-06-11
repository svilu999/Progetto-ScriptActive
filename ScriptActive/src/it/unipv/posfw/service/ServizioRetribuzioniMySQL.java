package it.unipv.posfw.service;

import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Implementazione MySQL del servizio per il calcolo delle retribuzioni.
 *
 * Questa classe interroga direttamente il database e calcola il totale mensile
 * delle retribuzioni dei Personal Trainer attivi.
 *
 * Regole applicate:
 * - FISSA_MENSILE:
 *   usa il valore stipendio_mensile presente nel contratto attivo.
 *
 * - A_LEZIONE:
 *   conta i corsi COMPLETO del mese corrente e li moltiplica
 *   per compenso_per_lezione.
 *
 * GestorePersonale non deve contenere query SQL.
 * Per questo motivo il calcolo viene delegato a questo servizio.
 */
public class ServizioRetribuzioniMySQL implements ServizioRetribuzioni {

    @Override
    public double calcolaTotaleRetribuzioniMensili() {

        String sql = """
            SELECT 
                COALESCE(SUM(costo_mensile), 0) AS totale_retribuzioni
            FROM (
                SELECT 
                    cp.id_trainer,
                    CASE
                        WHEN cp.tipo_retribuzione = 'FISSA_MENSILE'
                            THEN cp.stipendio_mensile

                        WHEN cp.tipo_retribuzione = 'A_LEZIONE'
                            THEN COUNT(c.id_corso) * cp.compenso_per_lezione

                        ELSE 0
                    END AS costo_mensile
                FROM contratto_personale cp
                JOIN personal_trainer pt
                    ON cp.id_trainer = pt.id_trainer
                LEFT JOIN corso c
                    ON cp.id_trainer = c.id_trainer_assegnato
                   AND c.stato = 'COMPLETO'
                   AND MONTH(c.data_ora) = MONTH(CURRENT_DATE)
                   AND YEAR(c.data_ora) = YEAR(CURRENT_DATE)
                WHERE cp.stato = 'ATTIVO'
                  AND pt.stato_contratto = 'ATTIVO'
                  AND pt.attivo = TRUE
                GROUP BY 
                    cp.id_trainer,
                    cp.tipo_retribuzione,
                    cp.stipendio_mensile,
                    cp.compenso_per_lezione
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
