package it.unipv.posfw.controller;

import it.unipv.posfw.database.UtenteDAO;
import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.dao.SessioneDAOSQL;

// Importiamo i modelli
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Direttore;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.domain.Utente;

// Importiamo le View
import it.unipv.posfw.view.LoginView;
import it.unipv.posfw.view.StoricoAllenamentiView;
import it.unipv.posfw.view.DashboardDirettoreView; 

// ATTENZIONE: Se la classe del Trainer si chiama diversamente, cambia questo import!
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
        // 5. SMISTAMENTO (ROUTING) IN BASE ALLA CLASSE DELL'UTENTE
        // =========================================================
        
        if (utenteLoggato instanceof Cliente) {
            // --- AREA RISERVATA CLIENTE ---
            StoricoAllenamentiView clienteView = new StoricoAllenamentiView();
            SessioneDAO sessioneDAO = new SessioneDAOSQL();
            StoricoAllenamenti clienteController = new StoricoAllenamenti(clienteView, sessioneDAO);
            
            clienteView.setController(clienteController);
            clienteView.setUtenteCorrente((Cliente) utenteLoggato); 
            
            clienteView.setVisible(true);
            clienteView.clickAccediStorico((Cliente) utenteLoggato);
            
        } else if (utenteLoggato instanceof Direttore) {
            // --- AREA RISERVATA DIRETTORE ---
            DashboardDirettoreView direttoreView = new DashboardDirettoreView();
            
            // SE HAI UN CONTROLLER PER IL DIRETTORE, ISTANZIALO QUI IN FUTURO
            
            direttoreView.setVisible(true);
            
        } else if (utenteLoggato instanceof PersonalTrainer) {
            // --- AREA RISERVATA PERSONAL TRAINER ---
            
            // Crea la finestra del Trainer
            PalinsestoCorsiView trainerView = new PalinsestoCorsiView();
            
            // SE I TUOI COMPAGNI HANNO CREATO IL CONTROLLER, ISTANZIALO QUI IN FUTURO
            
            // Rende visibile la finestra
            trainerView.setVisible(true);
            
            System.out.println("Accesso effettuato come Personal Trainer: " + utenteLoggato.getNome());
        }
    }
}