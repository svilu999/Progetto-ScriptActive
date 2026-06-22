package it.unipv.poingsfw.dao;

import java.util.List;

import it.unipv.poingsfw.domain.SessioneAllenamento;

/**
 * Interfaccia {@code SessioneDAO} per l'implementazione del pattern <b>Data Access Object (DAO)</b>.
 * <p>
 * Garantisce il disaccoppiamento architetturale tra la logica di business e il livello di persistenza, 
 * permettendo ai Controller di interagire con il database tramite astrazioni anziché implementazioni concrete.
 * </p>
 * <p>
 * Supporta operativamente lo <b>Use Case UC4: Registrazione e Monitoraggio Prestazioni</b>, 
 * fornendo i metodi necessari per il salvataggio dei parametri quantitativi e il recupero 
 * dei dati aggregati per la vista riepilogativa dello storico.
 * </p>
 * * @author Vilucchi
 * @version 1.1
 * @see it.unipv.poingsfw.domain.SessioneAllenamento
 */
public interface SessioneDAO {

    /**
     * Persiste una nuova sessione di allenamento nel database.
     * * @param sessione L'entità {@link SessioneAllenamento} contenente gli esercizi e i dati da salvare.
     * @return {@code true} se la transazione di inserimento va a buon fine, {@code false} in caso di errore.
     */
    boolean salvaSessione(SessioneAllenamento sessione);

    /**
     * Recupera lo storico completo delle sessioni di allenamento per uno specifico utente.
     * * @param idCliente L'identificativo univoco del Cliente Premium.
     * @return Una {@link List} di oggetti {@link SessioneAllenamento}. 
     * Restituisce una lista vuota se l'utente non ha registrato alcun allenamento.
     */
    List<SessioneAllenamento> getStorico(String idCliente);

    /**
     * Rimuove definitivamente una specifica sessione di allenamento dal livello di persistenza.
     * * @param sessione L'entità {@link SessioneAllenamento} bersaglio da eliminare.
     * @return {@code true} se l'operazione di cancellazione ha successo, {@code false} altrimenti.
     */
    boolean eliminaSessioneSpecifica(SessioneAllenamento sessione);
}