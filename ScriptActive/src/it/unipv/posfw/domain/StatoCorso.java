package it.unipv.posfw.domain;

/**
 * Rappresenta un tipo enumerativo (enum) che definisce il set di costanti predefinite
 * volte a descrivere lo stato interno di un'entità all'interno del Modello di Dominio.
 * 
 * Secondo i principi della progettazione orientata agli oggetti (OOP), questa enum 
 * fornisce una specifica di tipo rigorosa per la gestione del ciclo di vita dell'oggetto 
 * Corso, permettendo di mappare lo "stato" dell'entità in modo tipizzato ed evitando 
 * l'anti-pattern delle costanti letterali. 
 * 
 * In termini architetturali, il cambiamento di questi valori è spesso il trigger per 
 * operazioni di sistema coordinate (es. notifiche tramite pattern Observer in caso di 
 * cancellazione).
 *
 * @author Lorenzo
 * @version 1.1
 * @see <a href="http://docs.oracle.com/javase/8/docs/api/">Java Standard Documentation</a>
 */
public enum StatoCorso {

    /**
     * Il corso è regolarmente inserito nel sistema e disponibile per le attività.
     * Rappresenta lo stato di default a seguito dello scenario principale di creazione (UC3).
     */
    ATTIVO,

    /**
     * Il corso è stato rimosso dal palinsesto.
     * La transizione verso questo stato innesca la notifica automatica agli osservatori 
     * (Clienti iscritti) come previsto dai diagrammi di sequenza per la risoluzione 
     * delle Open Issue.
     */
    CANCELLATO,

    /**
     * Il corso ha raggiunto il numero massimo di partecipanti.
     * Stato gestito dall'entità Corso in qualità di Information Expert per quanto 
     * riguarda la logica di capienza e disponibilità posti.
     */
    COMPLETO
}