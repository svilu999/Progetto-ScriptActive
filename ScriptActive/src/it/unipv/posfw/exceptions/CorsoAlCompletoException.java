package it.unipv.posfw.exceptions;

public class CorsoAlCompletoException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public CorsoAlCompletoException(String messaggio) {
        super(messaggio);
    }
}