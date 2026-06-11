package it.unipv.posfw.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Corso implements Subject {

    private String idCorso;
    private String nome;
    private LocalDateTime dataOra;
    private int postiDisponibili;
    private int capienzaMassima;

    // Associazioni strutturali derivate dal diagramma UML.
    private StatoCorso stato;
    private PersonalTrainer trainerAssegnato;
    private List<Observer> iscritti;
    private List<Prenotazione> prenotazioni;

    public Corso(String idCorso, String nome, LocalDateTime orario, int capienzaMassima, PersonalTrainer trainerAssegnato) {
        this.idCorso = idCorso;
        this.nome = nome;
        this.dataOra = orario;
        this.capienzaMassima = capienzaMassima;
        this.postiDisponibili = capienzaMassima;
        this.trainerAssegnato = trainerAssegnato;
        this.stato = StatoCorso.ATTIVO;

        this.iscritti = new ArrayList<>();
        this.prenotazioni = new ArrayList<>();
    }

    // =========================================================
    // UC3 - gestione posti del corso
    // =========================================================
    public void decrementaPosti() {
        if (postiDisponibili > 0) {
            postiDisponibili--;
        }
    }

    public void incrementaPosti() {
        if (postiDisponibili < capienzaMassima) {
            postiDisponibili++;
        }
    }

    public boolean alCompleto() {
        return postiDisponibili == 0;
    }

    // =========================================================
    // Pattern Observer
    // =========================================================
    @Override
    public void attach(Observer o) {
        if (!alCompleto() && !iscritti.contains(o)) {
            iscritti.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        iscritti.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : iscritti) {
            o.update("ATTENZIONE: Il corso '" + this.nome + "' ha subito variazioni.");
        }
    }

    // =========================================================
    // UC5 - punto di merge con Gestione Personale
    // =========================================================
    public void setTrainerAssegnato(PersonalTrainer trainerAssegnato) {
        /*
         * Merge UC5:
         * quando un Personal Trainer viene licenziato, il corso NON deve essere
         * cancellato. Lo swap cambia solo il trainer assegnato, preservando
         * id corso, data, posti, iscritti e stato del corso.
         */
        this.trainerAssegnato = trainerAssegnato;
        notifyObservers();
    }

    // =========================================================
    // Getter e setter di dominio
    // =========================================================
    public String getIdCorso() {
        return idCorso;
    }

    public String getNome() {
        return nome;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public int getPostiDisponibili() {
        return postiDisponibili;
    }

    public void setPostiDisponibili(int postiDisponibili) {
        this.postiDisponibili = postiDisponibili;
    }

    public int getCapienzaMassima() {
        return capienzaMassima;
    }

    public void setCapienzaMassima(int capienzaMassima) {
        this.capienzaMassima = capienzaMassima;
    }

    public StatoCorso getStato() {
        return stato;
    }

    public void setStato(StatoCorso stato) {
        this.stato = stato;
        if (this.stato == StatoCorso.CANCELLATO) {
            notifyObservers();
        }
    }

    public PersonalTrainer getTrainerAssegnato() {
        return trainerAssegnato;
    }
}