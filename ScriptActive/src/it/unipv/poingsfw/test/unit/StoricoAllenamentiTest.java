package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.controller.StoricoAllenamentiController;
import it.unipv.poingsfw.dao.SessioneDAO;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.DatiFormPojo;
import it.unipv.poingsfw.domain.SessioneAllenamento;
import it.unipv.poingsfw.domain.TipoAbbonamento;
import it.unipv.poingsfw.exceptions.DatiAllenamentoNonValidiException;
import it.unipv.poingsfw.exceptions.SalvataggioFallitoException;
import it.unipv.poingsfw.exceptions.SchedaVuotaException;
import it.unipv.poingsfw.exceptions.UtenteNonPremiumException;
import it.unipv.poingsfw.test.fake.NavigatoreFake;
import it.unipv.poingsfw.test.fake.VistaStoricoAllenamentiFake;

/**
 * Test di unità dello <b>Use Case UC4: Registrazione e Monitoraggio Prestazioni</b>.
 * <p>
 * Il controller è collaudato in isolamento: vista, persistenza e navigazione sono
 * oggetti fittizi, quindi i test non aprono finestre e non richiedono il database.
 * </p>
 */
class StoricoAllenamentiTest {

    /** Stub in memoria della persistenza delle sessioni. */
    private static final class SessioneDAOStub implements SessioneDAO {
        private final List<SessioneAllenamento> salvate = new ArrayList<>();
        private boolean esitoOperazioni = true;

        @Override
        public boolean salvaSessione(SessioneAllenamento sessione) {
            if (esitoOperazioni) {
                salvate.add(sessione);
            }
            return esitoOperazioni;
        }

        @Override
        public boolean eliminaSessioneSpecifica(SessioneAllenamento sessione) {
            return esitoOperazioni && salvate.remove(sessione);
        }

        @Override
        public List<SessioneAllenamento> getStorico(String idCliente) {
            return new ArrayList<>(salvate);
        }
    }

    private StoricoAllenamentiController controller;
    private VistaStoricoAllenamentiFake view;
    private SessioneDAOStub dao;
    private NavigatoreFake navigatore;

    private Cliente clientePremium;
    private Cliente clienteBase;

    @BeforeEach
    void setUp() {
        view = new VistaStoricoAllenamentiFake();
        dao = new SessioneDAOStub();
        navigatore = new NavigatoreFake();
        controller = new StoricoAllenamentiController(view, dao, navigatore);

        clientePremium = new Cliente("Mario", "Rossi", "m@m.it", "CF1", TipoAbbonamento.PREMIUM);
        clienteBase = new Cliente("Luigi", "Base", "l@l.it", "CF2", TipoAbbonamento.BASE);
    }

    private List<DatiFormPojo> unEsercizio(String nome, double carico, int ripetizioni) {
        List<DatiFormPojo> esercizi = new ArrayList<>();
        esercizi.add(new DatiFormPojo(nome, carico, ripetizioni));
        return esercizi;
    }

    // --- MAIN SUCCESS SCENARIO ---

    @Test
    void testSalvaSessione_Successo() throws Exception {
        controller.salvaSessioneCompleta(new Date(), unEsercizio("Squat", 50, 10), clientePremium);

        assertEquals(1, dao.salvate.size(), "La sessione deve essere affidata al livello di persistenza");
        assertEquals("CF1", dao.salvate.get(0).getIdCliente());
        assertEquals(1, view.getAggiornamentiStorico(), "Dopo il salvataggio la vista deve essere aggiornata");
        assertEquals(1, view.getStoricoMostrato().size());
    }

    @Test
    void testGestisciAccesso_UtentePremium() throws Exception {
        controller.gestisciAccessoSezione(clientePremium);

        assertTrue(view.isModuloInserimentoMostrato(), "L'utente Premium deve vedere il modulo di inserimento");
        assertEquals(1, view.getAggiornamentiStorico(), "Lo storico deve essere caricato all'accesso");
    }

    // --- ECCEZIONI (FLUSSI ALTERNATIVI) ---

    // 1. Scheda vuota
    @Test
    void testSalvaSessione_SchedaVuota() {
        assertThrows(SchedaVuotaException.class,
                () -> controller.salvaSessioneCompleta(new Date(), new ArrayList<>(), clientePremium));
        assertTrue(dao.salvate.isEmpty());
    }

    // 2. Carico negativo (dati non validi)
    @Test
    void testSalvaSessione_CaricoNegativo() {
        assertThrows(DatiAllenamentoNonValidiException.class,
                () -> controller.salvaSessioneCompleta(new Date(), unEsercizio("Panca", -10, 10), clientePremium));
        assertTrue(dao.salvate.isEmpty(), "Una sessione non valida non deve raggiungere la persistenza");
    }

    @Test
    void testSalvaSessione_RipetizioniNonPositive() {
        assertThrows(DatiAllenamentoNonValidiException.class,
                () -> controller.salvaSessioneCompleta(new Date(), unEsercizio("Panca", 40, 0), clientePremium));
        assertTrue(dao.salvate.isEmpty());
    }

    // 3. Utente non Premium (accesso negato)
    @Test
    void testGestisciAccesso_UtenteNonPremium() {
        assertThrows(UtenteNonPremiumException.class, () -> controller.gestisciAccessoSezione(clienteBase));
        assertFalse(view.isModuloInserimentoMostrato());
    }

    // 4. La persistenza rifiuta il salvataggio
    @Test
    void testSalvaSessione_SalvataggioFallito() {
        dao.esitoOperazioni = false;

        assertThrows(SalvataggioFallitoException.class,
                () -> controller.salvaSessioneCompleta(new Date(), unEsercizio("Squat", 50, 10), clientePremium));
        assertEquals(0, view.getAggiornamentiStorico(), "Se il salvataggio fallisce la vista non va aggiornata");
    }

    // --- ELIMINAZIONE ---

    @Test
    void testEliminaSessione_Successo() throws Exception {
        controller.salvaSessioneCompleta(new Date(), unEsercizio("Squat", 50, 10), clientePremium);
        SessioneAllenamento salvata = dao.salvate.get(0);

        controller.eliminaSessioneSelezionata(salvata, clientePremium);

        assertTrue(dao.salvate.isEmpty());
        assertTrue(view.getStoricoMostrato().isEmpty(), "Lo storico mostrato deve riflettere l'eliminazione");
    }

    @Test
    void testEliminaSessione_Fallita() {
        dao.esitoOperazioni = false;
        SessioneAllenamento sessione = new SessioneAllenamento(new Date(), "CF1");

        assertThrows(SalvataggioFallitoException.class,
                () -> controller.eliminaSessioneSelezionata(sessione, clientePremium));
    }

    // --- NAVIGAZIONE ---

    @Test
    void testTornaAllaDashboard_RiapreLaDashboardDelCliente() throws Exception {
        controller.gestisciAccessoSezione(clientePremium);

        controller.tornaAllaDashboard();

        assertTrue(view.isChiusa());
        assertEquals(NavigatoreFake.DASHBOARD_CLIENTE, navigatore.getUltimaDestinazione());
        assertSame(clientePremium, navigatore.getUltimoCliente());
    }

    @Test
    void testRichiediUpgradePremium_InformaLUtenteSenzaChiudere() {
        controller.richiediUpgradePremium();

        assertEquals(1, view.getAvvisi().size());
        assertFalse(view.isChiusa());
    }
}
