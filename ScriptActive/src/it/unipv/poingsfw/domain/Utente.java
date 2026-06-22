package it.unipv.poingsfw.domain;

/**
 * Classe astratta che rappresenta un utente generico del sistema.
 * Fornisce gli attributi comuni e la logica base per l'autenticazione.
 */
public abstract class Utente {
    
    // Visibilità protected così le classi figlie (Cliente, PT, ecc.) possono accedervi direttamente
    protected String nome;
    protected String cognome;
    protected String email;
    
    // Dal progetto di gruppo (per ID database e sicurezza)
    private int id;
    protected String passwordHash;
    
    // Dal tuo caso d'uso locale (per la gestione della password in fase di registrazione)
    protected String password;

    // --- NUOVO ATTRIBUTO: STATO DELL'ACCOUNT ---
    // Mappa la colonna "Stato" ("Attivo" o "Inattivo") nel database
    protected String stato;

    // --- COSTRUTTORE UNIFICATO ---
    public Utente(String nome, String cognome, String email) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
    }
 
    /**
     * Metodo astratto per instradare l'utente alla corretta area riservata.
     * Ogni classe figlia (Cliente, PT, Direttore) fornirà la propria implementazione.
     * * @param router Il controller che gestisce il cambio vista.
     */
    public abstract void accediAreaRiservata(it.unipv.poingsfw.controller.LoginController router);
    
    // =======================================================
    // METODI DI LOGICA DI BUSINESS (BLOCCO ACCOUNT)
    // =======================================================

    /**
     * Controlla se l'account dell'utente è stato disabilitato manualmente dal Direttore.
     * * @return true se lo stato è "Attivo", false in caso contrario.
     */
    public boolean isAccountAbilitato() {
        if (this.stato == null) {
            return false;
        }
        // equalsIgnoreCase protegge da eventuali errori di battitura nel DB (es. "attivo" vs "Attivo")
        return this.stato.equalsIgnoreCase("Attivo");
    }

    /**
     * Regola base per accedere al sistema.
     * Un Utente generico (Direttore o PT) può accedere solo se il suo account è abilitato.
     * NOTA: Questa regola verrà sovrascritta (Override) nella classe Cliente per controllare anche l'abbonamento.
     * * @return true se l'utente ha i permessi per loggarsi.
     */
    public boolean puoAccedereAlSistema() {
        return this.isAccountAbilitato();
    }

    // =======================================================
    // GETTER E SETTER
    // =======================================================
    
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public String getCognome() { 
        return cognome; 
    }
    
    public void setCognome(String cognome) { 
        this.cognome = cognome; 
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getPasswordHash() { 
        return passwordHash; 
    }
    
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}