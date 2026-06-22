package it.unipv.poingsfw.dao;

import it.unipv.poingsfw.domain.Utente;

/**
 * Interfaccia {@code LoginDAO} per l'astrazione delle operazioni di persistenza legate all'autenticazione.
 * <p>
 * Implementa il pattern architetturale <b>Data Access Object (DAO)</b>, disaccoppiando il dominio 
 * applicativo dai dettagli della base di dati. Questo favorisce la testabilità e rispetta il 
 * Principio di Inversione delle Dipendenze (DIP) dei principi SOLID.
 * </p>
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Risponde direttamente al caso d'uso di <b>Login</b>. Fornisce il contratto architetturale per 
 * il recupero delle credenziali, permettendo al Controller di determinare l'identità e il ruolo 
 * dell'utente che tenta l'accesso al sistema.
 * </p>
 * * @author Vilucchi
 * @version 1.1
 * @see it.unipv.poingsfw.domain.Utente
 */
public interface LoginDAO {

    /**
     * Interroga il livello di persistenza per validare le credenziali fornite dall'interfaccia utente.
     * <p>
     * Se la validazione ha successo, il metodo restituisce un'entità di dominio pienamente 
     * inizializzata. Tale entità (essendo polimorfica) consentirà al Controller di innescare 
     * il corretto instradamento tramite <i>Double Dispatch</i> verso la dashboard appropriata.
     * </p>
     *
     * @param email    L'indirizzo email immesso dall'attore nel form di login.
     * @param password La password associata all'account da verificare nel data store.
     * @return L'istanza polimorfica di {@link Utente} se le credenziali sono corrette e l'account è attivo; {@code null} in caso contrario.
     */
    Utente verificaCredenziali(String email, String password);
}