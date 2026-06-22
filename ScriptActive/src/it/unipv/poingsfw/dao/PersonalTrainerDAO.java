package it.unipv.poingsfw.dao;

import java.util.List;

import it.unipv.poingsfw.domain.PersonalTrainer;

/**
 * Interfaccia DAO per la gestione della persistenza dei Personal Trainer.
 *
 * Questa interfaccia definisce le operazioni principali che il sistema può
 * eseguire sui Personal Trainer, senza esporre al controller i dettagli
 * dell'implementazione concreta del database.
 *
 * Viene usata da GestorePersonale per salvare, cercare,
 * aggiornare e disattivare logicamente i Personal Trainer.
 */
public interface PersonalTrainerDAO {

    /**
     * Salva un nuovo Personal Trainer nel sistema.
     *
     * @param pt Personal Trainer da salvare
     */
    void salva(PersonalTrainer pt);

    /**
     * Cerca un Personal Trainer tramite il suo identificativo.
     *
     * @param idPT identificativo del Personal Trainer da cercare
     * @return Personal Trainer trovato, oppure null se non esiste
     */
    PersonalTrainer trovaPerId(String idPT);
    
    /**
     * Cerca un Personal Trainer tramite la sua email.
     *
     * Il metodo viene usato in fase di assunzione per impedire
     * la registrazione duplicata dello stesso Personal Trainer.
     *
     * @param email email del Personal Trainer da cercare
     * @return Personal Trainer trovato, oppure null se non esiste
     */
    PersonalTrainer trovaPerEmail(String email);

    /**
     * Disattiva logicamente un Personal Trainer.
     *
     * L'operazione non elimina fisicamente il record dal database, ma permette
     * all'implementazione concreta di applicare il soft delete.
     *
     * @param idPT identificativo del Personal Trainer da disattivare
     */
    void elimina(String idPT);

    /**
     * Aggiorna i dati di un Personal Trainer già presente nel sistema.
     *
     * @param pt Personal Trainer con i dati aggiornati
     */
    void aggiorna(PersonalTrainer pt);

    /**
     * Restituisce tutti i Personal Trainer presenti nel sistema.
     *
     * @return lista dei Personal Trainer disponibili
     */
    List<PersonalTrainer> trovaTutti();
}