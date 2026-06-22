package it.unipv.poingsfw.controller;

import java.util.List;

import it.unipv.poingsfw.dao.SessioneDAO;
import it.unipv.poingsfw.database.SessioneDAOMySQL;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.domain.DatiFormPojo;
import it.unipv.poingsfw.domain.SessioneAllenamento;
import it.unipv.poingsfw.exceptions.DatiAllenamentoNonValidiException;
import it.unipv.poingsfw.exceptions.SalvataggioFallitoException;
import it.unipv.poingsfw.exceptions.SchedaVuotaException;
import it.unipv.poingsfw.exceptions.UtenteNonPremiumException;
import it.unipv.poingsfw.view.DashboardClienteView;
import it.unipv.poingsfw.view.PalinsestoCorsiView;
import it.unipv.poingsfw.view.StoricoAllenamentiView;

/**
 * <p>
 * Agisce come intermediario direzionale tra la logica di presentazione ({@link StoricoAllenamentiView}) e 
 * il livello di accesso ai dati ({@link SessioneDAO}). Assorbe la logica di business e il coordinamento 
 * dell'applicazione, intercettando gli eventi dell'interfaccia utente (tramite il Modello di delega degli eventi)
 * per tradurli in operazioni sul Modello di dominio e successive transizioni di stato della Vista.
 * </p>
 * <p>
 * <b>Tracciabilità Architetturale e Requisiti:</b><br>
 * Risolve la logica applicativa dello <b>Use Case UC4: Registrazione e Monitoraggio Prestazioni</b>.
 * Gestisce l'autorizzazione di accesso condizionata dal dominio (distinzione tra Cliente Premium e Utente Base),
 * valida i parametri quantitativi in input (precondizioni dell'UC4) e coordina il consolidamento del tracciato 
 * storico richiamando il livello di persistenza.
 * </p>
 * * @author Vilucchi Prina
 * @version 1.2
 * @see it.unipv.poingsfw.view.StoricoAllenamentiView
 * @see it.unipv.poingsfw.dao.SessioneDAO
 */
public class StoricoAllenamentiController {
    
    private StoricoAllenamentiView view;
    private SessioneDAO dao;
    private Cliente clienteLoggato; 

    /**
     * Costruttore della classe {@code StoricoAllenamentiController}.
     * <p>
     * Esegue la <i>Dependency Injection</i> dei componenti architetturali (Vista e DAO). 
     * Stabilisce il binding bidirezionale iniziale associando questo controller alla vista delegata,
     * e procede alla registrazione dei listener (Observer) per l'interazione utente.
     * </p>
     */
    public StoricoAllenamentiController(StoricoAllenamentiView view, SessioneDAO dao) {
        this.view = view;
        this.dao = dao;
        this.view.setController(this);
        
        inizializzaBottoniNavigazione();
    }

    /**
     * Coordina la registrazione dei listener (<i>Event Delegation Model</i>) sui componenti della Vista.
     * <p>
     * <b>Risoluzione Information Hiding:</b> Conformemente al principio di basso accoppiamento, il Controller 
     * non manipola più direttamente i componenti grafici atomici (es. i {@code JButton} che  sono privati), 
     * ma invoca i metodi pubblici di servizio esposti dalla Vista per iniettare i propri {@link java.awt.event.ActionListener}.
     * Questo incapsula la struttura dell'interfaccia e separa la "reazione" (Controller) dall'"azione" (View).
     * </p>
     */
    private void inizializzaBottoniNavigazione() {
        
        /* * Registrazione Listener per il routing all'indietro (Ritorno alla Dashboard principale).
         * Coordina la chiusura del frame corrente e l'istanziazione/popolamento della Vista precedente.
         */
        view.addIndietroListener(e -> {
            view.dispose(); 
            
            DashboardClienteView dashboardView = new DashboardClienteView();
            dashboardView.impostaDatiCliente(clienteLoggato);
                        
            try {
                // Recupero dipendenze di dominio per il popolamento della Dashboard
                GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni();
                List<Corso> corsiDelCliente = gestorePrenotazioni.getCorsiPrenotatiDalCliente(clienteLoggato);
                dashboardView.mostraCorsiPrenotati(corsiDelCliente);
            } catch (Exception ex) {
                System.err.println("Errore architetturale nel recupero dei corsi: " + ex.getMessage());
            }
            
            /* *  Information Hiding.
             * I bottoni della DashboardClienteView sono stati resi privati. 
             * L'inserimento dei listener avviene tramite metodi setter dedicati esposti dalla Vista.
             */
            dashboardView.addAreaPremiumListener(ev -> {
                dashboardView.dispose();
                StoricoAllenamentiView premiumView = new StoricoAllenamentiView();
                SessioneDAO sessioneDAO = new SessioneDAOMySQL();
                StoricoAllenamentiController clienteController = new StoricoAllenamentiController(premiumView, sessioneDAO);
                
                premiumView.setController(clienteController);
                premiumView.setUtenteCorrente(clienteLoggato); 
                premiumView.setVisible(true);
                premiumView.clickAccediStorico(clienteLoggato);
            });
            
            dashboardView.addPrenotaCorsiListener(ev -> {
                dashboardView.dispose();
                PalinsestoCorsiView corsiView = new PalinsestoCorsiView(); 
                corsiView.setClienteLoggato(dashboardView.getUtenteCorrente());
                corsiView.setVisible(true);
            });
            
            dashboardView.setVisible(true);
        });

        /* * Registrazione Listener per il Flusso Alternativo (Upgrade Premium).
         * Risponde allo Use Case UC4 - Alternative Flow 1: Intercetta l'azione di simulazione 
         * dell'upgrade qualora il cliente Base tenti l'accesso a funzioni avanzate.
         */
        view.addSimulaAccessoListener(e -> {
            view.dispose(); 
            System.out.println("Routing verso il Modulo Pagamenti per il Cliente: " + clienteLoggato.getNome());
            
            /*
             * L'integrazione con il modulo di Billing (Payment Gateway) o la logica di 
             * mutazione del livello di abbonamento nel dominio avverrà in questo blocco,
             */
        });
    }

    /**
     * Applica le policy di dominio per la validazione dei privilegi di accesso.
     * <p>
     * Se l'entità {@link Cliente} non soddisfa le precondizioni previste dallo Use Case UC4 
     * (livello abbonamento = Premium), solleva un'eccezione di dominio delegando alla Vista 
     * l'onere di renderizzare l'interfaccia restrittiva. In caso di successo, procede al 
     * caricamento e alla visualizzazione del Modello aggiornato (Storico).
     * </p>
     * * @param cliente Il Client in sessione che richiede l'accesso al modulo.
     * @throws UtenteNonPremiumException Eccezione di business sollevata nel Flusso Alternativo 1 (Utente Base).
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
     * Gestisce l'elaborazione del <i>Main Success Scenario</i> (UC4) validando e persistendo la sessione.
     * <p>
     * Isola e protegge l'integrità del dominio (Information Expert). Effettua un'analisi formale dei 
     * <i>Data Transfer Object</i> ({@link DatiFormPojo}), assicurando il rispetto dei vincoli quantitativi 
     * prima di istanziare e mappare le entità di dominio ({@link SessioneAllenamento}). Infine, affida 
     * l'esecuzione della transazione al livello DAO.
     * </p>
     * * @param data      La coordinata temporale in cui si contestualizza la sessione.
     * @param esercizi  La collezione in memoria temporanea degli esercizi inseriti.
     * @param cliente   L'attore primario proprietario dell'aggregato.
     * @throws SchedaVuotaException Se la collezione degli esercizi risulta priva di elementi.
     * @throws DatiAllenamentoNonValidiException In presenza di violazioni dei vincoli di dominio (carichi < 0, rep <= 0).
     * @throws SalvataggioFallitoException Se il database non conferma l'operazione di commit.
     */
    public void salvaSessioneCompleta(java.util.Date data, List<DatiFormPojo> esercizi, Cliente cliente) 
           throws SchedaVuotaException, DatiAllenamentoNonValidiException, SalvataggioFallitoException {
        
        if (esercizi == null || esercizi.isEmpty()) {
            throw new SchedaVuotaException();
        }

        /* Validazione semantica dei parametri di dominio */
        for (DatiFormPojo esercizio : esercizi) {
            if (esercizio.getCarichi() < 0) {
                throw new DatiAllenamentoNonValidiException("Errore in '" + esercizio.getNomeEsercizio() + "': Il carico non può essere negativo.");
            }
            if (esercizio.getRipetizioni() <= 0) {
                throw new DatiAllenamentoNonValidiException("Errore in '" + esercizio.getNomeEsercizio() + "': Le ripetizioni devono essere maggiori di zero.");
            }
        }

        /* Mapping strutturale: da Dati di Input a Entità di Dominio Coesa */
        SessioneAllenamento nuovaSessione = new SessioneAllenamento(data, cliente.getIdCliente());
        for (DatiFormPojo esercizio : esercizi) {
            nuovaSessione.aggiungiEsercizio(esercizio);
        }

        /* Delega al layer di Persistenza (DAO) */
        boolean isSalvato = dao.salvaSessione(nuovaSessione);

        if (!isSalvato) {
            throw new SalvataggioFallitoException("Il sistema di persistenza ha respinto l'inserimento.");
        }

        /* Aggiornamento reattivo dello stato della Vista post-transazione */
        caricaStorico(cliente); 
    }

    /**
     * Intercetta la richiesta di rimozione di un'entità di dominio consolidata e la trasmette al DAO.
     * <p>
     * In caso di conferma positiva dalla persistenza, orchestra il rinfresco visivo richiedendo al sistema
     * un nuovo fetch dei dati, in conformità col pattern architetturale (Model Updates View).
     * </p>
     * * @param sessione Il riferimento in memoria della sessione bersaglio per la cancellazione.
     * @param cliente  L'attore proprietario per il re-fetch dei dati.
     * @throws SalvataggioFallitoException Qualora l'esecuzione della query di DELETE fallisca.
     */
    public void eliminaSessioneSelezionata(SessioneAllenamento sessione, Cliente cliente) throws SalvataggioFallitoException {
        boolean rimosso = dao.eliminaSessioneSpecifica(sessione);
        
        if (rimosso) {
            caricaStorico(cliente); 
        } else {
            throw new SalvataggioFallitoException("Anomalia nel livello DAO: impossibile eliminare l'allenamento.");
        }
    }

    /**
     * Esegue l'estrazione delle entità di dominio dal Database e notifica la View per il rendering.
     * <p>
     * Implementazione concreta della Postcondizione dello Use Case UC4: il sistema aggrega i dati persistiti 
     * per generare o aggiornare la vista riepilogativa dello storico attività.
     * </p>
     * * @param cliente Il Client in esame utilizzato come parametro di ricerca per la query.
     */
    private void caricaStorico(Cliente cliente) {
        List<SessioneAllenamento> storico = dao.getStorico(cliente.getIdCliente());
        view.mostraStorico(storico);
    }
}