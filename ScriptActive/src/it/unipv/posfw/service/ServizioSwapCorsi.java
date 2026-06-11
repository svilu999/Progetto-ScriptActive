package it.unipv.posfw.service;

import it.unipv.posfw.exceptions.SostitutoNonValidoException;

/**
 * Interfaccia di integrazione tra UC5 (Gestione dei Contratti del Personale)
 * e il modulo dei corsi/palinsesto.
 *
 * Il punto importante è che GestorePersonale NON deve conoscere i dettagli
 * interni di GestoreCorsi, CorsoDAO o della lista dei corsi. GestorePersonale
 * deve soltanto sapere che esiste un servizio capace di:
 *
 * 1. verificare se un Personal Trainer ha corsi ancora rilevanti;
 * 2. verificare se ha corsi imminenti;
 * 3. richiedere lo swap del trainer sui corsi attivi/futuri.
 *
 * In questo modo UC5 resta autonomo e non "ruba" responsabilità al caso d'uso
 * del palinsesto corsi. La classe concreta che implementa questa interfaccia
 * può essere GestoreCorsi, ma GestorePersonale dipende dall'astrazione.
 */
public interface ServizioSwapCorsi {

    /**
     * Verifica se il Personal Trainer indicato ha almeno un corso ATTIVO
     * programmato da adesso in avanti.
     *
     * Serve soprattutto nel licenziamento senza sostituto: se esistono corsi
     * attivi/futuri, il sistema non deve disattivare il PT lasciando corsi senza
     * istruttore.
     *
     * @param idTrainer id del Personal Trainer da controllare
     * @return true se esiste almeno un corso attivo/futuro assegnato a quel PT
     */
    boolean haCorsiAttiviOFuturi(String idTrainer);

    /**
     * Verifica se il Personal Trainer indicato ha corsi imminenti.
     *
     * Nel progetto la soglia scelta è 24 ore: un corso è imminente se è ATTIVO
     * e cade tra il momento attuale e le prossime 24 ore. La soglia è gestita
     * nell'implementazione concreta, così UC5 non deve conoscere i dettagli del
     * palinsesto.
     *
     * @param idTrainer id del Personal Trainer da controllare
     * @return true se esiste almeno un corso imminente assegnato a quel PT
     */
    boolean haCorsiImminenti(String idTrainer);

    /**
     * Richiede la sostituzione del Personal Trainer sui corsi attivi/futuri.
     *
     * Questo metodo rappresenta la "notifica swap" citata nel caso d'uso UC5:
     * GestorePersonale segnala al modulo corsi che un PT sta per essere
     * licenziato e che i suoi corsi devono essere riassegnati a un sostituto.
     *
     * Importante: lo swap NON deve cancellare i corsi. Deve soltanto cambiare il
     * trainer assegnato, mantenendo intatti id corso, data, capienza, iscritti e
     * stato del corso.
     *
     * @param idTrainerDaSostituire id del PT che verrà licenziato
     * @param idTrainerSostituto id del PT che prenderà in carico i corsi
     * @return numero di corsi aggiornati
     * @throws SostitutoNonValidoException se il sostituto non è valido per lo swap
     */
    int sostituisciTrainerNeiCorsi(String idTrainerDaSostituire, String idTrainerSostituto)
            throws SostitutoNonValidoException;
}
