package it.unipv.poingsfw.strategy;

/**
 * Strategia usata per calcolare una retribuzione a provvigione.
 *
 * In questa strategia lo stipendio dipende dal numero di lezioni svolte
 * e dal compenso previsto per ogni lezione.
 */
public class RetribuzioneProvvigione implements StrategiaRetribuzione {

    private double quotaPerLezione;

    /**
     * Crea una retribuzione a provvigione indicando il compenso per ogni lezione.
     *
     * @param quotaPerLezione compenso riconosciuto per una singola lezione
     */
    public RetribuzioneProvvigione(double quotaPerLezione) {
        this.quotaPerLezione = quotaPerLezione;
    }

    /**
     * Calcola lo stipendio moltiplicando il numero di lezioni per la quota prevista.
     *
     * @param numeroLezioni numero di lezioni svolte
     * @return stipendio calcolato in base alle lezioni svolte
     */
    @Override
    public double calcolaStipendio(int numeroLezioni) {
        return numeroLezioni * quotaPerLezione;
    }

    /**
     * Restituisce il tipo di contratto usato da questa strategia.
     *
     * @return tipo di contratto
     */
    @Override
    public String getTipoContratto() {
        return "Provvigione";
    }
}
