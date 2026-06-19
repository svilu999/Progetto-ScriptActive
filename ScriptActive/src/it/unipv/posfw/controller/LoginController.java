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

public class LoginController {
    private LoginView view;
    private UtenteDAO dao;

    public LoginController(LoginView view, UtenteDAO dao) {
        this.view = view;
        this.dao = dao;
    }

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

                // ROUTING  IN BASE ALLA TIPOLOGIA DI UTENTE
                
        // Questa singola riga chiama il metodo corretto 
        utenteLoggato.accediAreaRiservata(this); 
    }

  

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
        
     
        dashboardView.btnPrenotaCorsi.addActionListener(e -> {
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