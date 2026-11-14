package it.unipv.poingsfw.service;

import it.unipv.poingsfw.dao.ClienteDAO;
import it.unipv.poingsfw.domain.Abbonamento;
import it.unipv.poingsfw.domain.LivelloAbbonamento;
import it.unipv.poingsfw.domain.Tariffario;
import it.unipv.poingsfw.exceptions.PagamentoFallitoException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * La classe {@code GestoreRinnovi} funge da <b>Motore di Background</b> per la 
 * gestione automatizzata delle scadenze degli abbonamenti.
 * <p>
 * È un servizio applicativo privo di interfaccia utente: utilizza le API di concorrenza
 * di Java per eseguire, a intervalli regolari, il controllo delle sottoscrizioni scadute,
 * l'addebito del rinnovo e l'aggiornamento della data di scadenza.
 * </p>
 * <p>
 * <b>Scelte Architetturali:</b><br>
 * Le dipendenze ({@link ClienteDAO} per la persistenza e {@link ServizioPagamenti} per
 * l'addebito) arrivano dal costruttore. L'unicità dello scheduler è garantita dal fatto
 * che l'istanza viene creata una sola volta nel <i>Composition Root</i>, senza ricorrere
 * a un Singleton: la logica di {@link #elaboraRinnovi()} è così verificabile con oggetti
 * fittizi, senza database e senza thread.
 * </p>
 * * @author Arianna Padula
 * @version 2.0
 */

public class GestoreRinnovi {

    private static final Logger LOG = Logger.getLogger(GestoreRinnovi.class.getName());

    private final ClienteDAO clienteDAO;
    private final ServizioPagamenti servizioPagamenti;
    private ScheduledExecutorService scheduler;

    /**
     * @param clienteDAO        accesso agli abbonamenti da rinnovare
     * @param servizioPagamenti sistema di pagamento su cui addebitare il rinnovo
     */
    public GestoreRinnovi(ClienteDAO clienteDAO, ServizioPagamenti servizioPagamenti) {
        this.clienteDAO = Objects.requireNonNull(clienteDAO, "clienteDAO non può essere null");
        this.servizioPagamenti = Objects.requireNonNull(servizioPagamenti, "servizioPagamenti non può essere null");
    }

    /**
     * Innesca il ciclo di vita del thread in background per i rinnovi automatici.
     * <p>
     * <b>Dinamica di Schedulazione:</b><br>
     * Il metodo istruisce lo {@code ScheduledExecutorService} ad eseguire un task
     * immediatamente all'avvio (delay iniziale pari a 0) e, successivamente, a 
     * reiterare l'operazione con una frequenza di 24 ore (1 giorno).
     * Invocazioni successive alla prima non hanno effetto.
     * </p>
     */
    
    public synchronized void avviaMotoreRinnovi() {
        if (scheduler != null) {
            return;
        }
        LOG.info("Avvio thread di controllo rinnovi automatici.");

        // Un solo thread lavoratore, daemon: non impedisce la chiusura dell'applicazione
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "motore-rinnovi");
            thread.setDaemon(true);
            return thread;
        });

        Runnable taskRinnovi = () -> {
            try {
                elaboraRinnovi();
            } catch (RuntimeException e) {
                // Un'eccezione non gestita annullerebbe tutte le esecuzioni future del task
                LOG.log(Level.SEVERE, "Controllo rinnovi interrotto da un errore.", e);
            }
        };

        // Esegue il task immediatamente (0 delay), poi lo ripete ogni 24 ore (1, DAYS)
        scheduler.scheduleAtFixedRate(taskRinnovi, 0, 1, TimeUnit.DAYS);
    }

    /**
     * Arresta il motore dei rinnovi, se avviato.
     */
    public synchronized void arrestaMotoreRinnovi() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * La logica core: recupera gli abbonamenti scaduti con rinnovo attivo, tenta l'addebito
     * e, in base all'esito, prolunga la scadenza oppure disattiva il rinnovo automatico.
     *
     * @return il numero di abbonamenti rinnovati con successo
     */
    public int elaboraRinnovi() {
        // 1. Chiediamo al DAO chi è scaduto e ha il rinnovo attivo
        List<Abbonamento> daRinnovare = clienteDAO.getAbbonamentiInScadenzaConRinnovo();
        int rinnovati = 0;

        for (Abbonamento abb : daRinnovare) {
            try {
                // 2. Addebito tramite il sistema di pagamento
                if (abb.getIban() == null) {
                    throw new PagamentoFallitoException("IBAN assente.");
                }
                double importo = Tariffario.calcolaImporto(abb.getTipo(), abb.getLivello());
                servizioPagamenti.elaboraPagamento(abb.getIban(), importo);

                // 3a. Pagamento riuscito: calcoliamo la nuova data e la salviamo
                Date nuovaScadenza = calcolaNuovaScadenza(abb.getDataScadenza(), abb.getLivello());
                clienteDAO.aggiornaScadenzaAbbonamento(abb.getIdAbbonamento(), nuovaScadenza);
                rinnovati++;
                LOG.info("Abbonamento " + abb.getIdAbbonamento() + " rinnovato fino al " + nuovaScadenza);
            } catch (PagamentoFallitoException e) {
                // 3b. Pagamento fallito: togliamo la spunta "Rinnovo Automatico"
                clienteDAO.disattivaRinnovoAutomatico(abb.getIdAbbonamento());
                LOG.warning("Rinnovo fallito per l'abbonamento " + abb.getIdAbbonamento() + ": rinnovo automatico disattivato.");
            }
        }
        return rinnovati;
    }

    /**
     * Metodo di utilità per l'aritmetica delle date.
     * <p>
     * Utilizza la classe {@code java.util.Calendar} per calcolare la nuova data 
     * di scadenza in base al livello dell'abbonamento (Mensile, Semestrale, Annuale), 
     * gestendo in automatico le anomalie di calendario (es. anni bisestili, mesi di 28/30 giorni).
     * </p>
     * * @param vecchiaScadenza   La data di scadenza attuale dell'abbonamento.
     * @param livello         L'enum che rappresenta la tipologia di abbonamento.
     * @return L'oggetto {@code Date} rappresentante la nuova data di scadenza calcolata.
     */
    
    private Date calcolaNuovaScadenza(Date vecchiaScadenza, LivelloAbbonamento livello) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(vecchiaScadenza);
        if (livello != null) {
            cal.add(Calendar.MONTH, livello.getMesi());
        }
        return cal.getTime();
    }
}
