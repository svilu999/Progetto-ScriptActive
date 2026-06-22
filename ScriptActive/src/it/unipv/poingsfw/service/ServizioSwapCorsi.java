package it.unipv.poingsfw.service;

import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;

/**
 * Interfaccia di servizio per l'integrazione tra la gestione del personale
 * e il modulo dei corsi.
 *
 * Questa interfaccia permette al controller GestorePersonale di verificare
 * la presenza di corsi assegnati a un Personal Trainer e di richiedere la
 * sostituzione del trainer nei corsi attivi o futuri.
 */
public interface ServizioSwapCorsi {

    /**
     * Verifica se un Personal Trainer ha almeno un corso attivo o futuro.
     *
     * Il metodo viene usato nel licenziamento senza sostituto: se il trainer
     * ha corsi ancora rilevanti, il sistema deve impedire il licenziamento
     * diretto per evitare corsi senza istruttore.
     *
     * @param idTrainer identificativo del Personal Trainer da controllare
     * @return true se esiste almeno un corso attivo o futuro assegnato al trainer,
     *         false altrimenti
     */
    boolean haCorsiAttiviOFuturi(String idTrainer);

    /**
     * Verifica se un Personal Trainer ha corsi imminenti.
     *
     * Il concetto di corso imminente dipende dall'implementazione concreta.
     * Nel progetto viene usato per segnalare che lo swap deve essere gestito
     * con particolare attenzione prima della disattivazione del trainer.
     *
     * @param idTrainer identificativo del Personal Trainer da controllare
     * @return true se il trainer ha almeno un corso imminente, false altrimenti
     */
    boolean haCorsiImminenti(String idTrainer);

    /**
     * Sostituisce un Personal Trainer nei corsi attivi o futuri.
     *
     * Il metodo rappresenta la richiesta di swap.
     * I corsi non vengono cancellati: viene aggiornato soltanto il trainer
     * assegnato, mantenendo invariati dati come data, capienza, iscritti e stato.
     *
     * @param idTrainerDaSostituire identificativo del trainer da sostituire
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @return numero di corsi aggiornati
     * @throws SostitutoNonValidoException se il sostituto non è valido per lo swap
     */
    int sostituisciTrainerNeiCorsi(String idTrainerDaSostituire, String idTrainerSostituto)
            throws SostitutoNonValidoException;
}
