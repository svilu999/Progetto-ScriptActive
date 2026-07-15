package it.unipv.poingsfw.strategy;

import java.util.Locale;

import it.unipv.poingsfw.exceptions.TrainerNonValidoException;

/**
 * Factory responsabile della creazione delle strategie di retribuzione.
 *
 * La classe centralizza la scelta della Strategy concreta, evitando che
 * Controller, View e Service conoscano direttamente le implementazioni.
 */
public final class StrategiaRetribuzioneFactory {

    private static final String TIPO_FISSA_MENSILE =
            "FISSA_MENSILE";

    private static final String TIPO_A_LEZIONE =
            "A_LEZIONE";

    private StrategiaRetribuzioneFactory() {
    }

    /**
     * Crea la strategia corrispondente al tipo di retribuzione indicato.
     *
     * @param tipoRetribuzione tipo della strategia da creare
     * @param importo importo associato alla strategia
     * @return strategia di retribuzione configurata
     * @throws TrainerNonValidoException se il tipo o l'importo non sono validi
     */
    public static StrategiaRetribuzione crea(
            String tipoRetribuzione,
            double importo)
            throws TrainerNonValidoException {

        if (tipoRetribuzione == null
                || tipoRetribuzione.isBlank()) {

            throw new TrainerNonValidoException(
                    "Tipo di retribuzione non indicato."
            );
        }

        if (importo < 0) {
            throw new TrainerNonValidoException(
                    "L'importo della retribuzione "
                    + "non può essere negativo."
            );
        }

        String tipoNormalizzato =
                tipoRetribuzione
                        .trim()
                        .toUpperCase(Locale.ROOT);

        return switch (tipoNormalizzato) {
            case TIPO_FISSA_MENSILE ->
                    new RetribuzioneFissa(importo);

            case TIPO_A_LEZIONE ->
                    new RetribuzioneProvvigione(importo);

            default ->
                    throw new TrainerNonValidoException(
                            "Tipo di retribuzione non riconosciuto: "
                            + tipoRetribuzione
                            + "."
                    );
        };
    }
}