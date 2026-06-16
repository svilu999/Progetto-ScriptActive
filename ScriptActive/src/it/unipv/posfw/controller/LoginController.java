package it.unipv.posfw.controller;

import java.util.List; // Import per la lista dei corsi

import it.unipv.posfw.database.UtenteDAO;
import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.dao.SessioneDAOSQL;
// Importiamo i modelli
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso; // Import per il corso
import it.unipv.posfw.domain.Direttore;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.domain.Utente;

// Importiamo le View e i Controller
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

        // =========================================================
        // ROUTING IN BASE ALLA CLASSE DELL'UTENTE
        // =========================================================
        
        if (utenteLoggato instanceof Cliente) {
            // --- AREA RISERVATA CLIENTE (ATTERRAGGIO SULLA DASHBOARD) ---
            Cliente clienteLoggato = (Cliente) utenteLoggato;
            
            DashboardClienteView dashboardView = new DashboardClienteView();
            dashboardView.impostaDatiCliente(clienteLoggato);
            
            // -------------------------------------------------------------
            // RECUPERO E STAMPA DEI CORSI PRENOTATI DAL DB
            // -------------------------------------------------------------
            try {
                GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni();
                List<Corso> corsiDelCliente = gestorePrenotazioni.getCorsiPrenotatiDalCliente(clienteLoggato);
                dashboardView.mostraCorsiPrenotati(corsiDelCliente);
            } catch (Exception e) {
                System.err.println("Errore nel recupero dei corsi: " + e.getMessage());
            }
            // -------------------------------------------------------------

            dashboardView.setVisible(true);
            
            // Bottone "LA MIA AREA PREMIUM"
            dashboardView.btnAreaPremium.addActionListener(e -> {
                dashboardView.dispose();
                
                StoricoAllenamentiView premiumView = new StoricoAllenamentiView();
                SessioneDAO sessioneDAO = new SessioneDAOSQL();
                StoricoAllenamenti clienteController = new StoricoAllenamenti(premiumView, sessioneDAO);
                
                premiumView.setController(clienteController);
                premiumView.setUtenteCorrente(clienteLoggato); 
                premiumView.setVisible(true);
                premiumView.clickAccediStorico(clienteLoggato);
            });
            
            // Bottone "VAI AL PALINSESTO CORSI"
            dashboardView.btnPrenotaCorsi.addActionListener(e -> {
                dashboardView.dispose();
                
             
                PalinsestoCorsiView corsiView = new PalinsestoCorsiView(); 
                
              
                corsiView.setClienteLoggato(dashboardView.getUtenteCorrente());
                
                // Opzionale: se i compagni hanno il loro controller
                GestoreCorsi controllerCorsi = GestoreCorsi.getInstance();
                // corsiView.setController(controllerCorsi); 
                
                corsiView.setVisible(true);
            });
            
        } else if (utenteLoggato instanceof Direttore) {
            // --- AREA RISERVATA DIRETTORE ---
            DashboardDirettoreView direttoreView = new DashboardDirettoreView();
            direttoreView.setVisible(true);
            
        } else if (utenteLoggato instanceof PersonalTrainer) {
            // --- AREA RISERVATA PERSONAL TRAINER --
            PalinsestoCorsiView trainerView = new PalinsestoCorsiView();
            
            // ECCO L'ISTANZIAZIONE DEL CONTROLLER SINGLETON DEI TUOI COLLEGHI:
            GestoreCorsi controllerCorsi = GestoreCorsi.getInstance();
            
        
            
            trainerView.setVisible(true);
                    }
    }
}