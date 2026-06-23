package it.unipv.poingsfw.test.unit;

import org.junit.Before;
import org.junit.Test;

import it.unipv.poingsfw.controller.SistemaPagamento;

import static org.junit.Assert.*;

/**
 * La classe {@code SistemaPagamentoTest} implementa i <b>Test di Unità</b> per 
 * validare il comportamento del Object bancario ({@link SistemaPagamento}).
 * <p>
 * L'obiettivo non è testare una reale transazione di rete, ma 
 * verificare che la logica di <b>Stubbing</b> interna risponda correttamente agli 
 * stimoli di input. Si applica la tecnica di testing della <b>Partizione di Equivalenza</b> 
 * per garantire che le classi dipendenti dal pagamento ricevano i giusti segnali 
 * booleani (true/false) nei flussi nominali e di errore.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class SistemaPagamentoTest {

    private SistemaPagamento sistemaPagamento;

    // SETUP: Inizializzazione del sistema per il test
    @Before
    public void setUp() {
        sistemaPagamento = new SistemaPagamento();
    }

    /**
     * Verifica il Main Success Scenario.
     * <p>
     * <b>Partizione di Equivalenza Valida:</b> Testa l'inserimento di un IBAN 
     * formalmente corretto, aspettandosi che il simulatore autorizzi la transazione.
     * </p>
     */
    
    @Test
    public void testAutorizzaTransazioneValida() {
        boolean esito = sistemaPagamento.autorizzaTransazione("IT99Z0123456789012345678901", 50.0);
        assertTrue("La transazione dovrebbe essere autorizzata", esito);
    }

    /**
     * Verifica il Flusso Alternativo (Errore).
     * <p>
     * <b>Partizione di Equivalenza Non Valida:</b> Inserisce un IBAN contenente 
     * la stringa trigger "ERRORE" per innescare artificialmente il blocco del pagamento.
     * Si aspetta che il simulatore rifiuti la transazione.
     * </p>
     */
    
    @Test
    public void testAutorizzaTransazioneConErrore() {
        // Inserisco la parola "ERRORE" per innescare il blocco del pagamento
        boolean esito = sistemaPagamento.autorizzaTransazione("IT99Z01234ERRORE5678901", 50.0);
        assertFalse("La transazione dovrebbe essere rifiutata", esito);
    }
} 
