package it.unipv.posfw.domain;

public class PersonalTrainer extends Utente {
    private String idTrainer;
    private String specializzazione;

    public PersonalTrainer(String nome, String cognome, String email, String idTrainer) {
        super(nome, cognome, email);
        this.idTrainer = idTrainer;
    }

    public String getIdTrainer() {
        return idTrainer;
    }
}