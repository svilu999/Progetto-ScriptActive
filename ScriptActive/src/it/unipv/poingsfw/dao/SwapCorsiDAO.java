package it.unipv.poingsfw.dao;

/**
 * DAO dedicato alle operazioni di persistenza necessarie
 * alla sostituzione di un Personal Trainer nei corsi.
 *
 * L'interfaccia espone esclusivamente operazioni tecniche sul database.
 * Le verifiche e le decisioni applicative restano nel Service.
 */
public interface SwapCorsiDAO {

    /**
     * Verifica se esistono corsi attivi o futuri associati
     * a un Personal Trainer.
     *
     * @param idTrainer identificativo numerico del trainer
     * @return true se esiste almeno un corso attivo o futuro
     */
    boolean esistonoCorsiAttiviOFuturiPerTrainer(int idTrainer);

    /**
     * Verifica se esistono corsi imminenti associati
     * a un Personal Trainer.
     *
     * @param idTrainer identificativo numerico del trainer
     * @return true se esiste almeno un corso imminente
     */
    boolean esistonoCorsiImminentiPerTrainer(int idTrainer);

    /**
     * Verifica se esiste un Personal Trainer attivo
     * con contratto attivo.
     *
     * @param idTrainer identificativo numerico del trainer
     * @return true se il trainer esiste ed è attivo
     */
    boolean esisteTrainerConContrattoAttivo(int idTrainer);

    /**
     * Verifica se esistono sovrapposizioni tra i corsi del trainer
     * da sostituire e quelli del sostituto.
     *
     * @param idTrainerDaSostituire identificativo del trainer da sostituire
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @return true se esiste almeno una sovrapposizione
     */
    boolean esistonoSovrapposizioniTraCorsi(
            int idTrainerDaSostituire,
            int idTrainerSostituto
    );

    /**
     * Riassegna i corsi al sostituto e disattiva il trainer sostituito.
     *
     * Le modifiche alla tabella Corso, alla tabella PersonalTrainer
     * e alla tabella Utente devono essere eseguite utilizzando la stessa
     * connessione e la stessa transazione.
     *
     * Il metodo non decide se la sostituzione sia consentita:
     * esegue esclusivamente le modifiche persistenti richieste dal Service.
     *
     * @param idTrainerDaSostituire identificativo del trainer da disattivare
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @return numero di corsi riassegnati
     */
    int riassegnaCorsiEDisattivaTrainer(
            int idTrainerDaSostituire,
            int idTrainerSostituto
    );
}