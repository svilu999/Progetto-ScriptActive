package it.unipv.poingsfw.dao;

import java.util.List;

import it.unipv.poingsfw.domain.Corso;

/**
 * Interfaccia che definisce il contratto formale per le operazioni di persistenza 
 * dell'entità di dominio Corso. 
 * 
 * Seguendo il pattern Data Access Object (DAO), questa interfaccia agisce come 
 * una Pure Fabrication, assegnando la responsabilità della persistenza a una 
 * classe artificiale esterna al dominio per mantenere alta la coesione delle Entity.
 * L'approccio permette di realizzare il principio di Dependency Inversion, 
 * garantendo il Low Coupling tra lo strato di logica applicativa e i dettagli 
 * implementativi del Data Access Layer.
 *
 * @author Lorenzo
 * @version 1.2
 * @see Corso
 */
public interface CorsoDAO {

    /**
     * Inserisce una nuova istanza di Corso nel sistema di persistenza.
     * Realizza il mapping indiretto tra l'oggetto di dominio (POJO) e il 
     * supporto di memorizzazione.
     * 
     * @param c L'oggetto di dominio Corso da rendere persistente.
     */
    void insert(Corso c);

    /**
     * Rimuove un Corso dal supporto di persistenza identificandolo tramite 
     * la sua chiave primaria (ID).
     * 
     * @param idCorso L'identificativo univoco del corso da eliminare.
     */
    void delete(String idCorso);

    /**
     * Ricerca e ricostruisce un oggetto Corso partendo dal suo identificativo.
     * Garantisce l'incapsulamento dell'informazione [7] durante il recupero dei dati.
     * 
     * @param idCorso L'identificativo univoco dell'entità.
     * @return L'istanza di {@link Corso} popolata con i dati recuperati, 
     *         o null se non presente.
     */
    Corso findById(String idCorso);

    /**
     * Recupera la lista completa di tutte le entità Corso persistite.
     * Utilizza una List per evitare l'esposizione di strutture dati specifiche 
     * del DBMS (come il ResultSet), prevenendo il Layer Leakage.
     * 
     * @return Una {@link List} contenente tutti gli oggetti di dominio Corso.
     */
    List<Corso> findAll();

    /**
     * Restituisce la collezione di corsi filtrata per i requisiti del palinsesto.
     * Questo metodo specializzato supporta la logica di visualizzazione della UI 
     * senza violare il principio di separazione Modello-Vista.
     * 
     * @return Una {@link List} di oggetti Corso pronti per la visualizzazione.
     */
    List<Corso> getPalinsesto();

    /**
     * Aggiorna esclusivamente l'attributo relativo ai posti disponibili.
     * Metodo inserito per soddisfare i requisiti del caso d'uso UC2,
     * permettendo aggiornamenti mirati senza dover persistere l'intera entità.
     * 
     * @param c L'istanza del Corso contenente lo stato aggiornato della capienza.
     */
    void updatePostiDisponibili(Corso c);
}