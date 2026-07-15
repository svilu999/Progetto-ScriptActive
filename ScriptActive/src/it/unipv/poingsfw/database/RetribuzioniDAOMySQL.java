package it.unipv.poingsfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.dao.RetribuzioniDAO;
import it.unipv.poingsfw.dto.DatiRetribuzioneTrainer;
import it.unipv.poingsfw.util.DatabaseManager;

/**
 * Implementazione MySQL del DAO dedicato alle retribuzioni.
 *
 * La classe recupera dal database solo i dati necessari al calcolo
 * retributivo. Il calcolo del totale mensile resta responsabilità del Service.
 */
public class RetribuzioniDAOMySQL implements RetribuzioniDAO {

    /**
     * Recupera i dati retributivi dei Personal Trainer attivi.
     *
     * Per ogni Personal Trainer attivo vengono letti il tipo di retribuzione,
     * gli importi configurati e il numero di corsi completati nel mese corrente.
     *
     * @return lista dei dati necessari al calcolo delle retribuzioni
     */
    @Override
    public List<DatiRetribuzioneTrainer> recuperaDatiRetribuzioniMensili() {
        String sql = """
            SELECT
                pt.TipoRetribuzione,
                COALESCE(pt.StipendioMensile, 0) AS StipendioMensile,
                COALESCE(pt.CompensoPerLezione, 0) AS CompensoPerLezione,
                COUNT(c.ID_Corso) AS NumeroLezioniCompletate
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
        """;

        List<DatiRetribuzioneTrainer> datiRetribuzioni = new ArrayList<>();

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                DatiRetribuzioneTrainer datiTrainer = new DatiRetribuzioneTrainer(
                        rs.getString("TipoRetribuzione"),
                        rs.getDouble("StipendioMensile"),
                        rs.getDouble("CompensoPerLezione"),
                        rs.getInt("NumeroLezioniCompletate")
                );

                datiRetribuzioni.add(datiTrainer);
            }

            return datiRetribuzioni;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Errore durante il recupero dei dati retributivi da MySQL.",
                    e
            );
        }
    }
}