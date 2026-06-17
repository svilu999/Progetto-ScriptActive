package it.unipv.posfw.controller;

import java.util.List;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.DatiFormPojo; 
import it.unipv.posfw.domain.SessioneAllenamento;
import it.unipv.posfw.domain.Corso; 
import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.dao.SessioneDAOSQL; 
import it.unipv.posfw.view.DashboardClienteView;
import it.unipv.posfw.view.StoricoAllenamentiView;
import it.unipv.posfw.view.PalinsestoCorsiView;

// IMPORTIAMO LE NOSTRE ECCEZIONI PERSONALIZZATE
import it.unipv.posfw.exceptions.SchedaVuotaException;
import it.unipv.posfw.exceptions.DatiAllenamentoNonValidiException;
import it.unipv.posfw.exceptions.SalvataggioFallitoException;
import it.unipv.posfw.exceptions.UtenteNonPremiumException;

public class StoricoAllenamentiController {
    private StoricoAllenamentiView view;
    private SessioneDAO dao;
    private Cliente clienteLoggato; // Memorizziamo il cliente per passarlo alle altre finestre

    public StoricoAllenamentiController(StoricoAllenamentiView view, SessioneDAO dao) {
        this.view = view;
        this.dao = dao;
        this.view.setController(this);
        
        // Attiviamo l'ascolto dei bottoni non appena il Controller nasce
        inizializzaBottoniNavigazione();
    }

    
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
                SessioneDAO sessioneDAO = new SessioneDAOSQL();
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

        // 2. AZIONE PER IL BOTTONE "SBLOCCA PREMIUM"
        // Siccome nella View c'era già un'azione di default, la "scolleghiamo" e ci mettiamo la nostra!
        for(java.awt.event.ActionListener al : view.btnSimulaAccesso.getActionListeners()) {
            view.btnSimulaAccesso.removeActionListener(al);
        }
        
        view.btnSimulaAccesso.addActionListener(e -> {
            view.dispose(); // Chiudiamo la finestra corrente
            
            System.out.println("Apro la pagina del Pagamento per l'utente: " + clienteLoggato.getNome());
            
            // ---> INSERISCI QUI IL CODICE PER APRIRE IL PAGAMENTO <---
            // Esempio:
            // PagamentoView pagView = new PagamentoView();
            // PagamentoController pagCtrl = new PagamentoController(pagView, clienteLoggato);
            // pagView.setVisible(true);
        });
    }
    // =========================================================================

    // Ora "lancia" (throws) un'eccezione se il cliente non è premium
    public void gestisciAccessoSezione(Cliente cliente) throws UtenteNonPremiumException {
        this.clienteLoggato = cliente; // Salviamo l'utente corrente in memoria
        
        if (!cliente.isPremium()) {
            throw new UtenteNonPremiumException();
        } else {
            view.mostraModuloInserimento();
            caricaStorico(cliente); 
        }
    }

    // Gestisce 3 tipi di errori diversi (Scheda Vuota, Dati Invalidi, Database KO)
    public void salvaSessioneCompleta(java.util.Date data, List<DatiFormPojo> esercizi, Cliente cliente) 
           throws SchedaVuotaException, DatiAllenamentoNonValidiException, SalvataggioFallitoException {
        
        // 1. Controllo: La scheda è vuota?
        if (esercizi == null || esercizi.isEmpty()) {
            throw new SchedaVuotaException();
        }

        // 2. Controllo: I dati degli esercizi sono validi?
        for (DatiFormPojo esercizio : esercizi) {
            if (esercizio.getCarichi() < 0) {
                throw new DatiAllenamentoNonValidiException("Errore in '" + esercizio.getNomeEsercizio() + "': Il carico non può essere negativo.");
            }
            if (esercizio.getRipetizioni() <= 0) {
                throw new DatiAllenamentoNonValidiException("Errore in '" + esercizio.getNomeEsercizio() + "': Le ripetizioni devono essere maggiori di zero.");
            }
        }

        // Se i controlli passano, creo l'oggetto Sessione
        SessioneAllenamento nuovaSessione = new SessioneAllenamento(data, cliente.getIdCliente());
        for (DatiFormPojo esercizio : esercizi) {
            nuovaSessione.aggiungiEsercizio(esercizio);
        }

        // 3. Provo a salvare sul database
        boolean isSalvato = dao.salvaSessione(nuovaSessione);

        if (!isSalvato) {
            // Se il DAO restituisce false (fallimento SQL), lancio l'eccezione
            throw new SalvataggioFallitoException("Il database ha respinto il salvataggio.");
        }

        // Se arriviamo fin qui senza lanciare eccezioni, è andato tutto bene
        caricaStorico(cliente); 
    }

    // Se l'eliminazione fallisce, lancio un'eccezione mascherando l'errore DB
    public void eliminaSessioneSelezionata(SessioneAllenamento sessione, Cliente cliente) throws SalvataggioFallitoException {
        boolean rimosso = dao.eliminaSessioneSpecifica(sessione);
        
        if (rimosso) {
            caricaStorico(cliente); 
        } else {
            throw new SalvataggioFallitoException("Impossibile eliminare l'allenamento. Riprova più tardi.");
        }
    }

    private void caricaStorico(Cliente cliente) {
        List<SessioneAllenamento> storico = dao.getStorico(cliente.getIdCliente());
        view.mostraStorico(storico);
    }
}