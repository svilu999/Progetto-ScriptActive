package it.unipv.poingsfw.exceptions;

public class UtenteNonPremiumException extends Exception {
    
    public UtenteNonPremiumException() {
        super("Accesso negato: questa funzionalità è riservata agli utenti Premium.");
    }
    
    public UtenteNonPremiumException(String messaggio) {
        super(messaggio);
    }
}