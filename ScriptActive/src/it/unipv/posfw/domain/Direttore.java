package it.unipv.posfw.domain;

public class Direttore extends Utente {
    private String codiceAutorizzazione;

    public Direttore(String nome, String cognome, String email, String codiceAutorizzazione) {
        super(nome, cognome, email); // Chiama il costruttore del padre (Utente)
        this.codiceAutorizzazione = codiceAutorizzazione;
    }
}