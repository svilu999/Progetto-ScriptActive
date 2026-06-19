package it.unipv.posfw.domain;

/**
 * Rappresenta l'entità centrale del Modello di Dominio per la gestione delle attività della palestra.
 * 
 * In conformità con l'architettura Model-View-Controller (MVC), questa classe funge da "Model", 
 * astraendo lo stato e il comportamento dei dati di business. È implementata come un Plain Old 
 * Java Object (POJO), garantendo che la logica di dominio sia pura e disaccoppiata da 
 * infrastrutture di persistenza o di presentazione.
 * 
 * La classe agisce come "ConcreteSubject" nel Pattern Observer: mantiene 
 * un registro di osservatori e provvede a notificarli 
 * automaticamente in caso di variazioni dello stato interno (meccanismo "Push from below"), 
 * come previsto nei diagrammi di sequenza per la cancellazione di un corso.
 * 
 * @author Lorenzo
 * @version 2.0
 * @see Subject
 * @see Observer
 */
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
    // Gestione posti del corso
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

    public void setTrainerAssegnato(PersonalTrainer trainerAssegnato) {

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