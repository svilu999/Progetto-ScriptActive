package it.unipv.poingsfw.controller;

import it.unipv.poingsfw.database.ClienteDAOMySQL;
import it.unipv.poingsfw.domain.Abbonamento;
import it.unipv.poingsfw.domain.LivelloAbbonamento;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * La classe {@code GestoreRinnovi} funge da <b>Motore di Background</b> per la 
 * gestione automatizzata delle scadenze degli abbonamenti.
 * <p>
 * Sviluppata all'interno del livello <b>Controller</b>, questa classe si occupa di 
 * orchestrare le operazioni periodiche. Utilizza le API di concorrenza di Java 
 * per istanziare un thread che, a intervalli regolari, interroga la base dati, 
 * simula i processi di addebito e aggiorna lo stato delle sottoscrizioni.
 * </p>
 * <p>
 * <b>Scelte Architetturali:</b><br>
 * Utilizza il pattern <b>Singleton</b> per garantire l'esistenza di un unico scheduler 
 * all'interno dell'applicazione. Questo previene l'esecuzione multipla e concorrente 
 * degli stessi rinnovi, evitando inconsistenze nel database e doppi addebiti.
 * L'uso di {@code ScheduledExecutorService} garantisce una gestione robusta del thread 
 * in background rispetto a timer tradizionali.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class GestoreRinnovi {

    private static GestoreRinnovi istanza;
    private ClienteDAOMySQL clienteDAO;
    private ScheduledExecutorService scheduler;

    /**
     * Costruttore privato della classe, necessario per applicare il pattern Singleton.
     * Inizializza il DAO per l'accesso ai dati e configura un pool di thread con un 
     * singolo lavoratore dedicato (Single Thread Executor) per le operazioni schedulate.
     */
    
    private GestoreRinnovi() {
        this.clienteDAO = new ClienteDAOMySQL();
        // Creiamo un "pool" con 1 solo thread lavoratore dedicato a questo compito
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Punto di accesso globale all'istanza unica della classe (Pattern Singleton).
     * * @return L'unica istanza attiva di {@code GestoreRinnovi}.
     */
    
    public static GestoreRinnovi getIstanza() {
        if (istanza == null) {
            istanza = new GestoreRinnovi();
        }
        return istanza;
    }

    /**
     * Innesca il ciclo di vita del thread in background per i rinnovi automatici.
     * <p>
     * <b>Dinamica di Schedulazione:</b><br>
     * Il metodo istruisce lo {@code ScheduledExecutorService} ad eseguire un task
     * immediatamente all'avvio (delay iniziale pari a 0) e, successivamente, a 
     * reiterare l'operazione con una frequenza di 24 ore (1 giorno).
     * Deve essere invocato una singola volta durante il bootstrap dell'applicazione.
     * </p>
     */
    
    public void avviaMotoreRinnovi() {
        System.out.println("[SISTEMA] Avvio thread di controllo rinnovi automatici...");

        // Definiamo il lavoro che il thread dovrà fare
        Runnable taskRinnovi = () -> {
            System.out.println("[THREAD RINNOVI] Inizio controllo scadenze...");
            elaboraRinnovi();
            System.out.println("[THREAD RINNOVI] Controllo terminato.");
        };

        // Esegue il task immediatamente (0 delay), poi lo ripete ogni 24 ore (1, DAYS)
        scheduler.scheduleAtFixedRate(taskRinnovi, 0, 1, TimeUnit.DAYS);
    }

    /**
     * La logica core: pesca dal DB, simula il pagamento, aggiorna o blocca.
     */
    private void elaboraRinnovi() {
        // 1. Chiediamo al DAO chi è scaduto e ha il rinnovo attivo
        List<Abbonamento> daRinnovare = clienteDAO.getAbbonamentiInScadenzaConRinnovo();

        for (Abbonamento abb : daRinnovare) {
            
            // 2. Simuliamo il pagamento (Fake in puro Java, come hai fatto nei tuoi test)
            // Se l'IBAN è null o contiene la stringa "ERRORE", simuliamo un fallimento
            boolean pagamentoOk = (abb.getIban() != null && !abb.getIban().contains("ERRORE"));

            if (pagamentoOk) {
                // 3a. Pagamento Passato: Calcoliamo la nuova data e la salviamo
                Date nuovaScadenza = calcolaNuovaScadenza(abb.getDataScadenza(), abb.getLivello());
                clienteDAO.aggiornaScadenzaAbbonamento(abb.getIdAbbonamento(), nuovaScadenza);
                System.out.println("[SUCCESS] Abbonamento " + abb.getIdAbbonamento() + " rinnovato fino al " + nuovaScadenza);
            } else {
                // 3b. Pagamento Fallito: Togliamo la spunta "Rinnovo Automatico"
                clienteDAO.disattivaRinnovoAutomatico(abb.getIdAbbonamento());
                System.err.println("[ERRORE] Rinnovo fallito per l'abbonamento " + abb.getIdAbbonamento() + ". Rinnovo disattivato.");
            }
        }
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

        if (livello == LivelloAbbonamento.MENSILE) {
            cal.add(Calendar.MONTH, 1);
        } else if (livello == LivelloAbbonamento.SEMESTRALE) {
            cal.add(Calendar.MONTH, 6);
        } else if (livello == LivelloAbbonamento.ANNUALE) {
            cal.add(Calendar.YEAR, 1);
        }
        
        return cal.getTime();
    }
}