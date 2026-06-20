package it.unipv.posfw.exceptions;

/**
 * Eccezione lanciata quando un cliente tenta di prenotare un corso 
 * che ha raggiunto la capienza massima (postiDisponibili = 0).
 * Segnala l'inserimento automatico in Lista d'Attesa.
 */

public class CorsoAlCompletoException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public CorsoAlCompletoException(String messaggio) {
        super(messaggio);
    }
}