package it.unipv.poingsfw.controller;

import java.util.Objects;

import it.unipv.poingsfw.controller.vista.VistaLogin;
import it.unipv.poingsfw.dao.UtenteDAO;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Direttore;
import it.unipv.poingsfw.domain.InstradatoreUtente;
import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.domain.Utente;
import it.unipv.poingsfw.exceptions.AbbonamentoScadutoException;
import it.unipv.poingsfw.exceptions.AccountInattivoException;
import it.unipv.poingsfw.exceptions.CredenzialiErrateException;

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
 * <p>
 * <b>Dipendenze:</b> il controller riceve dal costruttore tre astrazioni — la vista
 * ({@link VistaLogin}), la persistenza ({@link UtenteDAO}) e la navigazione
 * ({@link Navigatore}) — e non conosce né Swing né MySQL. Dopo l'autenticazione
 * l'instradamento verso l'area riservata avviene per <i>Double Dispatch</i>:
 * il controller implementa {@link InstradatoreUtente} e l'entità {@link Utente}
 * richiama il metodo corrispondente al proprio ruolo.
 * </p>
 * * @author Vilucchi e Padula
 * @version 3.0
 */
public class LoginController implements InstradatoreUtente {
    
    private final VistaLogin view;
    private final UtenteDAO dao;
    private final Navigatore navigatore;
    private Utente utenteLoggato; 

    public LoginController(VistaLogin view, UtenteDAO dao, Navigatore navigatore) {
        this.view = Objects.requireNonNull(view, "La vista non può essere null.");
        this.dao = Objects.requireNonNull(dao, "Il DAO non può essere null.");
        this.navigatore = Objects.requireNonNull(navigatore, "Il navigatore non può essere null.");
    }

    /**
     * Esegue il login coordinando il rinnovo automatico e la validazione dei privilegi.
     */
    public void effettuaLogin(String email, String password) {
        
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            view.mostraErrore("Inserisci sia email che password.");
            return;
        }

        try {
            /* 1. Logica di dominio proattiva: Tenta un rinnovo silenzioso prima dell'autenticazione */
            boolean sbloccatoInAutomatico = dao.tentaRinnovoSilenzioso(email);

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
            view.chiudi();

            /* 5. Trigger Double Dispatch per l'instradamento polimorfico */
            utenteLoggato.accediAreaRiservata(this); 
            
        } catch (AbbonamentoScadutoException e) {
            /* Gestione del flusso di rinnovo: intercettazione dello stato di dominio "Scaduto" */
            this.utenteLoggato = null;
            if (view.chiediConfermaRinnovo(e.getMessage())) {
                view.chiudi();
                navigatore.apriRinnovo(email);
            }

        } catch (CredenzialiErrateException | AccountInattivoException e) {
            /* Gestione unificata degli errori di sicurezza */
            this.utenteLoggato = null;
            view.mostraErrore(e.getMessage());
        }
    }

    /**
     * Gestisce la richiesta di un visitatore non ancora iscritto: lascia il login e apre
     * il modulo di registrazione (UC1).
     */
    public void richiediRegistrazione() {
        view.chiudi();
        navigatore.apriRegistrazione();
    }

    public Utente getUtenteLoggato() {
        return this.utenteLoggato;
    }

    /* --- Metodi di Dispatch (Routing): il "dove" è deciso qui, il "come" dal Navigatore --- */

    @Override
    public void instradaCliente(Cliente clienteLoggato) {
        navigatore.apriDashboardCliente(clienteLoggato);
    }

    @Override
    public void instradaDirettore(Direttore direttoreLoggato) {
        navigatore.apriDashboardDirettore(direttoreLoggato);
    }

    @Override
    public void instradaTrainer(PersonalTrainer trainerLoggato) {
        navigatore.apriAreaTrainer();
    }
}
