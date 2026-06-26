package it.unipv.poingsfw.strategy;

import it.unipv.poingsfw.exceptions.TrainerNonValidoException;

/**
 * Factory per creare la strategia di retribuzione corretta.
 *
 * Serve a evitare che View o Controller conoscano direttamente
 * RetribuzioneFissa e RetribuzioneProvvigione.
 */
public final class StrategiaRetribuzioneFactory {

    private StrategiaRetribuzioneFactory() {
        // Costruttore privato perché la factory non deve essere istanziata.
    }

    public static StrategiaRetribuzione crea(String tipoRetribuzione, double importo)
            throws TrainerNonValidoException {

        if (tipoRetribuzione == null || tipoRetribuzione.trim().isEmpty()) {
            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: tipo di retribuzione non indicato.");
        }

        if (importo < 0) {
            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: l'importo della retribuzione non può essere negativo.");
        }

        String tipoNormalizzato = tipoRetribuzione.trim().toUpperCase();

        switch (tipoNormalizzato) {
            case "FISSA_MENSILE":
                return new RetribuzioneFissa(importo);

            case "A_LEZIONE":
                return new RetribuzioneProvvigione(importo);

            default:
                throw new TrainerNonValidoException(
                        "OPERAZIONE ANNULLATA: tipo di retribuzione non riconosciuto: "
                                + tipoRetribuzione + ".");
        }
    }
}
