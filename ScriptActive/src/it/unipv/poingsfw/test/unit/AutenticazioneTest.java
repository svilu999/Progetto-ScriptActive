package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.controller.LoginController;
import it.unipv.poingsfw.domain.Abbonamento;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Direttore;
import it.unipv.poingsfw.domain.LivelloAbbonamento;
import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.domain.TipoAbbonamento;
import it.unipv.poingsfw.test.fake.NavigatoreFake;
import it.unipv.poingsfw.test.fake.UtenteDAOFake;
import it.unipv.poingsfw.test.fake.VistaLoginFake;

/**
 * La classe {@code AutenticazioneTest} costituisce la suite di <b>Testing di Unità</b> (Unit Testing) 
 * che collauda in isolamento la logica di autenticazione.
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Questa suite automatizza la verifica dello <b>Use Case UC6: Autenticazione (Login)</b>. 
 * Accerta che il sistema gestisca correttamente il flusso in cui l'attore primario (Cliente, Direttore o Personal Trainer) 
 * fornisca credenziali valide (Main Success Scenario) o non valide (Alternative Flows), garantendo il rispetto 
 * del requisito di sistema RS3 relativo al controllo degli accessi.
 * </p>
 * <p>
 * Il componente sotto test (il {@link LoginController}) dipende soltanto da interfacce: la persistenza,
 * la vista e la navigazione sono sostituite da oggetti fittizi. I test non aprono finestre, non mostrano
 * dialog e non richiedono il database: possono quindi essere eseguiti in automatico su qualsiasi macchina.
 * </p>
 * 
 * @author Simone
 * @version 2.0
 * @see it.unipv.poingsfw.controller.LoginController
 */
class AutenticazioneTest {

    private LoginController controller;
    private VistaLoginFake view;
    private UtenteDAOFake dao;
    private NavigatoreFake navigatore;

    @BeforeEach
    void setUp() {
        view = new VistaLoginFake();
        dao = new UtenteDAOFake();
        navigatore = new NavigatoreFake();
        controller = new LoginController(view, dao, navigatore);
    }

    /** Crea un cliente attivo il cui abbonamento scade tra {@code mesiAllaScadenza} mesi (negativo = già scaduto). */
    private Cliente creaCliente(int mesiAllaScadenza) {
        Cliente cliente = new Cliente("Mario", "Rossi", "m@m.it", "CF1", TipoAbbonamento.BASE);
        cliente.setStato("Attivo");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, mesiAllaScadenza);
        Abbonamento abbonamento = new Abbonamento("CF1", LivelloAbbonamento.ANNUALE, TipoAbbonamento.BASE, false, "IBAN123");
        abbonamento.setDataScadenza(cal.getTime());
        cliente.setAbbonamentoAttivo(abbonamento);
        return cliente;
    }

    // --- MAIN SUCCESS SCENARIO ---

    /**
     * Verifica il <b>Main Success Scenario dello Use Case UC6</b> (Login con credenziali corrette).
     * Il controller memorizza l'utente autenticato, chiude il login e instrada il cliente alla sua dashboard.
     */
    @Test
    void testEffettuaLogin_Successo() {
        dao.setUtenteDaRestituire(creaCliente(12));

        controller.effettuaLogin("m@m.it", "password123");

        assertNotNull(controller.getUtenteLoggato(),
                "L'utente loggato deve essere presente in memoria dopo un'autenticazione riuscita");
        assertEquals("m@m.it", controller.getUtenteLoggato().getEmail());
        assertTrue(view.isChiusa(), "La schermata di login deve essere chiusa dopo l'accesso");
        assertEquals(NavigatoreFake.DASHBOARD_CLIENTE, navigatore.getUltimaDestinazione());
        assertTrue(view.getErrori().isEmpty());
    }

    /** Il Double Dispatch instrada ogni ruolo verso la propria area riservata. */
    @Test
    void testEffettuaLogin_DirettoreInstradatoAllaSuaDashboard() {
        Direttore direttore = new Direttore("Anna", "Bianchi", "direttore@scriptactive.it", "DIR-001");
        direttore.setStato("Attivo");
        dao.setUtenteDaRestituire(direttore);

        controller.effettuaLogin("direttore@scriptactive.it", "1234");

        assertEquals(NavigatoreFake.DASHBOARD_DIRETTORE, navigatore.getUltimaDestinazione());
    }

    @Test
    void testEffettuaLogin_TrainerInstradatoAllaSuaArea() {
        PersonalTrainer trainer = new PersonalTrainer("Marco", "Verdi", "trainer@scriptactive.it", "2");
        trainer.setStato("Attivo");
        dao.setUtenteDaRestituire(trainer);

        controller.effettuaLogin("trainer@scriptactive.it", "1234");

        assertEquals(NavigatoreFake.AREA_TRAINER, navigatore.getUltimaDestinazione());
    }

    // --- FLUSSI ALTERNATIVI ---

    /**
     * Verifica i <b>Flussi Alternativi dello Use Case UC6</b> (Login con credenziali errate o inesistenti).
     * Se il livello dati non trova l'utente, il controller blocca l'accesso e lo comunica tramite la vista.
     */
    @Test
    void testEffettuaLogin_CredenzialiErrate() {
        dao.setUtenteDaRestituire(null);

        controller.effettuaLogin("wrong@email.it", "wrong");

        assertNull(controller.getUtenteLoggato(),
                "Il controller non deve registrare alcun utente in stato di loggato a fronte di credenziali errate");
        assertEquals(1, view.getErrori().size(), "La vista deve mostrare un errore");
        assertFalse(view.isChiusa(), "La schermata di login deve restare aperta");
        assertNull(navigatore.getUltimaDestinazione(), "Non deve avvenire alcuna navigazione");
    }

    /** Verifica il Flusso Alternativo 2 (Account disabilitato o sospeso). */
    @Test
    void testEffettuaLogin_AccountDisabilitato() {
        Cliente clienteSospeso = creaCliente(12);
        clienteSospeso.setStato("Inattivo");
        dao.setUtenteDaRestituire(clienteSospeso);

        controller.effettuaLogin("luigi@email.it", "password123");

        assertNull(controller.getUtenteLoggato(),
                "Il controller non deve far accedere un utente con stato Inattivo");
        assertEquals(1, view.getErrori().size());
        assertNull(navigatore.getUltimaDestinazione());
    }

    @Test
    void testEffettuaLogin_CampiVuoti() {
        controller.effettuaLogin("", "");

        assertNull(controller.getUtenteLoggato());
        assertEquals(1, view.getErrori().size());
        assertNull(navigatore.getUltimaDestinazione());
    }

    // --- ABBONAMENTO SCADUTO ---

    @Test
    void testEffettuaLogin_AbbonamentoScaduto_UtenteAccettaIlRinnovo() {
        dao.setUtenteDaRestituire(creaCliente(-1));
        view.setRispostaRinnovo(true);

        controller.effettuaLogin("m@m.it", "password123");

        assertNull(controller.getUtenteLoggato(), "Con l'abbonamento scaduto l'accesso non deve essere concesso");
        assertNotNull(view.getMessaggioRinnovo(), "La vista deve proporre il rinnovo");
        assertEquals(NavigatoreFake.RINNOVO, navigatore.getUltimaDestinazione());
        assertEquals("m@m.it", navigatore.getEmailRinnovo());
        assertTrue(view.isChiusa());
    }

    @Test
    void testEffettuaLogin_AbbonamentoScaduto_UtenteRifiutaIlRinnovo() {
        dao.setUtenteDaRestituire(creaCliente(-1));
        view.setRispostaRinnovo(false);

        controller.effettuaLogin("m@m.it", "password123");

        assertNull(controller.getUtenteLoggato());
        assertNull(navigatore.getUltimaDestinazione(), "Senza conferma non si apre la schermata di rinnovo");
        assertFalse(view.isChiusa(), "La schermata di login deve restare aperta");
    }

    @Test
    void testEffettuaLogin_AbbonamentoScaduto_SbloccatoDalRinnovoAutomatico() {
        dao.setUtenteDaRestituire(creaCliente(-1));
        dao.setEsitoRinnovoSilenzioso(true);

        controller.effettuaLogin("m@m.it", "password123");

        assertNotNull(controller.getUtenteLoggato(),
                "Se il rinnovo automatico è andato a buon fine l'accesso deve essere concesso");
        assertEquals(NavigatoreFake.DASHBOARD_CLIENTE, navigatore.getUltimaDestinazione());
    }

    // --- DIRAMAZIONE VERSO LA REGISTRAZIONE ---

    @Test
    void testRichiediRegistrazione_ApreIlModuloDiRegistrazione() {
        controller.richiediRegistrazione();

        assertTrue(view.isChiusa());
        assertEquals(NavigatoreFake.REGISTRAZIONE, navigatore.getUltimaDestinazione());
    }
}
