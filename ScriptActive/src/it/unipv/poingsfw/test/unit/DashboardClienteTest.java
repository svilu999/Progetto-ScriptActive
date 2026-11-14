package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.controller.DashboardClienteController;
import it.unipv.poingsfw.controller.GestorePrenotazioni;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.test.fake.CorsoDAOInMemoria;
import it.unipv.poingsfw.test.fake.NavigatoreFake;
import it.unipv.poingsfw.test.fake.PrenotazioneDAOInMemoria;
import it.unipv.poingsfw.test.fake.VistaDashboardClienteFake;

/**
 * Test di unità del {@link DashboardClienteController}: popolamento della dashboard
 * e navigazione verso palinsesto e area Premium.
 */
class DashboardClienteTest {

    private VistaDashboardClienteFake view;
    private NavigatoreFake navigatore;
    private Cliente cliente;
    private Corso corsoPrenotato;

    @BeforeEach
    void setUp() {
        view = new VistaDashboardClienteFake();
        navigatore = new NavigatoreFake();
        cliente = new Cliente("Mario", "Rossi", "mario.rossi@email.it", "RSSMRA80A01H501Z");
        cliente.setId(3);
        corsoPrenotato = new Corso("1", "Corso di Funzionale", LocalDateTime.now().plusDays(1), 15, null);
    }

    private DashboardClienteController creaController(PrenotazioneDAOInMemoria prenotazioneDAO) {
        GestorePrenotazioni gestore = new GestorePrenotazioni(prenotazioneDAO, new CorsoDAOInMemoria());
        return new DashboardClienteController(view, gestore, navigatore, cliente);
    }

    @Test
    void testInizializza_MostraClienteECorsiPrenotati() {
        PrenotazioneDAOInMemoria prenotazioneDAO = new PrenotazioneDAOInMemoria() {
            @Override
            public List<Corso> getCorsiPerCliente(String idCliente) {
                return "3".equals(idCliente) ? List.of(corsoPrenotato) : List.of();
            }
        };

        creaController(prenotazioneDAO).inizializza();

        assertSame(cliente, view.getClienteMostrato());
        assertEquals(List.of(corsoPrenotato), view.getCorsiMostrati());
        assertTrue(view.getErrori().isEmpty());
    }

    @Test
    void testInizializza_ErroreDiPersistenza_MostraUnMessaggio() {
        PrenotazioneDAOInMemoria prenotazioneDAO = new PrenotazioneDAOInMemoria() {
            @Override
            public List<Corso> getCorsiPerCliente(String idCliente) {
                throw new RuntimeException("Database non raggiungibile (simulato).");
            }
        };

        creaController(prenotazioneDAO).inizializza();

        assertSame(cliente, view.getClienteMostrato(), "I dati del cliente restano visibili");
        assertEquals(1, view.getErrori().size());
    }

    @Test
    void testApriAreaPremium() {
        creaController(new PrenotazioneDAOInMemoria()).apriAreaPremium();

        assertTrue(view.isChiusa());
        assertEquals(NavigatoreFake.STORICO_ALLENAMENTI, navigatore.getUltimaDestinazione());
        assertSame(cliente, navigatore.getUltimoCliente());
    }

    @Test
    void testApriPalinsesto() {
        creaController(new PrenotazioneDAOInMemoria()).apriPalinsesto();

        assertTrue(view.isChiusa());
        assertEquals(NavigatoreFake.PALINSESTO_CLIENTE, navigatore.getUltimaDestinazione());
    }
}
