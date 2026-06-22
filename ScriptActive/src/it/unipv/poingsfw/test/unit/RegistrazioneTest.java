package it.unipv.poingsfw.test.unit;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import it.unipv.poingsfw.controller.GestoreRegistrazione;
import it.unipv.poingsfw.domain.LivelloAbbonamento;
import it.unipv.poingsfw.domain.Sede;
import it.unipv.poingsfw.domain.TipoAbbonamento;
import it.unipv.poingsfw.exceptions.PagamentoFallitoException;

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
        // Arrange: Dati validi.
        String nome = "Mario";
        String cognome = "Rozzi";
        String password = "PasswordSicura123!";
        String ibanValido = "IT99Z0123456789012345678901";
        
        // TRUCCO: Usiamo il timestamp per generare Email e CF sempre unici ad ogni esecuzione!
        long timestamp = System.currentTimeMillis();
        String emailUnivoca = "mario.rozzi" + timestamp + "@email.it";
        String cfUnivoco = "CF" + timestamp; 

        try {
            // Act: Chiamiamo il metodo con i dati generati dinamicamente
            gestoreRegistrazione.registraNuovoCliente(nome, cognome, emailUnivoca, password, cfUnivoco, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, ibanValido);
            
            // Assert: Se arriviamo a questa riga, tutto è andato liscio
            assertTrue("La registrazione è avvenuta con successo senza lanciare eccezioni", true);
            
        } catch (Exception e) {
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
 // TEST 3: Flusso Alternativo 2 (Utente Già Esistente)
    @Test
    public void testRegistrazioneFallitaPerUtenteDuplicato() {
        // Arrange: Inventiamo un Codice Fiscale specifico per questo test
        String cfDuplicato = "CFDUPLICATO12345"; 

        // 1. FORZATURA: Registriamo l'utente una prima volta per assicurarci che esista nel DB
        try {
            gestoreRegistrazione.registraNuovoCliente("Anna", "Verdi", "anna@email.it", 
                                                      "Password123!", cfDuplicato, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, "IT99Z0123456789012345678901");
        } catch (Exception e) {
            // Se esiste già da lanci precedenti, ignoriamo l'errore e andiamo avanti
        }

        // 2. Act & Assert: Ora che siamo CERTI che esista, proviamo a registrarlo di nuovo!
        // Ci aspettiamo che il sistema blocchi la duplicazione lanciando l'eccezione
        assertThrows(it.unipv.poingsfw.exceptions.UtenteGiaEsistenteException.class, () -> {
            gestoreRegistrazione.registraNuovoCliente("Anna", "Verdi", "anna@email.it", 
                                                      "Password123!", cfDuplicato, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, "IT99Z0123456789012345678901");
        });
    }
}