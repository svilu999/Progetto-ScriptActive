package it.unipv.posfw.domain;

import it.unipv.posfw.strategy.StrategiaRetribuzione;

public class PersonalTrainer extends Utente {
    
    // --- DATI DI LORENZO E DATABASE ---
    private String idTrainer; // Spesso usato per il Codice Fiscale
    private String specializzazione;
    private String tipoContratto; // Aggiunto per non perdere il dato del DB (es. Part-Time)
    
    // --- DATI DEL TUO CASO D'USO (UC5) ---
    private StrategiaRetribuzione strategia;
    private String statoContratto; 
    private boolean isAttivo;

    // =========================================================
    // COSTRUTTORE 1: Il tuo (Quello completo per UC5)
    // =========================================================
    // Da usare quando si creano Trainer con una strategia di stipendio definita
    public PersonalTrainer(String nome, String cognome, String email, String idTrainer, String specializzazione, StrategiaRetribuzione strategia) {
        super(nome, cognome, email); 
        this.idTrainer = idTrainer;
        this.specializzazione = specializzazione;
        this.strategia = strategia;
        this.statoContratto = "ATTIVO";
        this.isAttivo = true;
    }

    // =========================================================
    // COSTRUTTORE 2: Quello di Lorenzo e del nostro DAO (Login)
    // =========================================================
    // Il nostro UtenteDAOMySQL userà in automatico questo quando l'utente fa Login!
    public PersonalTrainer(String nome, String cognome, String email, String idTrainer) {
        super(nome, cognome, email); 
        this.idTrainer = idTrainer;
        
        // Dati di default per evitare che il programma crashi (NullPointerException)
        this.specializzazione = "Non definita";
        this.strategia = null;
        this.statoContratto = "ATTIVO";
        this.isAttivo = true;
    }

    // =========================================================
    // GETTER E SETTER (Per far felici tutti i framework e DB)
    // =========================================================
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

    // =========================================================
    // METODO DI BUSINESS (Intatto!)
    // =========================================================
    public double calcolaStipendioMensile(int corsi) {
        if (strategia != null) {
            return strategia.calcolaStipendio(corsi);
        }
        return 0.0;
    }
}