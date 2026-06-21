package it.unipv.posfw.test.unit;

import org.junit.Before;
import org.junit.Test;

import it.unipv.posfw.controller.SistemaPagamento;

import static org.junit.Assert.*;

public class SistemaPagamentoTest {

    private SistemaPagamento sistemaPagamento;

    // SETUP: Inizializzazione del sistema per il test
    @Before
    public void setUp() {
        sistemaPagamento = new SistemaPagamento();
    }

    // ACTION & ASSERTION: Partizione Equivalenza Valida (Main Scenario)
    @Test
    public void testAutorizzaTransazioneValida() {
        boolean esito = sistemaPagamento.autorizzaTransazione("IT99Z0123456789012345678901", 50.0);
        assertTrue("La transazione dovrebbe essere autorizzata", esito);
    }

    // ACTION & ASSERTION: Partizione Non Correttezza (Alternative Flow 1)
    @Test
    public void testAutorizzaTransazioneConErrore() {
        // Inserisco la parola "ERRORE" per innescare il blocco del pagamento
        boolean esito = sistemaPagamento.autorizzaTransazione("IT99Z01234ERRORE5678901", 50.0);
        assertFalse("La transazione dovrebbe essere rifiutata", esito);
    }
} 
