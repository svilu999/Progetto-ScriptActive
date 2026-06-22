package it.unipv.posfw.test.unit;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// Assicurati che i package corrispondano ai tuoi
import it.unipv.posfw.controller.GestoreRegistrazione;
import it.unipv.posfw.domain.LivelloAbbonamento;
import it.unipv.posfw.domain.Sede;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.exceptions.PagamentoFallitoException;

public class RegistrazioneTest {

    private GestoreRegistrazione gestoreRegistrazione;
    private Sede sedeDiTest;

    @Before
    public void setUp() {
    	// Usa il Singleton per ottenere l'istanza del gestore
        gestoreRegistrazione = GestoreRegistrazione.getIstanza();
        
        // Creiamo la sede di test usando il costruttore corretto: (int idSede, String nomeSede)
        sedeDiTest = new Sede(1, "Sede Centrale Milano");
    }

    // TEST 1: Main Success Scenario (Happy Path)
    @Test
    public void testRegistrazioneConSuccesso() {
        // Arrange: Dati validi. L'IBAN NON contiene la parola "ERRORE"
        String nome = "Mario";
        String cognome = "Rozzi";
        String email = "mario.rozzi@email.it";
        String password = "PasswordSicura123!";
        // Attenzione: cambia il Codice Fiscale ogni volta che fai girare il test, 
        // o metti un codice che sai non essere nel database
        String cf = "NUOVCF99Z01H501Z"; 
        String ibanValido = "IT99Z0123456789012345678901";

        try {
            // Act: Chiamiamo il metodo
            gestoreRegistrazione.registraNuovoCliente(nome, cognome, email, password, cf, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, ibanValido);
            
            // Assert: Se arriviamo a questa riga senza che sia stata lanciata un'eccezione, il test è passato!
            assertTrue("La registrazione è avvenuta con successo senza lanciare eccezioni", true);
            
        } catch (Exception e) {
            // Se viene lanciata un'eccezione inaspettata, il test fallisce
            fail("La registrazione ha fallito inaspettatamente lanciando l'eccezione: " + e.getMessage());
        }
    }

    // TEST 2: Flusso Alternativo 1 (Errore Pagamento)
    @Test
    public void testRegistrazioneFallitaPerPagamentoRespinto() {
        // Arrange: Dati validi, ma l'IBAN contiene la parola "ERRORE"
        String nome = "Luigi";
        String cognome = "Bianchi";
        String email = "luigi.bianchi@email.it";
        String password = "PasswordSicura123!";
        String cf = "BNCLGU90B02H501Z";
        String ibanInvalido = "IT99Z01234ERRORE5678901"; // INNESCA L'ERRORE NEL GESTORE PAGAMENTI

        // Act & Assert
        // Diciamo a JUnit: "Guarda che mi aspetto che questo pezzo di codice lanci una PagamentoFallitoException"
        assertThrows(PagamentoFallitoException.class, () -> {
            gestoreRegistrazione.registraNuovoCliente(nome, cognome, email, password, cf, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, ibanInvalido);
        });
        
        // Il test passa SOLO se viene lanciata l'eccezione PagamentoFallitoException.
        // Ciò dimostra che il flusso alternativo funziona e blocca l'inserimento nel database!
    }
}