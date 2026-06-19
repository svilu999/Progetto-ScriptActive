package it.unipv.posfw.domain;

import it.unipv.posfw.strategy.StrategiaRetribuzione;

public class PersonalTrainer extends Utente {
    

    private String idTrainer;
    private String specializzazione;
    private String tipoContratto;
    
    private StrategiaRetribuzione strategia;
    private String statoContratto; 
    private boolean isAttivo;

    // COSTRUTTORE 1
    // Da usare quando si creano Trainer con una strategia di stipendio definita
    public PersonalTrainer(String nome, String cognome, String email, String idTrainer, String specializzazione, StrategiaRetribuzione strategia) {
        super(nome, cognome, email); 
        this.idTrainer = idTrainer;
        this.specializzazione = specializzazione;
        this.strategia = strategia;
        this.statoContratto = "ATTIVO";
        this.isAttivo = true;
    }

    // COSTRUTTORE 2
    // Da usare quando l'utente fa il login
    public PersonalTrainer(String nome, String cognome, String email, String idTrainer) {
        super(nome, cognome, email); 
        this.idTrainer = idTrainer;
        
        // Dati di default per evitare dei crash (NullPointerException)
        this.specializzazione = "Non definita";
        this.strategia = null;
        this.statoContratto = "ATTIVO";
        this.isAttivo = true;
    }

    // GETTER E SETTER
    public String getIdTrainer() { return idTrainer; }
    public void setIdTrainer(String idTrainer) { this.idTrainer = idTrainer; }
    
    public String getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(String specializzazione) { this.specializzazione = specializzazione; }

    public String getTipoContratto() { return tipoContratto; }
    public void setTipoContratto(String tipoContratto) { this.tipoContratto = tipoContratto; }
    
    public StrategiaRetribuzione getStrategia() { return strategia; }
    public void setStrategia(StrategiaRetribuzione strategia) { this.strategia = strategia; }
    
    public String getStatoContratto() { return statoContratto; }
    public void setStatoContratto(String statoContratto) { this.statoContratto = statoContratto; }
    
    public boolean isAttivo() { return isAttivo; }
    public void setAttivo(boolean isAttivo) { this.isAttivo = isAttivo; }
    
    @Override
    public void accediAreaRiservata(it.unipv.posfw.controller.LoginController router) {
        router.apriDashboardTrainer(this);
    }

    // METODO DI BUSINESS
    public double calcolaStipendioMensile(int corsi) {
        if (strategia != null) {
            return strategia.calcolaStipendio(corsi);
        }
        return 0.0;
    }
}