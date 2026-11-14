package it.unipv.poingsfw.controller.vista;

import java.util.List;

import it.unipv.poingsfw.domain.SessioneAllenamento;

/**
 * Ciò che lo {@code StoricoAllenamentiController} si aspetta dall'area Premium (UC4).
 */
public interface VistaStoricoAllenamenti {

    /** Abilita e mostra il modulo di inserimento di una nuova sessione. */
    void mostraModuloInserimento();

    /** Mostra lo storico delle sessioni di allenamento registrate. */
    void mostraStorico(List<SessioneAllenamento> storico);

    /** Mostra un messaggio informativo. */
    void mostraAvviso(String messaggio);

    /** Chiude la schermata. */
    void chiudi();
}
