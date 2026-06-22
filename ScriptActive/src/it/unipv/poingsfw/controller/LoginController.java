package it.unipv.poingsfw.controller;

import it.unipv.poingsfw.dao.UtenteDAO;
import it.unipv.poingsfw.database.SessioneDAOMySQL;
import it.unipv.poingsfw.database.UtenteDAOMySQL;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Direttore;
import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.domain.Utente;
import it.unipv.poingsfw.exceptions.AbbonamentoScadutoException;
import it.unipv.poingsfw.exceptions.AccountInattivoException;
import it.unipv.poingsfw.exceptions.CredenzialiErrateException;
import it.unipv.poingsfw.view.DashboardClienteView;
import it.unipv.poingsfw.view.DashboardDirettoreView;
import it.unipv.poingsfw.view.LoginView;
import it.unipv.poingsfw.view.PalinsestoCorsiView;
import it.unipv.poingsfw.view.StoricoAllenamentiView;

/**
 * Controller di autenticazione avanzato con gestione del ciclo di vita della sessione.
 * <p>
 * Questa classe orchestra il flusso di login non solo validando le credenziali, ma applicando 
 * policy di dominio dinamiche (rinnovo abbonamento, verifica stato account). 
 * </p>
 * <p>
 * <b>Nota Architetturale:</b> L'uso di eccezioni specifiche ({@link AbbonamentoScadutoException}, ecc.)
 * trasforma il controllo del flusso in una gestione basata sulle policy, migliorando la 
 * leggibilità e la manutenibilità (Separazione tra "Logica di autenticazione" e "Gestione degli errori").
 * </p>
 * * @author Vilucchi e Padula
 * @version 2.1
 */
public class LoginController {
    
    private LoginView view;
    private UtenteDAO dao;
    private Utente utenteLoggato; 

    public LoginController(LoginView view, UtenteDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    /**
     * Esegue il login coordinando il rinnovo automatico e la validazione dei privilegi.
     */
    public void effettuaLogin(String email, String password) {
        
        if (email.isEmpty() || password.isEmpty()) {
            view.mostraErrore("Inserisci sia email che password.");
            return;
        }

        try {
            /* 1. Logica di dominio proattiva: Tenta un rinnovo silenzioso prima dell'autenticazione */
            UtenteDAOMySQL mDao = new UtenteDAOMySQL();
            boolean sbloccatoInAutomatico = mDao.tentaRinnovoSilenzioso(email);

            /* 2. Verifica delle credenziali nel layer di persistenza */
            Utente utente = dao.effettuaLogin(email, password);

            if (utente == null) {
                throw new CredenzialiErrateException("Email o password errate.");    
            }
            
            /* 3. Validazione delle policy di accesso (stato account e abbonamento) */
            if (!utente.puoAccedereAlSistema()) {
                if (!utente.isAccountAbilitato()) {
                    throw new AccountInattivoException("Accesso negato: account sospeso.");
                } else {
                    /* Se il cliente è bloccato e non è stato sbloccato dal rinnovo silenzioso */
                    if (utente instanceof Cliente && !sbloccatoInAutomatico) {
                        throw new AbbonamentoScadutoException("Accesso negato: il tuo abbonamento è scaduto.");
                    }
                }
            }
            
            this.utenteLoggato = utente;

            /* 4. Transizione di stato della UI */
            view.dispose();

            /* 5. Trigger Double Dispatch per l'instradamento polimorfico */
            utenteLoggato.accediAreaRiservata(this); 
            
        } catch (AbbonamentoScadutoException e) {
            /* Gestione del flusso di rinnovo: intercettazione dello stato di dominio "Scaduto" */
            this.utenteLoggato = null;
            view.mostraPopupRinnovo(email, e.getMessage());

        } catch (CredenzialiErrateException | AccountInattivoException e) {
            /* Gestione unificata degli errori di sicurezza */
            this.utenteLoggato = null;
            view.mostraErrore(e.getMessage());
        }
    }

    public Utente getUtenteLoggato() {
        return this.utenteLoggato;
    }

    /* --- Metodi di Dispatch (Routing) --- */

    public void apriDashboardCliente(Cliente clienteLoggato) {
        DashboardClienteView dashboardView = new DashboardClienteView();
        dashboardView.impostaDatiCliente(clienteLoggato);
        
        try {
            GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni();
            dashboardView.mostraCorsiPrenotati(gestorePrenotazioni.getCorsiPrenotatiDalCliente(clienteLoggato));
        } catch (Exception e) {
            System.err.println("Errore nel recupero corsi: " + e.getMessage());
        }
        
        dashboardView.setVisible(true);
        
        dashboardView.addAreaPremiumListener(e -> {
            dashboardView.dispose();
            StoricoAllenamentiView premiumView = new StoricoAllenamentiView();
            StoricoAllenamentiController clienteController = new StoricoAllenamentiController(premiumView, new SessioneDAOMySQL());
            
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
        new DashboardDirettoreView().setVisible(true);
    }

    public void apriDashboardTrainer(PersonalTrainer trainerLoggato) {
        new PalinsestoCorsiView().setVisible(true);
    }
}