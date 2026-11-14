package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.controller.RinnovoController;
import it.unipv.poingsfw.domain.Abbonamento;
import it.unipv.poingsfw.domain.LivelloAbbonamento;
import it.unipv.poingsfw.domain.Tariffario;
import it.unipv.poingsfw.domain.TipoAbbonamento;
import it.unipv.poingsfw.service.GestorePagamenti;
import it.unipv.poingsfw.service.GestoreRinnovi;
import it.unipv.poingsfw.test.fake.ClienteDAOInMemoria;
import it.unipv.poingsfw.test.fake.NavigatoreFake;
import it.unipv.poingsfw.test.fake.UtenteDAOFake;
import it.unipv.poingsfw.test.fake.VistaRinnovoFake;

/**
 * Test di unità del rinnovo degli abbonamenti: rinnovo manuale dopo un login bloccato
 * ({@link RinnovoController}), rinnovo automatico in background ({@link GestoreRinnovi})
 * e listino prezzi ({@link Tariffario}).
 */
class RinnovoTest {

    private static final String EMAIL = "mario.rossi@email.it";

    private VistaRinnovoFake view;
    private UtenteDAOFake utenteDAO;
    private NavigatoreFake navigatore;
    private RinnovoController controller;

    @BeforeEach
    void setUp() {
        view = new VistaRinnovoFake();
        utenteDAO = new UtenteDAOFake();
        navigatore = new NavigatoreFake();
        controller = new RinnovoController(view, utenteDAO, navigatore, EMAIL);
    }

    // --- RINNOVO MANUALE ---

    @Test
    void testConfermaRinnovo_AggiornaLAbbonamentoETornaAlLogin() {
        controller.confermaRinnovo(LivelloAbbonamento.SEMESTRALE);

        assertEquals(1, utenteDAO.getRinnovi().size());
        UtenteDAOFake.Rinnovo rinnovo = utenteDAO.getRinnovi().get(0);
        assertEquals(EMAIL, rinnovo.email());
        assertEquals(6, rinnovo.mesi());
        assertEquals("SEMESTRALE", rinnovo.piano());

        assertEquals(LivelloAbbonamento.SEMESTRALE, view.getLivelloConfermato());
        assertTrue(view.isChiusa());
        assertEquals(NavigatoreFake.LOGIN, navigatore.getUltimaDestinazione());
    }

    @Test
    void testConfermaRinnovo_SenzaPianoSelezionato_MostraErrore() {
        controller.confermaRinnovo(null);

        assertTrue(utenteDAO.getRinnovi().isEmpty());
        assertEquals(1, view.getErrori().size());
        assertFalse(view.isChiusa());
        assertNull(navigatore.getUltimaDestinazione());
    }

    @Test
    void testConfermaRinnovo_ErroreDiPersistenza_RestaSullaSchermata() {
        utenteDAO.setErroreInRinnovo(true);

        controller.confermaRinnovo(LivelloAbbonamento.MENSILE);

        assertEquals(1, view.getErrori().size());
        assertNull(view.getLivelloConfermato(), "Non si deve confermare un rinnovo non avvenuto");
        assertFalse(view.isChiusa());
        assertNull(navigatore.getUltimaDestinazione());
    }

    @Test
    void testAnnulla_TornaAlLoginSenzaRinnovare() {
        controller.annulla();

        assertTrue(utenteDAO.getRinnovi().isEmpty());
        assertTrue(view.isChiusa());
        assertEquals(NavigatoreFake.LOGIN, navigatore.getUltimaDestinazione());
    }

    // --- RINNOVO AUTOMATICO ---

    private Abbonamento creaAbbonamentoScaduto(int id, LivelloAbbonamento livello, String iban, Date scadenza) {
        Abbonamento abbonamento = new Abbonamento("CF" + id, livello, TipoAbbonamento.BASE, true, iban);
        abbonamento.setIdAbbonamento(id);
        abbonamento.setDataScadenza(scadenza);
        return abbonamento;
    }

    @Test
    void testElaboraRinnovi_ProlungaGliAbbonamentiPagatiEDisattivaGliAltri() {
        Calendar cal = Calendar.getInstance();
        cal.set(2030, Calendar.JANUARY, 15, 0, 0, 0);
        Date scadenza = cal.getTime();

        ClienteDAOInMemoria clienteDAO = new ClienteDAOInMemoria();
        clienteDAO.aggiungiAbbonamentoDaRinnovare(
                creaAbbonamentoScaduto(1, LivelloAbbonamento.SEMESTRALE, "IT99Z0123456789012345678901", scadenza));
        clienteDAO.aggiungiAbbonamentoDaRinnovare(
                creaAbbonamentoScaduto(2, LivelloAbbonamento.MENSILE, "IT99Z01234ERRORE5678901", scadenza));
        clienteDAO.aggiungiAbbonamentoDaRinnovare(
                creaAbbonamentoScaduto(3, LivelloAbbonamento.MENSILE, null, scadenza));
        GestoreRinnovi gestoreRinnovi = new GestoreRinnovi(clienteDAO, new GestorePagamenti());

        int rinnovati = gestoreRinnovi.elaboraRinnovi();

        assertEquals(1, rinnovati);

        cal.add(Calendar.MONTH, 6);
        assertEquals(cal.getTime(), clienteDAO.getScadenzeAggiornate().get(1),
                "L'abbonamento semestrale pagato deve essere prolungato di sei mesi");
        assertEquals(1, clienteDAO.getScadenzeAggiornate().size());
        assertEquals(java.util.List.of(2, 3), clienteDAO.getRinnoviDisattivati(),
                "Se l'addebito fallisce il rinnovo automatico viene disattivato");
    }

    // --- LISTINO ---

    @Test
    void testTariffario() {
        assertEquals(50.0, Tariffario.calcolaImporto(TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE));
        assertEquals(250.0, Tariffario.calcolaImporto(TipoAbbonamento.BASE, LivelloAbbonamento.SEMESTRALE));
        assertEquals(450.0, Tariffario.calcolaImporto(TipoAbbonamento.BASE, LivelloAbbonamento.ANNUALE));
        assertEquals(65.0, Tariffario.calcolaImporto(TipoAbbonamento.PREMIUM, LivelloAbbonamento.MENSILE));
        assertEquals(12, LivelloAbbonamento.ANNUALE.getMesi());
    }
}
