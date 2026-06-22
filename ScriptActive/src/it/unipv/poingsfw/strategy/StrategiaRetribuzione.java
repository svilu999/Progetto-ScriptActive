package it.unipv.poingsfw.strategy;

/**
 * Interfaccia Strategy per il calcolo della retribuzione di un PersonalTrainer.
 *
 * L'interfaccia permette di separare la classe PersonalTrainer dagli algoritmi
 * concreti di calcolo dello stipendio. Ogni implementazione definisce una
 * diversa modalità retributiva.
 */
public interface StrategiaRetribuzione {

    /**
     * Calcola lo stipendio in base al numero di lezioni o corsi fornito.
     *
     * @param numeroLezioni numero di lezioni da considerare nel calcolo
     * @return importo dello stipendio calcolato
     */
    double calcolaStipendio(int numeroLezioni);

    /**
     * Restituisce il tipo di contratto associato alla strategia.
     *
     * @return descrizione del tipo di contratto
     */
    String getTipoContratto();
}
