package it.unipv.posfw.test.unit;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import it.unipv.posfw.controller.LoginController;
import it.unipv.posfw.database.UtenteDAO;
import it.unipv.posfw.domain.Utente;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.view.LoginView;

/**
 * La classe {@code AutenticazioneTest} costituisce la suite di <b>Testing di Unità</b> (Unit Testing) 
 * implementata avvalendosi del framework <b>JUnit 4</b> per collaudare in isolamento la logica di autenticazione.
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Questa suite automatizza la verifica dello <b>Use Case UC6: Autenticazione (Login)</b>. 
 * Accerta che il sistema gestisca correttamente il flusso in cui l'attore primario (Cliente, Direttore o Personal Trainer) 
 * fornisca credenziali valide (Main Success Scenario) o non valide (Alternative Flows), garantendo il rispetto 
 * del requisito di sistema RS3 relativo al controllo degli accessi.
 * </p>
 * <p>
 * Nel pieno rispetto delle linee guida di Ingegneria del Software, l'attività di test dei difetti isola il componente sotto test 
 * (il {@link LoginController}) svincolandolo dall'effettiva connessione al database. Ciò è ottenuto fornendo una implementazione 
 * fittizia (Mock) dell'interfaccia {@link UtenteDAO} mediante l'impiego di una classe anonima, perchè mi risultava piu semplice.
 * </p>
 * 
 * @author Simone
 * @version 1.0
 * @see org.junit.Test
 * @see it.unipv.posfw.controller.LoginController
 */
public class AutenticazioneTest {

    private LoginController controller;
    private LoginView view;
    
    /**
     * Riferimento all'interfaccia DAO utilizzata per simulare e isolare l'accesso al livello di persistenza.
     */
    private UtenteDAO testDao;
    
    /**
     * Oggetto di dominio utilizzato per forzare lo stato di ritorno del DAO mockato durante l'esecuzione del test.
     */
    private Utente utenteDaRestituire;

    /**
     * Metodo annotato con {@link org.junit.Before} per l'esecuzione preliminare automatica prima di ogni test.
     * <p>
     * <b>Motivazione Architetturale:</b> Questa fase costituisce il <i>Setup</i> architetturale del test. 
     * Per testare il Controller in totale isolamento (Unit Testing puro), viene creata una <b>Classe Anonima</b> che 
     * implementa formalmente l'interfaccia {@code UtenteDAO}. Questa tecnica elimina l'accoppiamento con il livello 
     * di persistenza reale (database MySQL), permettendo al test di governare artificialmente l'input di ritorno 
     * attraverso l'attributo {@code utenteDaRestituire}.
     * </p>
     */
    @Before
    public void setUp() {
        // Fase di SETUP: Inizializzazione della vista delegata (Top-level container Swing fittizio).
        view = new LoginView();
        
        // Iniezione di una dipendenza (Mocking) tramite Classe Anonima per l'interfaccia Data Access Object.
        testDao = new UtenteDAO() {
            @Override
            public Utente effettuaLogin(String email, String password) {
                // Restituisce l'oggetto preconfigurato dal test case, eludendo la reale esecuzione della query SQL.
                return utenteDaRestituire;
            }

            @Override
            public void registraCliente(String cf, String nome, String cognome, String email, String passwordHash) {
                // Metodo astratto implementato ma non invocato in questa partizione di test dedicata all'UC6.
            }
        };
        
        // Inizializzazione del Controller con le dipendenze mockate (Pattern Architetturale MVC).
        controller = new LoginController(view, testDao);
    }

    /**
     * Verifica il <b>Main Success Scenario dello Use Case UC6</b> (Login con credenziali corrette).
     * <p>
     * Avvalendosi di una partizione di equivalenza di correttezza (input di autenticazione validi), il test accerta che 
     * il Controller aggiorni in modo affidabile lo stato dell'applicazione, intercettando e memorizzando l'utente 
     * restituito dal DAO come entità attiva nel sistema. La validazione è operata dai metodi di asserzione 
     * {@link org.junit.Assert#assertNotNull(String, Object)} e {@link org.junit.Assert#assertEquals(String, Object, Object)}.
     * </p>
     */
    @Test
    public void testEffettuaLogin_Successo() {
        // Fase di SETUP: Configurazione dell'ambiente isolato per simulare l'identificazione positiva dell'utente nel DB.
        utenteDaRestituire = new Cliente("Mario", "Rossi", "m@m.it", "CF1", null);
        
        // Fase di ACTION: Invocazione della logica di business simulando i flussi di input dell'attore primario.
        controller.effettuaLogin("m@m.it", "password123");
        
        // Fase di ASSERTION: Validazione dello stato postcondizionale del sistema.
        assertNotNull("L'utente loggato deve essere presente in memoria dopo un'autenticazione riuscita", controller.getUtenteLoggato());
        assertEquals("m@m.it", controller.getUtenteLoggato().getEmail());
    }

    /**
     * Verifica i <b>Flussi Alternativi dello Use Case UC6</b> (Login con credenziali errate o inesistenti).
     * <p>
     * Avvalendosi di una partizione di non correttezza (deviazione del comportamento atteso), il test forza il sistema 
     * a elaborare un input illecito. Accerta che, nel caso in cui il livello dati restituisca {@code null} 
     * (record non rintracciato), l'architettura blocchi l'accesso mantenendo vuoto lo stato dell'oggetto Controller. 
     * La convalida avviene mediante l'asserzione mirata {@link org.junit.Assert#assertNull(String, Object)}.
     * </p>
     */
    @Test
    public void testEffettuaLogin_CredenzialiErrate() {
        // Fase di SETUP: Configurazione dell'ambiente isolato per simulare un riscontro negativo del database.
        utenteDaRestituire = null;
        
        // Fase di ACTION: Invocazione del metodo del Controller iniettando i parametri della partizione di non correttezza.
        controller.effettuaLogin("wrong@email.it", "wrong");
        
        // Fase di ASSERTION: Verifica della solidità del sistema e del mantenimento delle barriere di autenticazione.
        assertNull("Il controller non deve registrare alcun utente in stato di loggato a fronte di credenziali errate", controller.getUtenteLoggato());
    }
}