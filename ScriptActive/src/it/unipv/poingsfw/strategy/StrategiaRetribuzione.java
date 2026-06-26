package it.unipv.poingsfw.strategy;

/**
 * Interfaccia Strategy per il calcolo della retribuzione di un Personal Trainer.
 *
 * Permette di separare il calcolo della retribuzione dalle altre classi del
 * dominio. Ogni implementazione rappresenta una diversa modalità retributiva.
 */
public interface StrategiaRetribuzione {

    /**
     * Calcola la retribuzione in base al numero di lezioni considerate.
     *
     * @param numeroLezioni numero di lezioni da considerare nel calcolo
     * @return importo della retribuzione calcolata
     */
    double calcolaStipendio(int numeroLezioni);

    /**
     * Restituisce il tipo di retribuzione rappresentato dalla strategia.
     *
     * @return tipo di retribuzione della strategia
     */
    String getTipoRetribuzione();
}