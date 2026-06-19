package it.unipv.posfw.controller;

import java.util.List;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.DatiFormPojo; 
import it.unipv.posfw.domain.SessioneAllenamento;
import it.unipv.posfw.domain.Corso; 
import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.database.SessioneDAOMySQL;
import it.unipv.posfw.view.DashboardClienteView;
import it.unipv.posfw.view.StoricoAllenamentiView;
import it.unipv.posfw.view.PalinsestoCorsiView;

import it.unipv.posfw.exceptions.SchedaVuotaException;
import it.unipv.posfw.exceptions.DatiAllenamentoNonValidiException;
import it.unipv.posfw.exceptions.SalvataggioFallitoException;
import it.unipv.posfw.exceptions.UtenteNonPremiumException;

/**
 * Controller principale per la gestione dello storico degli allenamenti (Area Premium).
 * Collega la vista {@link StoricoAllenamentiView} con il database tramite {@link SessioneDAO}.
 * Si occupa di validare i dati di input, gestire i permessi utente e il routing tra le schermate.
 */
public class StoricoAllenamentiController {
    private StoricoAllenamentiView view;
    private SessioneDAO dao;
    private Cliente clienteLoggato; 

    /**
     * Costruttore del Controller. Inizializza la vista, il DAO e configura i listener.
     *
     * @param view La vista associata a questo controller.
     * @param dao  L'oggetto DAO per l'accesso ai dati delle sessioni di allenamento.
     */
    public StoricoAllenamentiController(StoricoAllenamentiView view, SessioneDAO dao) {
        this.view = view;
        this.dao = dao;
        this.view.setController(this);
        
        inizializzaBottoniNavigazione();
    }

    /**
     * Configura i listener per i pulsanti di navigazione dell'interfaccia.
     * Gestisce il ritorno alla Dashboard principale e l'apertura della pagina di pagamento
     * per l'upgrade all'area Premium.
     */
    private void inizializzaBottoniNavigazione() {
        
        view.btnIndietro.addActionListener(e -> {
            view.dispose(); 
            
            DashboardClienteView dashboardView = new DashboardClienteView();
            dashboardView.impostaDatiCliente(clienteLoggato);
                       
            try {
                GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni();
                List<Corso> corsiDelCliente = gestorePrenotazioni.getCorsiPrenotatiDalCliente(clienteLoggato);
                dashboardView.mostraCorsiPrenotati(corsiDelCliente);
            } catch (Exception ex) {
                System.err.println("Errore nel recupero dei corsi: " + ex.getMessage());
            }
            
            dashboardView.btnAreaPremium.addActionListener(ev -> {
                dashboardView.dispose();
                StoricoAllenamentiView premiumView = new StoricoAllenamentiView();
                SessioneDAO sessioneDAO = new SessioneDAOMySQL();
                StoricoAllenamentiController clienteController = new StoricoAllenamentiController(premiumView, sessioneDAO);
                
                premiumView.setController(clienteController);
                premiumView.setUtenteCorrente(clienteLoggato); 
                premiumView.setVisible(true);
                premiumView.clickAccediStorico(clienteLoggato);
            });
            
            dashboardView.btnPrenotaCorsi.addActionListener(ev -> {
                dashboardView.dispose();
                PalinsestoCorsiView corsiView = new PalinsestoCorsiView(); 
                corsiView.setClienteLoggato(dashboardView.getUtenteCorrente());
                corsiView.setVisible(true);
            });
            
            dashboardView.setVisible(true);
        });

        for(java.awt.event.ActionListener al : view.btnSimulaAccesso.getActionListeners()) {
            view.btnSimulaAccesso.removeActionListener(al);
        }
        
        view.btnSimulaAccesso.addActionListener(e -> {
            view.dispose(); 
            System.out.println("Apro la pagina del Pagamento per l'utente: " + clienteLoggato.getNome());
            
            // ---> INSERISCI QUI IL CODICE PER APRIRE IL PAGAMENTO <---
        });
    }

    /**
     * Verifica se il cliente ha i permessi per accedere all'area Premium.
     * Se l'utente è Premium, sblocca l'interfaccia e carica lo storico dal database.
     *
     * @param cliente L'utente correntemente loggato nel sistema.
     * @throws UtenteNonPremiumException Se il cliente ha un abbonamento BASE e non è autorizzato.
     */
    public void gestisciAccessoSezione(Cliente cliente) throws UtenteNonPremiumException {
        this.clienteLoggato = cliente; 
        
        if (!cliente.isPremium()) {
            throw new UtenteNonPremiumException();
        } else {
            view.mostraModuloInserimento();
            caricaStorico(cliente); 
        }
    }

    /**
     * Valida e salva una nuova sessione di allenamento nel database.
     * Effettua controlli sulla presenza degli esercizi e sulla validità dei parametri numerici (carichi, ripetizioni).
     *
     * @param data     La data in cui è stata svolta la sessione di allenamento.
     * @param esercizi La lista degli esercizi svolti, incapsulati in oggetti {@link DatiFormPojo}.
     * @param cliente  Il cliente a cui associare la sessione di allenamento.
     * @throws SchedaVuotaException Se la lista degli esercizi è nulla o vuota.
     * @throws DatiAllenamentoNonValidiException Se vengono rilevati carichi negativi o ripetizioni errate.
     * @throws SalvataggioFallitoException Se il database respinge l'inserimento o si verifica un errore SQL.
     */
    public void salvaSessioneCompleta(java.util.Date data, List<DatiFormPojo> esercizi, Cliente cliente) 
           throws SchedaVuotaException, DatiAllenamentoNonValidiException, SalvataggioFallitoException {
        
        if (esercizi == null || esercizi.isEmpty()) {
            throw new SchedaVuotaException();
        }

        for (DatiFormPojo esercizio : esercizi) {
            if (esercizio.getCarichi() < 0) {
                throw new DatiAllenamentoNonValidiException("Errore in '" + esercizio.getNomeEsercizio() + "': Il carico non può essere negativo.");
            }
            if (esercizio.getRipetizioni() <= 0) {
                throw new DatiAllenamentoNonValidiException("Errore in '" + esercizio.getNomeEsercizio() + "': Le ripetizioni devono essere maggiori di zero.");
            }
        }

        SessioneAllenamento nuovaSessione = new SessioneAllenamento(data, cliente.getIdCliente());
        for (DatiFormPojo esercizio : esercizi) {
            nuovaSessione.aggiungiEsercizio(esercizio);
        }

        boolean isSalvato = dao.salvaSessione(nuovaSessione);

        if (!isSalvato) {
            throw new SalvataggioFallitoException("Il database ha respinto il salvataggio.");
        }

        caricaStorico(cliente); 
    }

    /**
     * Elimina una specifica sessione di allenamento dal database.
     * Aggiorna automaticamente l'interfaccia ricaricando lo storico aggiornato.
     *
     * @param sessione La sessione di allenamento da eliminare.
     * @param cliente  Il cliente proprietario della sessione.
     * @throws SalvataggioFallitoException Se l'eliminazione fallisce sul database.
     */
    public void eliminaSessioneSelezionata(SessioneAllenamento sessione, Cliente cliente) throws SalvataggioFallitoException {
        boolean rimosso = dao.eliminaSessioneSpecifica(sessione);
        
        if (rimosso) {
            caricaStorico(cliente); 
        } else {
            throw new SalvataggioFallitoException("Impossibile eliminare l'allenamento. Riprova più tardi.");
        }
    }

    /**
     * Recupera lo storico degli allenamenti dal database per uno specifico cliente
     * e ordina alla vista di mostrarlo a schermo.
     *
     * @param cliente Il cliente di cui recuperare lo storico.
     */
    private void caricaStorico(Cliente cliente) {
        List<SessioneAllenamento> storico = dao.getStorico(cliente.getIdCliente());
        view.mostraStorico(storico);
    }
}