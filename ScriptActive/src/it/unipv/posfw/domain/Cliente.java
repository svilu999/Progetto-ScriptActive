package it.unipv.posfw.domain;

// Eredita da Utente e implementa Observer per mantenere il codice dei compagni intatto
public class Cliente extends Utente implements Observer {
    
    // --- VARIABILI DI ISTANZA UNIFICATE ---
    private String codiceFiscale; 
    
    // Variabili dal progetto di gruppo (GitHub)
    private TipoAbbonamento abbonamento;
    
    // Variabili dal tuo caso d'uso (Registrazione)
    private Sede sedePrincipale;
    private Abbonamento abbonamentoAttivo;

    // --- COSTRUTTORI IN OVERLOADING ---
    
    // 1. Costruttore Base (Serve al tuo DAO quando fa la ricerca per CF)
    public Cliente(String nome, String cognome, String email, String codiceFiscale) {
        super(nome, cognome, email);
        this.codiceFiscale = codiceFiscale;
    }

    // 2. Costruttore dei Compagni (Per non rompere le loro istanziazioni)
    public Cliente(String nome, String cognome, String email, String codiceFiscale, TipoAbbonamento abbonamento) {
        super(nome, cognome, email);
        this.codiceFiscale = codiceFiscale;
        this.abbonamento = abbonamento;
    }

    // 3. Costruttore per la Registrazione (Il tuo caso d'uso)
    // NOTA: Ho convertito String tipoAbbonamento in TipoAbbonamento per allinearci a loro
    public Cliente(String nome, String cognome, String email, String codiceFiscale, Sede sedePrincipale, TipoAbbonamento abbonamento) {
        super(nome, cognome, email);
        this.codiceFiscale = codiceFiscale;
        this.sedePrincipale = sedePrincipale;
        this.abbonamento = abbonamento;
    }

    // --- METODI DELL'INTERFACCIA OBSERVER (Dei compagni) ---
    @Override
    public void update(String messaggio) {
        // Presuppone che Utente abbia un metodo getNomeCompleto()
        System.out.println("[NOTIFICA a " + this.getNomeCompleto() + "]: " + messaggio);
    }

    // --- METODI SPECIFICI DEI COMPAGNI ---
    public boolean isPremium() {
        return this.abbonamento == TipoAbbonamento.PREMIUM;
    }

    public String getIdCliente() {
        return this.codiceFiscale;
    }

    // --- GETTER E SETTER UNIFICATI ---
    public String getCodiceFiscale() { 
        return codiceFiscale; 
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public Sede getSedePrincipale() {
        return sedePrincipale;
    }

    public void setSedePrincipale(Sede sedePrincipale) {
        this.sedePrincipale = sedePrincipale;
    }

    public Abbonamento getAbbonamentoAttivo() {
        return abbonamentoAttivo;
    }

    public void setAbbonamentoAttivo(Abbonamento abbonamentoAttivo) {
        this.abbonamentoAttivo = abbonamentoAttivo;
    }

    public TipoAbbonamento getAbbonamento() {
        return abbonamento;
    }

    public void setAbbonamento(TipoAbbonamento abbonamento) {
        this.abbonamento = abbonamento;
    }

    public TipoAbbonamento getTipoAbbonamento() {
        return this.abbonamento;
    }
}