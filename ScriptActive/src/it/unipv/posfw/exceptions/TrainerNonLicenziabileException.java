package it.unipv.posfw.exceptions;

/**
 * Eccezione specifica di UC5.
 *
 * Viene lanciata quando il Direttore prova a licenziare un Personal Trainer in
 * una situazione non sicura per il sistema, ad esempio quando il PT ha ancora
 * corsi attivi/futuri ma non è stato indicato alcun sostituto.
 *
 * Il suo scopo è evitare una disattivazione incompleta del personale, cioè il
 * caso in cui il PT venga segnato come LICENZIATO mentre alcuni corsi restano
 * ancora assegnati a lui o rimangono senza istruttore.
 */
public class TrainerNonLicenziabileException extends Exception {

    public TrainerNonLicenziabileException(String message) {
        super(message);
    }
}
