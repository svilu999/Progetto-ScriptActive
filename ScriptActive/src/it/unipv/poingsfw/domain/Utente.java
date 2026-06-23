package it.unipv.poingsfw.domain;

/**
 * La classe astratta {@code Utente} definisce il nucleo strutturale e comportamentale 
 * di qualsiasi entità "umana" in grado di interagire con il sistema.
 * <p>
 * Agisce come <b>Superclasse (Base Class)</b> all'interno della gerarchia di ereditarietà, 
 * incapsulando gli attributi anagrafici comuni e le logiche condivise di autenticazione 
 * e autorizzazione. Non essendo pensata per essere istanziata direttamente 
 * (motivo per cui è dichiarata {@code abstract}), funge da "stampo" per le classi figlie 
 * (es. {@code Cliente}, {@code PersonalTrainer}).
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public abstract class Utente {
    
    protected String nome;
    protected String cognome;
    protected String email;
    
    private int id;
    protected String passwordHash;
    
    protected String password;

    protected String stato;

    /**
     * Costruttore parametrizzato parziale. 
     * Inizializza i campi anagrafici obbligatori comuni a tutte le tipologie di utente.
     * * @param nome    Il nome proprio dell'utente.
     * @param cognome Il cognome dell'utente.
     * @param email   L'indirizzo email, utilizzato anche come credenziale (Username) di accesso.
     */
    
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
    
    /**
     * Controlla se l'account dell'utente è stato disabilitato manualmente dal Direttore.
     * * @return true se lo stato è "Attivo", false in caso contrario.
     */
    
    public boolean isAccountAbilitato() {
        if (this.stato == null) {
            return false;
        }
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