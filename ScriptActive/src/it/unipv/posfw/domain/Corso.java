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
    
    // Associazioni Strutturali derivate dal nuovo Diagramma
    private StatoCorso stato;
    private PersonalTrainer trainerAssegnato;
    private List<Observer> iscritti;
    private List<Prenotazione> prenotazioni; // Nuova associazione

    // Costruttore aggiornato secondo la firma UML
    public Corso(String idCorso, String nome, LocalDateTime orario, int capienzaMassima, PersonalTrainer trainerAssegnato) {
        this.idCorso = idCorso;
        this.nome = nome;
        this.dataOra = orario;
        this.capienzaMassima = capienzaMassima;
        this.postiDisponibili = capienzaMassima; // All'inizio i posti disponibili sono pari alla capienza
        this.trainerAssegnato = trainerAssegnato;
        this.stato = StatoCorso.ATTIVO;
        
        this.iscritti = new ArrayList<>();
        this.prenotazioni = new ArrayList<>();
    }

    // --- NUOVI METODI UML PER LA GESTIONE POSTI ---
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

    // --- METODI OBSERVER (SUBJECT) ---
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

    // --- GETTER & SETTER DI BUSINESS ---
    public String getIdCorso() {
        return idCorso;
    }

    public StatoCorso getStato() {
        return stato;
    }

    public void setStato(StatoCorso stato) {
        this.stato = stato;
        if (this.stato == StatoCorso.CANCELLATO) {
            notifyObservers(); // Automazione notifica
        }
    }
    
    public PersonalTrainer getTrainerAssegnato() {
        return trainerAssegnato;
    }
    
    public LocalDateTime getDataOra() {
        return dataOra;
    }
    public String getNome() {
        return nome;
    }
    public int getCapienzaMassima() {
        return capienzaMassima;
    }

    public int getPostiDisponibili() {
        return postiDisponibili;
    }
}