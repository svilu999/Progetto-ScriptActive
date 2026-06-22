package it.unipv.poingsfw.domain;
/**
 * Interfaccia che definisce il contratto formale per il ruolo di Subject (Soggetto) 
 * all'interno del Pattern Comportamentale Observer [GoF].
 * 
 * L'uso di questa astrazione è fondamentale per 
 * realizzare il principio di Low Coupling (Basso Accoppiamento) tra lo strato di 
 * dominio e lo strato di presentazione (GUI). Essa permette di implementare la 
 * "Separazione Modello-Vista" (MV Separation), garantendo che le entità del dominio 
 * (es. Corso) non conoscano direttamente le classi della View, ma interagiscano con 
 * esse esclusivamente tramite questo contratto d'interfaccia.
 * 
 * Il meccanismo abilita una comunicazione di tipo "Push from below", in cui il 
 * Modello informa gli strati superiori del mutamento del proprio stato interno 
 * (es. variazione di disponibilità posti o annullamento attività), assicurando 
 * la Protected Variation rispetto ai cambiamenti futuri nella UI.
 *
 * @author Lorenzo
 * @version 2.0
 * @see Observer
 */
public interface Subject {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers();
}