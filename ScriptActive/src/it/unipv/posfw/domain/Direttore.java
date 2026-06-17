package it.unipv.posfw.domain;

public class Direttore extends Utente {
    
    private String codiceAutorizzazione;

    // COSTRUTTORE BASE (Utilizzato dal DAO durante il Login)
    public Direttore(String nome, String cognome, String email, String codiceAutorizzazione) {
        super(nome, cognome, email); 
        this.codiceAutorizzazione = codiceAutorizzazione;
    }
    @Override
    public void accediAreaRiservata(it.unipv.posfw.controller.LoginController router) {
        router.apriDashboardDirettore(this);
    }

    public String getCodiceAutorizzazione() { 
        return codiceAutorizzazione; 
    }

    public void setCodiceAutorizzazione(String codiceAutorizzazione) { 
        this.codiceAutorizzazione = codiceAutorizzazione; 
    }
}