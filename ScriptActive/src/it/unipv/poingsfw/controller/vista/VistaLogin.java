package it.unipv.poingsfw.controller.vista;

/**
 * Ciò che il {@code LoginController} si aspetta dalla schermata di autenticazione.
 * <p>
 * Il controller dipende da questa interfaccia e non dalla finestra Swing concreta:
 * non importa classi grafiche e può essere collaudato con una vista fittizia.
 * </p>
 */
public interface VistaLogin {

    /** Mostra un messaggio di errore di accesso. */
    void mostraErrore(String messaggio);

    /**
     * Informa l'utente che l'abbonamento è scaduto e chiede se vuole rinnovarlo subito.
     *
     * @param messaggio motivo del blocco dell'accesso
     * @return {@code true} se l'utente sceglie di procedere al rinnovo
     */
    boolean chiediConfermaRinnovo(String messaggio);

    /** Chiude la schermata. */
    void chiudi();
}
