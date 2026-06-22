package it.unipv.posfw.domain;


public class Cliente extends Utente implements Observer {
    
    private String codiceFiscale; 
    private TipoAbbonamento abbonamento;
    private Sede sedePrincipale;
    private Abbonamento abbonamentoAttivo;
    
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

    @Override
    public void update(String messaggio) {
        System.out.println("[NOTIFICA a " + this.getNomeCompleto() + "]: " + messaggio);
    }

    @Override
    public void accediAreaRiservata(it.unipv.posfw.controller.LoginController router) {
        router.apriDashboardCliente(this);
    }
    
    // =======================================================
    // NUOVO METODO AGGIUNTO: LOGICA DI ACCESSO
    // =======================================================

    /**
     * Sovrascrive (Override) la regola base di accesso definita nella classe Utente.
     * Il Cliente può accedere SOLO SE l'account è "Attivo" (regola del padre) 
     * E l'abbonamento non è scaduto (regola specifica del figlio).
     * @return true se l'utente ha i permessi e un abbonamento valido per loggarsi.
     */
    @Override
    public boolean puoAccedereAlSistema() {
        // 1. super.puoAccedereAlSistema() verifica se lo stato dell'account è "Attivo"
        // 2. Aggiungiamo il controllo di sicurezza su abbonamentoAttivo
        // 3. Chiamiamo il metodo isValidoOggi() che abbiamo aggiunto in Abbonamento
        return super.puoAccedereAlSistema() && 
               this.abbonamentoAttivo != null && 
               this.abbonamentoAttivo.isValidoOggi();
    }

    // =======================================================
    
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