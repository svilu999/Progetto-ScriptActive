package it.unipv.poingsfw.service;

import java.util.List;
import java.util.Objects;

import it.unipv.poingsfw.dao.RetribuzioniDAO;
import it.unipv.poingsfw.dto.DatiRetribuzioneTrainer;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.strategy.StrategiaRetribuzione;
import it.unipv.poingsfw.strategy.StrategiaRetribuzioneFactory;

/**
 * Service applicativo dedicato al calcolo delle retribuzioni mensili.
 *
 * La classe contiene la logica di calcolo delle retribuzioni e delega al DAO
 * solo il recupero dei dati necessari dal database.
 */
public class GestoreRetribuzioni implements ServizioRetribuzioni {

    private static final String TIPO_FISSA_MENSILE = "FISSA_MENSILE";
    private static final String TIPO_A_LEZIONE = "A_LEZIONE";

    private final RetribuzioniDAO retribuzioniDAO;

    /**
     * Crea il service per il calcolo delle retribuzioni.
     *
     * @param retribuzioniDAO DAO usato per recuperare i dati retributivi
     */
    public GestoreRetribuzioni(RetribuzioniDAO retribuzioniDAO) {
        this.retribuzioniDAO = Objects.requireNonNull(
                retribuzioniDAO,
                "retribuzioniDAO non può essere null"
        );
    }

    /**
     * Calcola il totale mensile delle retribuzioni dei Personal Trainer attivi.
     *
     * Il DAO recupera i dati dal database, mentre questo Service applica la
     * logica di calcolo tramite il pattern Strategy.
     *
     * @return totale mensile delle retribuzioni
     */
    @Override
    public double calcolaTotaleRetribuzioniMensili() {
        List<DatiRetribuzioneTrainer> datiRetribuzioni =
                retribuzioniDAO.recuperaDatiRetribuzioniMensili();

        double totale = 0.0;

        for (DatiRetribuzioneTrainer datiTrainer : datiRetribuzioni) {
            totale += calcolaRetribuzioneTrainer(datiTrainer);
        }

        return totale;
    }

    /**
     * Calcola la retribuzione mensile di un singolo Personal Trainer.
     *
     * @param datiTrainer dati necessari al calcolo retributivo
     * @return retribuzione mensile calcolata
     */
    private double calcolaRetribuzioneTrainer(DatiRetribuzioneTrainer datiTrainer) {
        String tipoRetribuzione = datiTrainer.getTipoRetribuzione();
        double importoBase = selezionaImportoBase(datiTrainer);

        try {
            StrategiaRetribuzione strategia = StrategiaRetribuzioneFactory.crea(
                    tipoRetribuzione,
                    importoBase
            );

            return strategia.calcolaStipendio(
                    datiTrainer.getNumeroLezioniCompletate()
            );

        } catch (TrainerNonValidoException e) {
            throw new RuntimeException(
                    "Errore durante la creazione della strategia retributiva.",
                    e
            );
        }
    }

    /**
     * Seleziona l'importo da usare per creare la strategia retributiva.
     *
     * Per la retribuzione fissa viene usato lo stipendio mensile. Per la
     * retribuzione a lezione viene usato il compenso per lezione.
     *
     * @param datiTrainer dati retributivi del Personal Trainer
     * @return importo base da passare alla Strategy Factory
     */
    private double selezionaImportoBase(DatiRetribuzioneTrainer datiTrainer) {
        String tipoRetribuzione = datiTrainer.getTipoRetribuzione();

        if (TIPO_FISSA_MENSILE.equals(tipoRetribuzione)) {
            return datiTrainer.getStipendioMensile();
        }

        if (TIPO_A_LEZIONE.equals(tipoRetribuzione)) {
            return datiTrainer.getCompensoPerLezione();
        }

        throw new RuntimeException("Tipo retribuzione non valido: " + tipoRetribuzione);
    }
}