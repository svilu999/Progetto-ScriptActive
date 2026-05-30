package it.unipv.posfw.domain;

// Eredita da Utente e implementa Observer!
public class Cliente extends Utente implements Observer {
    private String codiceFiscale;

    public Cliente(String nome, String cognome, String email, String codiceFiscale) {
        super(nome, cognome, email);
        this.codiceFiscale = codiceFiscale;
    }

    @Override
    public void update(String messaggio) {
        System.out.println("[NOTIFICA a " + this.getNomeCompleto() + "]: " + messaggio);
    }
}