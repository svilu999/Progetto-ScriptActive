package it.unipv.poingsfw.controller;

import java.util.Objects;

import it.unipv.poingsfw.controller.vista.VistaDashboardCliente;
import it.unipv.poingsfw.domain.Cliente;

/**
 * Controller della dashboard del cliente.
 * <p>
 * Popola la schermata con i dati del cliente autenticato e con i corsi che ha
 * prenotato, e traduce i gesti dell'utente in richieste di navigazione.
 * </p>
 * <p>
 * Prima del refactoring questo codice era duplicato in due controller diversi
 * (login e storico allenamenti), ciascuno dei quali costruiva la finestra, i DAO
 * e i listener per conto proprio.
 * </p>
 */
public class DashboardClienteController {

    private final VistaDashboardCliente view;
    private final GestorePrenotazioni gestorePrenotazioni;
    private final Navigatore navigatore;
    private final Cliente cliente;

    public DashboardClienteController(VistaDashboardCliente view, GestorePrenotazioni gestorePrenotazioni,
                                      Navigatore navigatore, Cliente cliente) {
        this.view = Objects.requireNonNull(view, "La vista non può essere null.");
        this.gestorePrenotazioni = Objects.requireNonNull(gestorePrenotazioni, "Il gestore prenotazioni non può essere null.");
        this.navigatore = Objects.requireNonNull(navigatore, "Il navigatore non può essere null.");
        this.cliente = Objects.requireNonNull(cliente, "Il cliente non può essere null.");
    }

    /**
     * Carica nella schermata i dati del cliente e le sue prenotazioni.
     */
    public void inizializza() {
        view.impostaDatiCliente(cliente);
        try {
            view.mostraCorsiPrenotati(gestorePrenotazioni.getCorsiPrenotatiDalCliente(cliente));
        } catch (RuntimeException e) {
            view.mostraErrore("Impossibile recuperare i corsi prenotati.");
        }
    }

    /** L'utente vuole accedere allo storico allenamenti (area Premium, UC4). */
    public void apriAreaPremium() {
        view.chiudi();
        navigatore.apriStoricoAllenamenti(cliente);
    }

    /** L'utente vuole consultare il palinsesto per prenotare un corso. */
    public void apriPalinsesto() {
        view.chiudi();
        navigatore.apriPalinsestoCliente(cliente);
    }
}
