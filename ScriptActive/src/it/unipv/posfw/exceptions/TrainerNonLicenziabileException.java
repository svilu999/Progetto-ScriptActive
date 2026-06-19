package it.unipv.posfw.exceptions;

/**
 * Eccezione lanciata quando un PersonalTrainer non può essere licenziato.
 *
 * Viene usata, ad esempio, quando il trainer ha ancora corsi attivi o futuri
 * e non è stato indicato un sostituto valido.
 */
public class TrainerNonLicenziabileException extends Exception {

    /**
     * Crea l'eccezione con il messaggio da mostrare o gestire.
     *
     * @param message descrizione dell'errore
     */
    public TrainerNonLicenziabileException(String message) {
        super(message);
    }
}
