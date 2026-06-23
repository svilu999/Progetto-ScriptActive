package it.unipv.poingsfw.domain;

/**
 * La classe {@code Cliente} rappresenta un utente registrato che ha sottoscritto un abbonamento.
 * <p>
 * Sfrutta il concetto di <b>Ereditarietà</b> estendendo la classe base {@link Utente}, 
 * da cui eredita attributi anagrafici e logiche di base, aggiungendo peculiarità specifiche 
 * come il codice fiscale, la sede di appartenenza e lo stato dell'abbonamento.
 * </p>
 * <p>
 * Implementa inoltre l'interfaccia {@link Observer}, inserendosi all'interno del pattern 
 * architetturale <b>Observer</b> per la ricezione di notifiche da parte del sistema 
 * (es. avvisi di scadenza abbonamento, promozioni).
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class Cliente extends Utente implements Observer {
    
    private String codiceFiscale; 
    private TipoAbbonamento abbonamento;
    private Sede sedePrincipale;
    private Abbonamento abbonamentoAttivo;
   
    /**
     * Costruttore base. Utilizza {@code super()} per delegare l'inizializzazione 
     * dei campi anagrafici alla superclasse {@code Utente}.
     */
    
    public Cliente(String nome, String cognome, String email, String codiceFiscale) {
        super(nome, cognome, email);
        this.codiceFiscale = codiceFiscale;
    }

    
    public Cliente(String nome, String cognome, String email, String codiceFiscale, TipoAbbonamento abbonamento) {
        super(nome, cognome, email);
        this.codiceFiscale = codiceFiscale;
        this.abbonamento = abbonamento;
    }

    public Cliente(String nome, String cognome, String email, String codiceFiscale, Sede sedePrincipale, TipoAbbonamento abbonamento) {
        super(nome, cognome, email);
        this.codiceFiscale = codiceFiscale;
        this.sedePrincipale = sedePrincipale;
        this.abbonamento = abbonamento;
    }

    /**
     * Metodo richiesto dall'interfaccia {@code Observer}.
     * Permette al cliente di ricevere messaggi in tempo reale dal sistema (Subject).
     * * @param messaggio Il contenuto della notifica da mostrare al cliente.
     */
    
    @Override
    public void update(String messaggio) {
        System.out.println("[NOTIFICA a " + this.getNomeCompleto() + "]: " + messaggio);
    }

    /**
     * Logica di routing per l'interfaccia utente. 
     * Sfrutta il polimorfismo per indirizzare il sistema verso la dashboard corretta.
     */
    
    @Override
    public void accediAreaRiservata(it.unipv.poingsfw.controller.LoginController router) {
        router.apriDashboardCliente(this);
    }
    
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

    /**
     * @return {@code true} se il cliente ha sottoscritto un piano Premium.
     */
    
    public boolean isPremium() {
        return this.abbonamento == TipoAbbonamento.PREMIUM;
    }

    public String getIdCliente() {
        return this.codiceFiscale;
    }

    // --- GETTER E SETTER ---
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

	public void setPremium(boolean b) {
		
		
	}
}