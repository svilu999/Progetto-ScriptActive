package it.unipv.poingsfw.dao;

import java.util.List;

import it.unipv.poingsfw.domain.Sede;

/**
 * L'interfaccia {@code SedeDAO} definisce il contratto formale per le operazioni di persistenza 
 * e recupero dati relative all'entità {@link Sede}.
 * <p>
 * In linea con il pattern <b>Data Access Object (DAO)</b>, questa interfaccia astrae le 
 * interazioni con il meccanismo di memorizzazione sottostante, garantendo che il livello di 
 * business dell'applicazione non dipenda da specifiche tecnologie di database (es. SQL, NoSQL).
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public interface SedeDAO {
	
	/**
     * Inserisce una nuova sede nel sistema di persistenza (Operazione di Create).
     * * @param nomeSede Il nome identificativo della nuova sede da aggiungere.
     */
    
    void aggiungiSede(String nomeSede);
    
    /**
     * Recupera tutte le sedi presenti nel database e ne direziona l'output direttamente 
     * sullo standard output (console).
     */
    
    void stampaTutteLeSedi();
    
    /**
     * Recupera l'elenco completo di tutte le sedi memorizzate nel database (Operazione di Read).
     * * @return Una {@link List} di oggetti {@link Sede} contenente tutti i record presenti nella persistenza. 
     * Se non sono presenti sedi, restituisce una lista vuota.
     */
    
    List<Sede> getTutteLeSedi();

}
