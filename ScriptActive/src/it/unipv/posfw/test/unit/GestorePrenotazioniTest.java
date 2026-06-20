package it.unipv.posfw.test.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.exceptions.CorsoAlCompletoException;
import it.unipv.posfw.exceptions.PrenotazioneGiaEffettuataException;
import it.unipv.posfw.controller.GestorePrenotazioni;

import java.time.LocalDateTime;

public class GestorePrenotazioniTest {

    private GestorePrenotazioni gestore;
    private Cliente utenteTest;
    private Corso corsoTest;

    @BeforeEach
    public void setUp() {

        gestore = new GestorePrenotazioni();
        

        utenteTest = new Cliente("TestNome", "TestCognome", "test@test.com", "TSTCF99X99Y999Z");
        utenteTest.setId(4);
        

        corsoTest = new Corso("1", "Corso di Prova", LocalDateTime.now(), 1, null);
    }

    @Test
    public void testPrenotazioneSuccesso() {

        corsoTest.setPostiDisponibili(1);
        

        assertDoesNotThrow(() -> {
            gestore.prenotaCorso(utenteTest, corsoTest);
        }, "La prenotazione dovrebbe andare a buon fine senza lanciare eccezioni.");
    }

    @Test
    public void testListaAttesaEccezione() {

        corsoTest.setPostiDisponibili(0);
        

        Exception eccezione = assertThrows(CorsoAlCompletoException.class, () -> {
            gestore.prenotaCorso(utenteTest, corsoTest);
        });
        

        assertTrue(eccezione.getMessage().contains("LISTA D'ATTESA"));
    }

    @Test
    public void testPrenotazioneDoppiaEccezione() {

        
        assertThrows(PrenotazioneGiaEffettuataException.class, () -> {

            gestore.prenotaCorso(utenteTest, corsoTest);
            

            gestore.prenotaCorso(utenteTest, corsoTest);
        });
    }
}