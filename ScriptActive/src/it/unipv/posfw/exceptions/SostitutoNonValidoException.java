package it.unipv.posfw.exceptions;

/**
 * Eccezione lanciata quando il PersonalTrainer scelto come sostituto
 * non rispetta i controlli richiesti.
 *
 * Può essere usata, ad esempio, se il sostituto non esiste, non è attivo,
 * coincide con il trainer da sostituire o non ha la stessa specializzazione.
 */
public class SostitutoNonValidoException extends Exception {

    /**
     * Crea l'eccezione con il messaggio da mostrare o gestire.
     *
     * @param messaggio descrizione dell'errore
     */
    public SostitutoNonValidoException(String messaggio) {
        super(messaggio);
    }
}
