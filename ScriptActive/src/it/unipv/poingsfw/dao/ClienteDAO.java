package it.unipv.poingsfw.dao;

import it.unipv.poingsfw.domain.Cliente;

/**
 * L'interfaccia {@code ClienteDAO} definisce il contratto per le operazioni di persistenza 
 * relative all'entità {@link Cliente}. 
 * <p>
 * Implementa il pattern architetturale <b>Data Access Object (DAO)</b>. 
 * Lo scopo di questa interfaccia è disaccoppiare la logica di business (Controller) 
 * dai dettagli implementativi del database sottostante (es. query SQL). 
 * Espone le classiche operazioni necessarie 
 * per la gestione del ciclo di vita dei dati di un cliente.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public interface ClienteDAO {
	
	/**
     * Inserisce un nuovo cliente all'interno del database (Operazione di Create).
     * * @param c L'oggetto {@code Cliente} contenente i dati da persistere.
     * @return {@code true} se l'inserimento è andato a buon fine, {@code false} altrimenti.
     */

    public boolean inserisciCliente(Cliente c);
    
    /**
     * Recupera i dati di un cliente specifico partendo dal suo identificativo univoco (Operazione di Read).
     * * @param codiceFiscale La stringa che rappresenta la Chiave Primaria (Codice Fiscale) del cliente.
     * @return L'oggetto {@code Cliente} popolato con i dati del database, oppure {@code null} se nessun cliente corrisponde al codice fiscale fornito.
     */

    public Cliente getClienteByCF(String codiceFiscale);
    
    /**
     * Aggiorna le informazioni di un cliente già esistente nel database (Operazione di Update).
     * * @param c L'oggetto {@code Cliente} contenente i dati aggiornati da sovrascrivere.
     */

    public void updateCliente(Cliente c);
    
    /**
     * Rimuove fisicamente o logicamente un cliente dal database (Operazione di Delete).
     * * @param codiceFiscale Il Codice Fiscale del cliente da eliminare.
     */

    public void deleteCliente(String codiceFiscale);
}
