package it.unipv.posfw.domain;

import it.unipv.posfw.strategy.StrategiaRetribuzione;

public class PersonalTrainer extends Utente {
    
    // --- DATI DI LORENZO ---
    private String idTrainer;
    private String specializzazione;
    
    // --- DATI DEL TUO CASO D'USO (UC5) ---
    private StrategiaRetribuzione strategia;
    private String statoContratto; 
    private boolean isAttivo;

    // =========================================================
    // COSTRUTTORE 1: Il tuo (Quello completo da 6 parametri)
    // =========================================================
    public PersonalTrainer(String nome, String cognome, String email, String idTrainer, String specializzazione, StrategiaRetribuzione strategia) {
        super(nome, cognome, email); 
        this.idTrainer = idTrainer;
        this.specializzazione = specializzazione;
        this.strategia = strategia;
        this.statoContratto = "ATTIVO";
        this.isAttivo = true;
    }

    // =========================================================
    // COSTRUTTORE 2: Quello di Lorenzo (Legacy da 4 parametri)
    // =========================================================
    public PersonalTrainer(String nome, String cognome, String email, String idTrainer) {
        super(nome, cognome, email); 
        this.idTrainer = idTrainer;
        
        // Dati di default per evitare che il programma crashi
        this.specializzazione = "Non definita";
        this.strategia = null;
        this.statoContratto = "ATTIVO";
        this.isAttivo = true;
    }

    // --- GETTER E SETTER ---
    public String getIdTrainer() { return idTrainer; }
    
    public String getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(String specializzazione) { this.specializzazione = specializzazione; }
    
    public StrategiaRetribuzione getStrategia() { return strategia; }
    public void setStrategia(StrategiaRetribuzione strategia) { this.strategia = strategia; }
    
    public String getStatoContratto() { return statoContratto; }
    public void setStatoContratto(String statoContratto) { this.statoContratto = statoContratto; }
    
    public boolean isAttivo() { return isAttivo; }
    public void setAttivo(boolean isAttivo) { this.isAttivo = isAttivo; }

    // --- METODO DI BUSINESS ---
    public double calcolaStipendioMensile(int corsi) {
        if (strategia != null) {
            return strategia.calcolaStipendio(corsi);
        }
        return 0.0;
    }
}