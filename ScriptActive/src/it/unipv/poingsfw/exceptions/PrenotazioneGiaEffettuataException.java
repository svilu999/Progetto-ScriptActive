package it.unipv.poingsfw.exceptions;

public class PrenotazioneGiaEffettuataException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public PrenotazioneGiaEffettuataException(String messaggio) {
        super(messaggio);
    }
}