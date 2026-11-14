package it.unipv.poingsfw.controller.vista;

import it.unipv.poingsfw.domain.LivelloAbbonamento;

/**
 * Ciò che il {@code RinnovoController} si aspetta dalla schermata di rinnovo abbonamento.
 */
public interface VistaRinnovo {

    /** Conferma all'utente che l'abbonamento è stato rinnovato con il piano indicato. */
    void mostraRinnovoCompletato(LivelloAbbonamento livello);

    /** Mostra un messaggio di errore. */
    void mostraErrore(String messaggio);

    /** Chiude la schermata. */
    void chiudi();
}
