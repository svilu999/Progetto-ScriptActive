package it.unipv.poingsfw.domain;

/**
 * Destinatario del <i>Double Dispatch</i> avviato da {@link Utente#accediAreaRiservata(InstradatoreUtente)}.
 * <p>
 * Ogni sottoclasse di {@link Utente} richiama il metodo corrispondente al proprio ruolo:
 * chi implementa questa interfaccia decide che cosa significa "aprire l'area riservata"
 * senza ricorrere a catene di {@code instanceof}.
 * </p>
 * <p>
 * L'interfaccia vive nel dominio perché è il dominio a definire i ruoli. Gli strati
 * superiori (controller) la implementano: la dipendenza va quindi dal controller verso
 * il Model e mai nel verso opposto.
 * </p>
 */
public interface InstradatoreUtente {

    /** Invocato quando l'utente autenticato è un {@link Cliente}. */
    void instradaCliente(Cliente cliente);

    /** Invocato quando l'utente autenticato è un {@link Direttore}. */
    void instradaDirettore(Direttore direttore);

    /** Invocato quando l'utente autenticato è un {@link PersonalTrainer}. */
    void instradaTrainer(PersonalTrainer trainer);
}
