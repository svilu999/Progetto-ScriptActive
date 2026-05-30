package it.unipv.posfw.domain;

public abstract class Utente {
    // Visibilità protected così le classi figlie (Cliente, PT) possono accedervi
    protected String nome;
    protected String cognome;
    protected String email;
    protected String passwordHash;

    public Utente(String nome, String cognome, String email) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
    }

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
    
    public String getEmail() {
        return email;
    }
}