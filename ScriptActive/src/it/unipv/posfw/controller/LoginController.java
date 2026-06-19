package it.unipv.posfw.controller;

import java.util.List;

import it.unipv.posfw.database.SessioneDAOMySQL;
import it.unipv.posfw.database.UtenteDAO;
import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso; 
import it.unipv.posfw.domain.Direttore;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.domain.Utente;
import it.unipv.posfw.view.LoginView;
import it.unipv.posfw.view.StoricoAllenamentiView;
import it.unipv.posfw.view.DashboardDirettoreView; 
import it.unipv.posfw.view.DashboardClienteView; 
import it.unipv.posfw.view.PalinsestoCorsiView; 

/**
 * La classe {@code LoginController} incarna il ruolo di <b>Controller</b> all'interno del pattern architetturale <b>MVC (Model-View-Controller)</b>.
 * <p>
 * Presiede alla gestione della fase iniziale del ciclo di vita dell'applicazione (Autenticazione).
 * Il suo compito è validare gli input della View, interrogare il Modello di persistenza (DAO) e governare 
 * le transizioni di stato della Graphical User Interface (Routing).
 * </p>
 * <p>
 * <b>Valenza Ingegneristica (Double Dispatch e Polimorfismo):</b><br>
 * Risolve proattivamente l'anti-pattern algoritmico basato sull'introspezione dei tipi (RTTI) e sulle catene 
 * decisionali (<i>Code Smell: Replace Conditional with Polymorphism</i>). Anziché utilizzare blocchi {@code if-else} 
 * con operatori {@code instanceof} per determinare quale dashboard aprire, il controller avvia il meccanismo di 
 * <b>Double Dispatch</b> delegando all'oggetto di dominio il compito di instradare il flusso chiamando a sua volta 
 * il metodo appropriato su questo controller. Questo garantisce il rispetto del principio Open/Closed (OCP) dei principi SOLID.
 * </p>
 * * @author Vilucchi
 * @version 1.2
 * @see it.unipv.posfw.view.LoginView
 * @see it.unipv.posfw.domain.Utente
 */
public class LoginController {
    
    private LoginView view;
    private UtenteDAO dao;
    private Utente utenteLoggato; 

    /**
     * Costruttore della classe {@code LoginController}.
     */
    public LoginController(LoginView view, UtenteDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    /**
     * Governa il flusso principale del caso d'uso di Autenticazione (Login).
     */
    public void effettuaLogin(String email, String password) {
        
        /* 1. Validazione sintattica pre-condizionale */
        if (email.isEmpty() || password.isEmpty()) {
            view.mostraErrore("Inserisci sia email che password.");
            return;
        }

        /* 2. Delega al livello di persistenza */
        Utente utente = dao.effettuaLogin(email, password);

        /* 3. Gestione del Flusso Alternativo: fallimento dell'autenticazione */
        if (utente == null) {
            this.utenteLoggato = null; // Assicuriamo che sia nullo in caso di fallimento
            view.mostraErrore("Credenziali errate o utente non attivo.");
            return;
        }
        
        /* ASSEGNAZIONE PER IL TEST E PER IL DOMINIO */
        this.utenteLoggato = utente;

        /* 4. Smontaggio della View di Login */
        view.dispose();

        /* 5. Innesco Double Dispatch */
        utenteLoggato.accediAreaRiservata(this); 
    }

    public Utente getUtenteLoggato() {
        return this.utenteLoggato;
    }

    /**
     * Punto di atterraggio del pattern Double Dispatch per il ruolo di attore <b>Cliente</b>.
     */
    public void apriDashboardCliente(Cliente clienteLoggato) {
        DashboardClienteView dashboardView = new DashboardClienteView();
        dashboardView.impostaDatiCliente(clienteLoggato);
        
        try {
            GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni();
            List<Corso> corsiDelCliente = gestorePrenotazioni.getCorsiPrenotatiDalCliente(clienteLoggato);
            dashboardView.mostraCorsiPrenotati(corsiDelCliente);
        } catch (Exception e) {
            System.err.println("Errore architetturale nel recupero dei corsi: " + e.getMessage());
        }
        
        dashboardView.setVisible(true);
        
        dashboardView.addAreaPremiumListener(e -> {
            dashboardView.dispose();
            
            StoricoAllenamentiView premiumView = new StoricoAllenamentiView();
            SessioneDAO sessioneDAO = new SessioneDAOMySQL();
            StoricoAllenamentiController clienteController = new StoricoAllenamentiController(premiumView, sessioneDAO);
            
            premiumView.setController(clienteController);
            premiumView.setUtenteCorrente(clienteLoggato); 
            premiumView.setVisible(true);
            premiumView.clickAccediStorico(clienteLoggato);
        });
        
        dashboardView.addPrenotaCorsiListener(e -> {
            dashboardView.dispose();
            PalinsestoCorsiView corsiView = new PalinsestoCorsiView(); 
            corsiView.setClienteLoggato(dashboardView.getUtenteCorrente());
            corsiView.setVisible(true);
        });
    }

    public void apriDashboardDirettore(Direttore direttoreLoggato) {
        DashboardDirettoreView direttoreView = new DashboardDirettoreView();
        direttoreView.setVisible(true);
    }

    public void apriDashboardTrainer(PersonalTrainer trainerLoggato) {
        PalinsestoCorsiView trainerView = new PalinsestoCorsiView();
        GestoreCorsi controllerCorsi = GestoreCorsi.getInstance();
        trainerView.setVisible(true);
    }
}