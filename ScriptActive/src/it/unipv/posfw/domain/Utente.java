package it.unipv.posfw.domain;

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

    // --- COSTRUTTORE UNIFICATO ---
    public Utente(String nome, String cognome, String email) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
    }

    // --- METODI SPECIFICI DEL PROGETTO DI GRUPPO ---
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    // --- GETTER E SETTER (Unione di tutti i metodi) ---
    
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

    // Metodi per la tua registrazione
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }

    // Metodi per il login/sicurezza dei tuoi compagni
    public String getPasswordHash() { 
        return passwordHash; 
    }
    
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }
}