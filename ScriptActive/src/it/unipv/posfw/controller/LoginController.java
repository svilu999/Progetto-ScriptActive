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
 * Controller responsabile dell'autenticazione degli utenti e del routing iniziale.
 * <p>
 * Applica il pattern architetturale MVC e utilizza la tecnica avanzata del 
 * <b>Double Dispatch (Polimorfismo)</b> per risolvere il code smell "Replace Conditional 
 * with Polymorphism". Evita catene di if-else basate sul controllo dei tipi (instanceof)
 * delegando il reindirizzamento direttamente agli oggetti di dominio.
 * </p>
 */
public class LoginController {
    private LoginView view;
    private UtenteDAO dao;

    /**
     * Costruisce il controller per la gestione del Login.
     *
     * @param view La vista (interfaccia grafica) del form di login.
     * @param dao  Il Data Access Object per la verifica delle credenziali sul database.
     */
    public LoginController(LoginView view, UtenteDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    /**
     * Gestisce il tentativo di accesso da parte di un utente.
     * Effettua i controlli preliminari sui campi, verifica le credenziali tramite DAO
     * e, in caso di successo, innesca il meccanismo polimorfico di instradamento.
     *
     * @param email    L'indirizzo email inserito dall'utente.
     * @param password La password inserita dall'utente.
     */
    public void effettuaLogin(String email, String password) {
        // 1. Controllo base
        if (email.isEmpty() || password.isEmpty()) {
            view.mostraErrore("Inserisci sia email che password.");
            return;
        }

        // 2. Chiedo al DAO di verificare nel DB
        Utente utenteLoggato = dao.effettuaLogin(email, password);

        // 3. Se l'utente non esiste, la password è sbagliata, o non è "Attivo"
        if (utenteLoggato == null) {
            view.mostraErrore("Credenziali errate o utente non attivo.");
            return;
        }

        // 4. Se il login ha successo, chiudiamo la schermata di login
        view.dispose();

        // =========================================================
        // ROUTING INGEGNERIZZATO IN BASE ALLA TIPOLOGIA DI UTENTE
        // =========================================================
        
        // Questa singola riga chiama il metodo corretto sulla base del tipo 
        // dinamico dell'utente (Cliente, Direttore o PersonalTrainer)
        utenteLoggato.accediAreaRiservata(this); 
    }


    /**
     * Inizializza e mostra la Dashboard dedicata ai Clienti.
     * Associa inoltre i listener necessari per la navigazione verso l'Area Premium
     * e verso il Palinsesto Corsi.
     * * Metodo richiamato tramite Double Dispatch dalla classe {@link Cliente}.
     *
     * @param clienteLoggato L'istanza del cliente autenticato nel sistema.
     */
    public void apriDashboardCliente(Cliente clienteLoggato) {
        DashboardClienteView dashboardView = new DashboardClienteView();
        dashboardView.impostaDatiCliente(clienteLoggato);
        
        try {
            GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni();
            List<Corso> corsiDelCliente = gestorePrenotazioni.getCorsiPrenotatiDalCliente(clienteLoggato);
            dashboardView.mostraCorsiPrenotati(corsiDelCliente);
        } catch (Exception e) {
            System.err.println("Errore nel recupero dei corsi: " + e.getMessage());
        }
        
        dashboardView.setVisible(true);
        
        // Listener per navigare allo Storico Allenamenti (Area Premium)
        dashboardView.btnAreaPremium.addActionListener(e -> {
            dashboardView.dispose();
            
            StoricoAllenamentiView premiumView = new StoricoAllenamentiView();
            SessioneDAO sessioneDAO = new SessioneDAOMySQL();
            StoricoAllenamentiController clienteController = new StoricoAllenamentiController(premiumView, sessioneDAO);
            
            premiumView.setController(clienteController);
            premiumView.setUtenteCorrente(clienteLoggato); 
            premiumView.setVisible(true);
            premiumView.clickAccediStorico(clienteLoggato);
        });
        
        // Listener per navigare al Palinsesto
        dashboardView.btnPrenotaCorsi.addActionListener(e -> {
            dashboardView.dispose();
            PalinsestoCorsiView corsiView = new PalinsestoCorsiView(); 
            corsiView.setClienteLoggato(dashboardView.getUtenteCorrente());
            corsiView.setVisible(true);
        });
    }

    /**
     * Inizializza e mostra la Dashboard dedicata al Direttore.
     * Metodo richiamato tramite Double Dispatch dalla classe {@link Direttore}.
     *
     * @param direttoreLoggato L'istanza del direttore autenticato nel sistema.
     */
    public void apriDashboardDirettore(Direttore direttoreLoggato) {
        DashboardDirettoreView direttoreView = new DashboardDirettoreView();
        direttoreView.setVisible(true);
    }

    /**
     * Inizializza e mostra il Portale Operativo (Palinsesto) dedicato ai Personal Trainer.
     * Metodo richiamato tramite Double Dispatch dalla classe {@link PersonalTrainer}.
     *
     * @param trainerLoggato L'istanza del trainer autenticato nel sistema.
     */
    public void apriDashboardTrainer(PersonalTrainer trainerLoggato) {
        PalinsestoCorsiView trainerView = new PalinsestoCorsiView();
        GestoreCorsi controllerCorsi = GestoreCorsi.getInstance();
        trainerView.setVisible(true);
    }
}