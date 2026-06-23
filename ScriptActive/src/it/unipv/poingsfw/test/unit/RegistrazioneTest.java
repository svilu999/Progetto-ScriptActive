package it.unipv.poingsfw.test.unit;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import it.unipv.poingsfw.controller.GestoreRegistrazione;
import it.unipv.poingsfw.domain.LivelloAbbonamento;
import it.unipv.poingsfw.domain.Sede;
import it.unipv.poingsfw.domain.TipoAbbonamento;
import it.unipv.poingsfw.exceptions.PagamentoFallitoException;

/**
 * La classe {@code RegistrazioneTest} implementa una suite di <b>Test di Unità</b> 
 * utilizzando il framework <b>JUnit</b>.
 * <p>
 * L'obiettivo di questa classe è validare la robustezza e la correttezza del {@link GestoreRegistrazione}. I test verificano sia il Flusso Principale di Successo, 
 * sia i Flussi Alternativi di Errore, garantendo che le eccezioni vengano sollevate e 
 * gestite correttamente in scenari anomali.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class RegistrazioneTest {

    private GestoreRegistrazione gestoreRegistrazione;
    private Sede sedeDiTest;

    /**
     * Metodo di Setup eseguito automaticamente da JUnit prima di ogni singolo test.
     * Serve a inizializzare lo stato dell'ambiente (Fixture) in modo pulito e indipendente.
     */
    
    @Before
    public void setUp() {
    	// Usa il Singleton per ottenere l'istanza del gestore
        gestoreRegistrazione = GestoreRegistrazione.getIstanza();
        
        // Creiamo la sede di test usando il costruttore corretto: (int idSede, String nomeSede)
        sedeDiTest = new Sede(1, "Sede Centrale Milano");
    }

    /**
     * Verifica il Main Success Scenario (Happy Path): 
     * Registrazione completa e andata a buon fine senza interruzioni.
     */
    
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
            gestoreRegistrazione.registraNuovoCliente(nome, cognome, emailUnivoca, password, cfUnivoco, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, ibanValido);
            
            assertTrue("La registrazione è avvenuta con successo senza lanciare eccezioni", true);
            
        } catch (Exception e) {
            fail("La registrazione ha fallito inaspettatamente lanciando l'eccezione: " + e.getMessage());
        }
    }

    /**
     * Verifica il Flusso Alternativo 1: 
     * Fallimento della registrazione dovuto al rifiuto della transazione bancaria (IBAN errato).
     */
    
    @Test
    public void testRegistrazioneFallitaPerPagamentoRespinto() {
        // Arrange: Dati validi, ma l'IBAN contiene la parola "ERRORE"
        String nome = "Luigi";
        String cognome = "Bianchi";
        String email = "luigi.bianchi@email.it";
        String password = "PasswordSicura123!";
        String cf = "BNCLGU90B02H501Z";
        String ibanInvalido = "IT99Z01234ERRORE5678901"; // INNESCA L'ERRORE NEL GESTORE PAGAMENTI

        assertThrows(PagamentoFallitoException.class, () -> {
            gestoreRegistrazione.registraNuovoCliente(nome, cognome, email, password, cf, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, ibanInvalido);
        });
        
        // Il test passa SOLO se viene lanciata l'eccezione PagamentoFallitoException.
        // Ciò dimostra che il flusso alternativo funziona e blocca l'inserimento nel database!
    }

    /**
     * Verifica un altro Flusso Alternativo esistente: 
     * Fallimento della registrazione dovuto al tentativo di inserire un Codice Fiscale già presente nel database.
     */
    
    @Test
    public void testRegistrazioneFallitaPerUtenteDuplicato() {
        String cfDuplicato = "CFDUPLICATO12345"; 

        try {
            gestoreRegistrazione.registraNuovoCliente("Anna", "Verdi", "anna@email.it", 
                                                      "Password123!", cfDuplicato, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, "IT99Z0123456789012345678901");
        } catch (Exception e) {
       
        }

        assertThrows(it.unipv.poingsfw.exceptions.UtenteGiaEsistenteException.class, () -> {
            gestoreRegistrazione.registraNuovoCliente("Anna", "Verdi", "anna@email.it", 
                                                      "Password123!", cfDuplicato, 
                                                      sedeDiTest, TipoAbbonamento.BASE, 
                                                      LivelloAbbonamento.MENSILE, true, "IT99Z0123456789012345678901");
        });
    }
}