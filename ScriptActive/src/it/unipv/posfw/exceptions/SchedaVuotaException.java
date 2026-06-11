package it.unipv.posfw.exceptions;

public class SchedaVuotaException extends Exception {
    
    public SchedaVuotaException() {
        super("Impossibile salvare: la scheda di allenamento è vuota. Aggiungi almeno un esercizio.");
    }

    public SchedaVuotaException(String messaggio) {
        super(messaggio);
    }
}