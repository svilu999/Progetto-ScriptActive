package it.unipv.poingsfw.dao;

/**
 * DAO dedicato alle operazioni di persistenza necessarie allo swap dei corsi.
 *
 * L'interfaccia espone solo operazioni tecniche sul database. Le regole
 * applicative, come la validità del sostituto o il blocco dello swap, restano
 * nel Service.
 */
public interface SwapCorsiDAO {

    /**
     * Verifica nel database se esistono corsi attivi o futuri associati
     * a un Personal Trainer.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se esiste almeno un corso attivo o futuro, false altrimenti
     */
    boolean esistonoCorsiAttiviOFuturiPerTrainer(int idTrainer);

    /**
     * Verifica nel database se esistono corsi imminenti associati
     * a un Personal Trainer.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se esiste almeno un corso imminente, false altrimenti
     */
    boolean esistonoCorsiImminentiPerTrainer(int idTrainer);

    /**
     * Verifica nel database se esiste un Personal Trainer attivo
     * con contratto attivo.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return true se il trainer esiste ed è attivo, false altrimenti
     */
    boolean esisteTrainerConContrattoAttivo(int idTrainer);

    /**
     * Verifica nel database se esistono sovrapposizioni orarie tra i corsi
     * del trainer da sostituire e i corsi già assegnati al sostituto.
     *
     * @param idTrainerDaSostituire identificativo numerico del trainer da sostituire
     * @param idTrainerSostituto identificativo numerico del trainer sostituto
     * @return true se esiste almeno una sovrapposizione, false altrimenti
     */
    boolean esistonoSovrapposizioniTraCorsi(
            int idTrainerDaSostituire,
            int idTrainerSostituto
    );

    /**
     * Riassegna nel database i corsi attivi o futuri dal vecchio trainer
     * al nuovo trainer.
     *
     * Il metodo non decide se lo swap sia valido: esegue solo l'update.
     *
     * @param idTrainerDaSostituire identificativo numerico del trainer da sostituire
     * @param idTrainerSostituto identificativo numerico del trainer sostituto
     * @return numero di righe aggiornate
     */
    int riassegnaCorsiAttiviOFuturi(
            int idTrainerDaSostituire,
            int idTrainerSostituto
    );
}