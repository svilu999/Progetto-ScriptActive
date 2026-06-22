package it.unipv.poingsfw.domain;
/**
 * Interfaccia che definisce il contratto formale per il ruolo di Observer,
 * noto anche come Subscriber o Listener, all'interno del Pattern Comportamentale Observer [GoF].
 * 
 * Questa astrazione è fondamentale per implementare il Principio di Separazione Modello-Vista (MV Separation).
 * Essa consente di minimizzare l'accoppiamento (Low Coupling) tra lo strato di dominio e l'interfaccia utente (GUI), 
 * in quanto gli oggetti del dominio non conoscono direttamente gli oggetti della View, 
 * ma interagiscono con essi esclusivamente tramite questo contratto d'interfaccia.
 * 
 * Il meccanismo realizza una comunicazione di tipo "Push from below", dove il Modello 
 * informa gli strati superiori del mutamento del proprio stato interno o del verificarsi 
 * di specifici eventi di sistema.
 *
 * @author Lorenzo
 * @version 2.0
 * @see Subject
 */
public interface Observer {
	/**
     * Metodo di callback invocato dal Subject (Soggetto) per notificare un cambiamento di stato.
     * 
     * Le informazioni relative all'evento vengono veicolate all'osservatore 
     * tramite parametri del metodo update, permettendo alla classe Observer di aggiornare la propria presentazione in modo sincrono con il Modello.
     * 
     * @param messaggio Stringa contenente il dettaglio informativo dell'evento (es. notifica di 
     *                  cancellazione corso).
     */
    void update(String messaggio);
}