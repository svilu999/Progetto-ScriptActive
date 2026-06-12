package it.unipv.posfw.domain;

// Eredita da Utente e implementa Observer
public class Cliente extends Utente implements Observer {
    
    // Variabili di istanza
    private String codiceFiscale; // Funge anche da idCliente
    private TipoAbbonamento abbonamento;

    // COSTRUTTORE UNIFICATO
    public Cliente(String nome, String cognome, String email, String codiceFiscale, TipoAbbonamento abbonamento) {
        super(nome, cognome, email); // Richiama il costruttore della superclasse Utente
        this.codiceFiscale = codiceFiscale;
        this.abbonamento = abbonamento;
    }

    // --- METODI DELL'INTERFACCIA OBSERVER ---
    @Override
    public void update(String messaggio) {
        // Presuppone che Utente abbia un metodo getNomeCompleto()
        System.out.println("[NOTIFICA a " + this.getNomeCompleto() + "]: " + messaggio);
    }

    // --- METODI SPECIFICI DEL CLIENTE ---
    public boolean isPremium() {
        return this.abbonamento == TipoAbbonamento.PREMIUM;
    }

    // Sostituisce il vecchio getIdCliente(), restituendo direttamente il codice fiscale
    public String getCodiceFiscale() { 
        return codiceFiscale; 
    }

    // Metodo di "retrocompatibilità": se nel resto del progetto avete già usato 
    // l'espressione cliente.getIdCliente(), questo metodo evita che il codice si rompa.
    public String getIdCliente() {
        return this.codiceFiscale;
    }

    // --- GETTER E SETTER AGGIUNTIVI ---
    public TipoAbbonamento getAbbonamento() {
        return abbonamento;
    }

    public void setAbbonamento(TipoAbbonamento abbonamento) {
        this.abbonamento = abbonamento;
    }

    // ECCO IL METODO AGGIUNTO PER RISOLVERE L'ERRORE!
    public TipoAbbonamento getTipoAbbonamento() {
        return this.abbonamento;
    }
}