package it.unipv.poingsfw.controller.vista;

import java.util.List;

import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;

/**
 * Ciò che il {@code DashboardClienteController} si aspetta dalla dashboard del cliente.
 */
public interface VistaDashboardCliente {

    /** Mostra i dati anagrafici e di abbonamento del cliente. */
    void impostaDatiCliente(Cliente cliente);

    /** Mostra l'elenco dei corsi prenotati dal cliente. */
    void mostraCorsiPrenotati(List<Corso> corsi);

    /** Mostra un messaggio di errore. */
    void mostraErrore(String messaggio);

    /** Chiude la schermata. */
    void chiudi();
}
