package it.unipv.poingsfw.domain;

/**
 * L'enumerazione {@code TipoAbbonamento} definisce i diversi livelli di servizio 
 * offerti dal sistema ai clienti.
 * <p>
 * Come per le durate, l'utilizzo di un <b>Enum</b> per mappare i piani di abbonamento 
 * garantisce la <i>Type Safety</i> e centralizza le opzioni disponibili.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public enum TipoAbbonamento {
    BASE,
    PREMIUM
}
