package it.unipv.posfw.test.system;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import it.unipv.posfw.controller.LoginController;
import it.unipv.posfw.database.UtenteDAOMySQL;
import it.unipv.posfw.view.LoginView;

/**
 * Suite di <b>System Test</b> per la validazione end-to-end dello Use Case di Autenticazione.
 * <p>
 * Verifica la coerenza del sistema durante il processo di login, assicurando che lo stato 
 * del Controller (sessione utente) rifletta correttamente l'esito della validazione 
 * eseguita dal livello DAO.
 * </p>
 * * @author Vilucchi
 * @version 1.5
 */
public class AutenticazioneSystemTest {

    private LoginController controller;
    private LoginView view;

    @Before
    public void setUp() {
        view = new LoginView();
        /* Inizializzazione con DAO reale per validazione su Database */
        controller = new LoginController(view, new UtenteDAOMySQL());
        view.setController(controller);
    }

    /**
     * Test del <i>Main Success Scenario</i>: Verifica che a seguito di credenziali 
     * valide, il sistema instanzi correttamente l'entità Utente nel Controller.
     */
    @Test
    public void testLoginSuccesso() {
        /* GIVEN: Credenziali presenti nel DB di test */
        String emailValida = "mario.rossi@email.it";
        String passwordValida = "1234";

        /* WHEN: Esecuzione del metodo di autenticazione */
        controller.effettuaLogin(emailValida, passwordValida);

        /* THEN: Asserzione sullo stato logico del sistema (Sessione Utente) */
        assertNotNull("Dopo il login, il sistema deve aver popolato l'entità utente corrente", 
                      controller.getUtenteLoggato());
        assertEquals("L'email dell'utente loggato deve corrispondere a quella inserita", 
                     emailValida, controller.getUtenteLoggato().getEmail());
    }

    /**
     * Test dell'<i>Alternative Flow</i>: Verifica il comportamento del sistema 
     * in presenza di credenziali non valide (Auth Failure).
     */
    @Test
    public void testLoginFallito_CredenzialiErrate() {
        /* GIVEN: Credenziali non presenti o errate */
        String email = "cliente@test.it";
        String passwordErrata = "wrongPassword";

        /* WHEN: Tentativo di accesso */
        controller.effettuaLogin(email, passwordErrata);

        /* THEN: Il sistema deve mantenere la sessione nulla (nessun accesso concesso) */
        assertNull("In caso di fallimento autenticazione, il sistema non deve istanziare l'utente", 
                   controller.getUtenteLoggato());
    }
}