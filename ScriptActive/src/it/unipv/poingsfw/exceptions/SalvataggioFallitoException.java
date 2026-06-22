package it.unipv.poingsfw.exceptions;

public class SalvataggioFallitoException extends Exception {
    
    public SalvataggioFallitoException() {
        super("Si è verificato un errore durante il salvataggio nel database. Riprova più tardi.");
    }

    public SalvataggioFallitoException(String messaggio) {
        super(messaggio);
    }
}