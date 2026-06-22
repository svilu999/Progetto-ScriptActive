package it.unipv.poingsfw.domain;
/**
 * Rappresenta l'entità "Direttore" all'interno del Modello di Dominio.
 * 
 * In conformità con l'architettura Model-View-Controller (MVC) illustrata nelle lezioni, 
 * questa classe funge da "Model" (nello specifico una Entity), incapsulando lo stato 
 * e i dati di business relativi a una specifica tipologia di utenza. È implementata 
 * come un Plain Old Java Object (POJO), garantendo l'indipendenza dalla logica di 
 * persistenza e presentazione.
 * 
 * La classe sfrutta il principio dell'Ereditarietà per estendere la classe base Utente, 
 * realizzando il polimorfismo necessario a gestire i diversi flussi di accesso all'area 
 * riservata senza ricorrere a logica condizionale dipendente dal tipo, evitando così 
 * anti-pattern di progettazione.
 * 
 * @author Lorenzo
 * @version 2.0
 * @see Utente
 */

public class Direttore extends Utente {
    
    private String codiceAutorizzazione;

    // COSTRUTTORE BASE (Utilizzato dal DAO durante il Login)
    public Direttore(String nome, String cognome, String email, String codiceAutorizzazione) {
        super(nome, cognome, email); 
        this.codiceAutorizzazione = codiceAutorizzazione;
    }
    @Override
    public void accediAreaRiservata(it.unipv.poingsfw.controller.LoginController router) {
        router.apriDashboardDirettore(this);
    }

    public String getCodiceAutorizzazione() { 
        return codiceAutorizzazione; 
    }

    public void setCodiceAutorizzazione(String codiceAutorizzazione) { 
        this.codiceAutorizzazione = codiceAutorizzazione; 
    }
}