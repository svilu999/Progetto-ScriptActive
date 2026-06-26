package it.unipv.poingsfw.dao;

import java.util.List;

import it.unipv.poingsfw.dto.DatiPersonalTrainer;

/**
 * Interfaccia DAO per la gestione della persistenza dei Personal Trainer.
 *
 * L'interfaccia lavora con DTO e identificativi già normalizzati, in modo
 * che il DAO si occupi solo di accesso ai dati e non di logica applicativa.
 */
public interface PersonalTrainerDAO {

    /**
     * Salva un nuovo Personal Trainer nel database.
     *
     * @param datiTrainer dati persistenti del Personal Trainer da salvare
     */
    void salva(DatiPersonalTrainer datiTrainer);

    /**
     * Cerca un Personal Trainer tramite identificativo numerico.
     *
     * @param idTrainer identificativo numerico del Personal Trainer
     * @return dati del Personal Trainer trovato, oppure null se non esiste
     */
    DatiPersonalTrainer trovaPerId(Integer idTrainer);

    /**
     * Cerca un Personal Trainer tramite email.
     *
     * @param email email del Personal Trainer da cercare
     * @return dati del Personal Trainer trovato, oppure null se non esiste
     */
    DatiPersonalTrainer trovaPerEmail(String email);

    /**
     * Disattiva logicamente un Personal Trainer.
     *
     * @param idTrainer identificativo numerico del Personal Trainer da disattivare
     */
    void elimina(Integer idTrainer);

    /**
     * Aggiorna i dati di un Personal Trainer già presente nel database.
     *
     * @param datiTrainer dati persistenti aggiornati del Personal Trainer
     */
    void aggiorna(DatiPersonalTrainer datiTrainer);

    /**
     * Restituisce tutti i Personal Trainer presenti nel database.
     *
     * @return lista dei dati persistenti dei Personal Trainer
     */
    List<DatiPersonalTrainer> trovaTutti();
}