package it.unipv.poingsfw.strategy;

/**
 * Strategy per il calcolo della retribuzione a lezione.
 *
 * La retribuzione dipende dal numero di lezioni completate e dal compenso
 * previsto per ogni lezione.
 */
public class RetribuzioneProvvigione
        implements StrategiaRetribuzione {

    private static final String TIPO_RETRIBUZIONE =
            "A_LEZIONE";

    private final double quotaPerLezione;

    /**
     * Crea una Strategy di retribuzione a lezione.
     *
     * @param quotaPerLezione compenso previsto per una singola lezione
     * @throws IllegalArgumentException se la quota è negativa
     */
    public RetribuzioneProvvigione(
            double quotaPerLezione) {

        if (quotaPerLezione < 0) {
            throw new IllegalArgumentException(
                    "La quota per lezione non può essere negativa."
            );
        }

        this.quotaPerLezione =
                quotaPerLezione;
    }

    /**
     * Calcola la retribuzione moltiplicando il numero di lezioni
     * per il compenso previsto.
     *
     * @param numeroLezioni numero di lezioni completate
     * @return retribuzione calcolata
     * @throws IllegalArgumentException se il numero di lezioni è negativo
     */
    @Override
    public double calcolaStipendio(
            int numeroLezioni) {

        if (numeroLezioni < 0) {
            throw new IllegalArgumentException(
                    "Il numero di lezioni non può essere negativo."
            );
        }

        return numeroLezioni
                * quotaPerLezione;
    }

    /**
     * Restituisce il tipo di retribuzione rappresentato dalla Strategy.
     *
     * @return tipo di retribuzione a lezione
     */
    @Override
    public String getTipoRetribuzione() {
        return TIPO_RETRIBUZIONE;
    }
}