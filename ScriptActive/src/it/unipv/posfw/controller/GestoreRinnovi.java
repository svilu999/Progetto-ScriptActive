package it.unipv.posfw.controller;

import it.unipv.posfw.database.ClienteDAOMySQL;
import it.unipv.posfw.domain.Abbonamento;
import it.unipv.posfw.domain.LivelloAbbonamento;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GestoreRinnovi {

    private static GestoreRinnovi istanza;
    private ClienteDAOMySQL clienteDAO;
    private ScheduledExecutorService scheduler;

    // Costruttore privato (Pattern Singleton)
    private GestoreRinnovi() {
        this.clienteDAO = new ClienteDAOMySQL();
        // Creiamo un "pool" con 1 solo thread lavoratore dedicato a questo compito
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public static GestoreRinnovi getIstanza() {
        if (istanza == null) {
            istanza = new GestoreRinnovi();
        }
        return istanza;
    }

    /**
     * Avvia il thread in background. 
     * Va chiamato UNA SOLA VOLTA all'avvio dell'applicazione.
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
     * Metodo di utilità per calcolare la nuova data aggiungendo i mesi corretti.
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