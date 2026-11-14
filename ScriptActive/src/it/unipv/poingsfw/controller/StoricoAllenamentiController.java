package it.unipv.poingsfw.controller;

import java.util.List;
import java.util.Objects;

import it.unipv.poingsfw.controller.vista.VistaStoricoAllenamenti;
import it.unipv.poingsfw.dao.SessioneDAO;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.DatiFormPojo;
import it.unipv.poingsfw.domain.SessioneAllenamento;
import it.unipv.poingsfw.exceptions.DatiAllenamentoNonValidiException;
import it.unipv.poingsfw.exceptions.SalvataggioFallitoException;
import it.unipv.poingsfw.exceptions.SchedaVuotaException;
import it.unipv.poingsfw.exceptions.UtenteNonPremiumException;

/**
 * <p>
 * Agisce come intermediario direzionale tra la logica di presentazione ({@link VistaStoricoAllenamenti}) e 
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
 * <p>
 * <b>Dipendenze:</b> il controller conosce solo astrazioni — la vista
 * ({@link VistaStoricoAllenamenti}), la persistenza ({@link SessioneDAO}) e la navigazione
 * ({@link Navigatore}) — ricevute dal costruttore. Non istanzia finestre né DAO.
 * </p>
 * * @author Vilucchi Prina
 * @version 2.0
 * @see it.unipv.poingsfw.controller.vista.VistaStoricoAllenamenti
 * @see it.unipv.poingsfw.dao.SessioneDAO
 */
public class StoricoAllenamentiController {
    
    private final VistaStoricoAllenamenti view;
    private final SessioneDAO dao;
    private final Navigatore navigatore;
    private Cliente clienteLoggato; 

    /**
     * Costruttore della classe {@code StoricoAllenamentiController}.
     * <p>
     * Esegue la <i>Dependency Injection</i> dei componenti architetturali (Vista, DAO e Navigatore).
     * Il collegamento inverso (la vista che conosce il proprio controller) è stabilito dal
     * Composition Root: la vista traduce i gesti dell'utente in chiamate ai metodi pubblici
     * di questa classe.
     * </p>
     */
    public StoricoAllenamentiController(VistaStoricoAllenamenti view, SessioneDAO dao, Navigatore navigatore) {
        this.view = Objects.requireNonNull(view, "La vista non può essere null.");
        this.dao = Objects.requireNonNull(dao, "Il DAO non può essere null.");
        this.navigatore = Objects.requireNonNull(navigatore, "Il navigatore non può essere null.");
    }

    /**
     * Routing all'indietro: chiude l'area allenamenti e torna alla dashboard del cliente.
     * <p>
     * Il controller decide la destinazione; la costruzione della schermata è compito del
     * {@link Navigatore}.
     * </p>
     */
    public void tornaAllaDashboard() {
        view.chiudi();
        if (clienteLoggato != null) {
            navigatore.apriDashboardCliente(clienteLoggato);
        } else {
            navigatore.apriLogin();
        }
    }

    /**
     * Flusso Alternativo 1 dello Use Case UC4 (Upgrade Premium).
     * <p>
     * L'integrazione con il modulo di pagamento per il cambio di abbonamento non è ancora
     * disponibile: il sistema lo comunica all'utente invece di chiudere la schermata.
     * </p>
     */
    public void richiediUpgradePremium() {
        view.mostraAvviso("L'upgrade a Premium non è ancora disponibile da questa schermata.\n"
                + "Rivolgiti alla reception della tua sede per cambiare abbonamento.");
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