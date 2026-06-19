package it.unipv.posfw.test.system;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import it.unipv.posfw.controller.StoricoAllenamentiController;
import it.unipv.posfw.database.SessioneDAOMySQL;
import it.unipv.posfw.view.StoricoAllenamentiView;
import it.unipv.posfw.domain.*;
import it.unipv.posfw.exceptions.*;

/**
 * Suite di <b>System Test</b> per la validazione end-to-end dello Use Case UC4.
 * <p>
 * Questo test verifica l'interazione tra i componenti reali del sistema (Controller, View e DAO). 
 * A differenza degli Unit Test, questi test di sistema verificano che l'integrazione tra 
 * il livello applicativo e il livello di persistenza (Database reale) sia corretta.
 * </p>
 * <p>
 * <b>Tracciabilità:</b> Copre il Main Success Scenario e l'Alternative Flow 1 dello Use Case UC4.
 * </p>
 * * @author Vilucchi
 * @version 1.1
 */
public class RegistrazioniPrestazioniSystemTest {

    private StoricoAllenamentiController controller;
    private StoricoAllenamentiView view;

    /**
     * Setup della Test Fixture: istanziazione dei componenti reali del sistema.
     */
    @Before
    public void setUp() {
        view = new StoricoAllenamentiView();
        /* Inizializzazione della dipendenza verso il database reale MySQL */
        controller = new StoricoAllenamentiController(view, new SessioneDAOMySQL());
        view.setController(controller);
    }

    /**
     * Test del <i>Main Success Scenario</i> (UC4): Verifica che un utente Premium
     * possa accedere correttamente alla sezione riservata.
     */
    @Test
    public void testMainSuccessScenario_AccessoPremiumConsentito() {
        /* GIVEN: Cliente con profilo Premium */
        Cliente premium = new Cliente("Luigi", "Verdi", "l@l.it", "CF2", TipoAbbonamento.PREMIUM);
        
        /* WHEN: Il controller valuta l'accesso */
        try {
            controller.gestisciAccessoSezione(premium);
            
            /* THEN: L'accesso deve essere concesso senza eccezioni */
            assertTrue("Accesso negato erroneamente a utente Premium", true); 
        } catch (UtenteNonPremiumException e) {
            fail("Il sistema ha bloccato un utente Premium autorizzato: " + e.getMessage());
        }
    }

    /**
     * Test dell'<i>Alternative Flow 1</i> (UC4): Verifica che un utente Base
     * sia correttamente inibito dall'accesso alla sezione Premium.
     */
    @Test
    public void testAlternativeFlow1_UtenteBaseBloccato() {
        /* GIVEN: Cliente con profilo Base */
        Cliente base = new Cliente("Mario", "Rossi", "m@m.it", "CF1", TipoAbbonamento.BASE);

        /* WHEN & THEN: L'accesso deve scatenare l'eccezione prevista */
        try {
            controller.gestisciAccessoSezione(base);
            fail("Violazione di Sicurezza: Il sistema ha permesso l'accesso a un utente Base non autorizzato.");
        } catch (UtenteNonPremiumException e) {
            /* Test superato: il sistema ha correttamente applicato la policy di dominio */
            assertNotNull(e); 
        }
    }
}