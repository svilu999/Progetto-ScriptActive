package it.unipv.poingsfw.dao; 

import it.unipv.poingsfw.domain.Utente;

/**
 * L'interfaccia {@code UtenteDAO} definisce le operazioni di persistenza e recupero dati 
 * relative all'entità {@link Utente}, concentrandosi specificamente sui flussi di 
 * autenticazione e registrazione.
 * <p>
 * Implementando il pattern <b>Data Access Object (DAO)</b>, separa la logica di business e 
 * di sicurezza dalle specifiche interrogazioni al database. Gestisce l'accesso alle 
 * credenziali garantendo che il livello superiore (Controller) possa verificare 
 * l'identità degli utenti e registrarne di nuovi in modo sicuro.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public interface UtenteDAO {
    
	/**
     * Verifica le credenziali di un utente per consentirne l'accesso al sistema.
     * <p>
     * <b>Logica di Autenticazione:</b> Il metodo interroga il database cercando una 
     * corrispondenza esatta tra l'indirizzo email e la password fornita. 
     * </p>
     * * @param email    L'indirizzo email fornito dall'utente in fase di login.
     * @param password La password in chiaro inserita dall'utente.
     * @return Un oggetto {@link Utente} popolato con i dati dell'account se le credenziali 
     * sono corrette; {@code null} se l'autenticazione fallisce (utente non trovato o password errata).
     */
    // Metodo per il login
    Utente effettuaLogin(String email, String password);
    
    /**
     * Registra un nuovo cliente all'interno del sistema di persistenza.
     * <p>
     * <b>Sicurezza:</b> Il parametro {@code passwordHash} indica che il livello superiore 
     * ha già provveduto ad offuscare la password (tramite algoritmi di hashing) prima di 
     * passarla al livello DAO. Il database non memorizzerà mai la password in chiaro.
     * </p>
     * * @param cf           Il Codice Fiscale univoco del cliente.
     * @param nome         Il nome di battesimo del cliente.
     * @param cognome      Il cognome del cliente.
     * @param email        L'indirizzo email associato all'account.
     * @param passwordHash La password crittografata/hashata da salvare nel database.
     */
    // Metodo per la registrazione
    void registraCliente(String cf, String nome, String cognome, String email, String passwordHash);

}