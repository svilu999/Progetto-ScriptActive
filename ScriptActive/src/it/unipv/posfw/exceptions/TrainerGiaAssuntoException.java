package it.unipv.posfw.exceptions;

/**
 * Eccezione lanciata quando si prova ad assumere un PersonalTrainer
 * già presente nel sistema.
 */
public class TrainerGiaAssuntoException extends Exception {

    /**
     * Crea l'eccezione con il messaggio da mostrare o gestire.
     *
     * @param messaggio descrizione dell'errore
     */
    public TrainerGiaAssuntoException(String messaggio) {
        super(messaggio);
    }
}
