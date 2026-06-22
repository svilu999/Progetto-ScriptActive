package it.unipv.posfw.test.unit; 

import static org.junit.Assert.*; 
import org.junit.Before; 
import org.junit.Test; 
import java.util.ArrayList; 
import java.util.List; 

import it.unipv.posfw.controller.StoricoAllenamentiController; 
import it.unipv.posfw.dao.SessioneDAO; 
import it.unipv.posfw.domain.*; 
import it.unipv.posfw.view.StoricoAllenamentiView; 
import it.unipv.posfw.exceptions.*; 

/**
 * Suite di Unit Testing per il controller StoricoAllenamenti.
 * Valida la business logic di UC4: registrazione sessioni e gestione privilegi.
 */
public class StoricoAllenamentiTest { 

    private StoricoAllenamentiController controller; 
    private StoricoAllenamentiView view; 
    private SessioneDAO testDao; 
    
    // Attributi di classe per i dati di test (Setup Condiviso)
    private Cliente clientePremium;
    private Cliente clienteBase;

    @Before 
    public void setUp() { 
        // 1. Inizializzazione Vista
        view = new StoricoAllenamentiView(); 
        
        // 2. Setup Dati Condivisi (Utenti)
        clientePremium = new Cliente("Mario", "Rossi", "m@m.it", "CF1", null); 
        clientePremium.setPremium(true); // Utente autorizzato per l'UC4
        
        clienteBase = new Cliente("Luigi", "Base", "l@l.it", "CF2", null); 
        clienteBase.setPremium(false); // Utente NON autorizzato per l'UC4
        
        // 3. Mock manuale (Stub)
        testDao = new SessioneDAO() { 
            @Override 
            public boolean salvaSessione(SessioneAllenamento s) { return true; } 
            
            @Override 
            public boolean eliminaSessioneSpecifica(SessioneAllenamento s) { return true; } 
            
            // ECCO LA MODIFICA: int è diventato String
            @Override 
            public List<SessioneAllenamento> getStorico(String id) { return new ArrayList<>(); } 
        }; 
        
        // 4. Iniezione delle dipendenze nel Controller
        controller = new StoricoAllenamentiController(view, testDao); 
    }

    // --- MAIN SUCCESS SCENARIO ---
    
    /**
     * Verifica il corretto salvataggio di una sessione valida.
     */
    @Test
    public void testSalvaSessione_Successo() throws Exception { 
        List<DatiFormPojo> esercizi = new ArrayList<>(); 
        esercizi.add(new DatiFormPojo("Squat", 50, 10)); // Dati validi (carico >= 0, rep > 0)
        
        // Usiamo il clientePremium già preparato nel setUp
        controller.salvaSessioneCompleta(new java.util.Date(), esercizi, clientePremium); 
        // Se non viene lanciata alcuna eccezione, il test è superato implicitamente
    }

    // --- ECCEZIONI (FLUSSI ALTERNATIVI) ---

    // 1. Test Eccezione: Scheda Vuota 
    @Test(expected = SchedaVuotaException.class) 
    public void testSalvaSessione_SchedaVuota() throws Exception { 
        // Invio di una lista vuota di esercizi
        controller.salvaSessioneCompleta(new java.util.Date(), new ArrayList<>(), clientePremium); 
    } 

    // 2. Test Eccezione: Carico Negativo (Dati non validi) 
    @Test(expected = DatiAllenamentoNonValidiException.class) 
    public void testSalvaSessione_CaricoNegativo() throws Exception { 
        List<DatiFormPojo> esercizi = new ArrayList<>(); 
        esercizi.add(new DatiFormPojo("Panca", -10, 10)); // Creiamo un esercizio non valido (carico < 0)
        
        controller.salvaSessioneCompleta(new java.util.Date(), esercizi, clientePremium); 
    } 

    // 3. Test Eccezione: Utente Non Premium (Accesso negato) 
    @Test(expected = UtenteNonPremiumException.class) 
    public void testGestisciAccesso_UtenteNonPremium() throws Exception { 
        // Usiamo il clienteBase preparato nel setUp (senza privilegi)
        controller.gestisciAccessoSezione(clienteBase); 
    } 
}