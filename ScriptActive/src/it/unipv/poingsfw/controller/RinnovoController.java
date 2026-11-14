package it.unipv.poingsfw.controller;

import java.util.Objects;

import it.unipv.poingsfw.controller.vista.VistaRinnovo;
import it.unipv.poingsfw.dao.UtenteDAO;
import it.unipv.poingsfw.domain.LivelloAbbonamento;

/**
 * Controller del rinnovo manuale di un abbonamento scaduto.
 * <p>
 * Viene attivato quando, in fase di login, il sistema rileva che l'abbonamento del
 * cliente è scaduto e l'utente sceglie di rinnovarlo. Riceve dalla vista il piano
 * scelto, delega l'aggiornamento al livello di persistenza e riporta l'utente alla
 * schermata di autenticazione.
 * </p>
 * <p>
 * Prima del refactoring questa logica (compreso l'accesso diretto al DAO MySQL) si
 * trovava dentro la finestra Swing.
 * </p>
 */
public class RinnovoController {

    private final VistaRinnovo view;
    private final UtenteDAO dao;
    private final Navigatore navigatore;
    private final String emailCliente;

    public RinnovoController(VistaRinnovo view, UtenteDAO dao, Navigatore navigatore, String emailCliente) {
        this.view = Objects.requireNonNull(view, "La vista non può essere null.");
        this.dao = Objects.requireNonNull(dao, "Il DAO non può essere null.");
        this.navigatore = Objects.requireNonNull(navigatore, "Il navigatore non può essere null.");
        this.emailCliente = emailCliente;
    }

    /**
     * Rinnova l'abbonamento del cliente con il piano scelto e torna al login.
     *
     * @param livello durata scelta dall'utente
     */
    public void confermaRinnovo(LivelloAbbonamento livello) {
        if (livello == null) {
            view.mostraErrore("Seleziona un piano di abbonamento.");
            return;
        }

        try {
            dao.eseguiRinnovo(emailCliente, livello.getMesi(), livello.name());
        } catch (RuntimeException e) {
            view.mostraErrore("Impossibile completare il rinnovo. Riprova più tardi.");
            return;
        }

        view.mostraRinnovoCompletato(livello);
        tornaAlLogin();
    }

    /**
     * L'utente rinuncia al rinnovo: torna alla schermata di autenticazione.
     */
    public void annulla() {
        tornaAlLogin();
    }

    private void tornaAlLogin() {
        view.chiudi();
        navigatore.apriLogin();
    }
}
