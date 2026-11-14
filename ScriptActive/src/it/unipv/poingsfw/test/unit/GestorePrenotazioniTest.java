package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.controller.GestorePrenotazioni;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.exceptions.CorsoAlCompletoException;
import it.unipv.poingsfw.exceptions.PrenotazioneGiaEffettuataException;
import it.unipv.poingsfw.exceptions.PrenotazioneInesistenteException;
import it.unipv.poingsfw.test.fake.CorsoDAOInMemoria;
import it.unipv.poingsfw.test.fake.PrenotazioneDAOInMemoria;

/**
 * Test di unità del {@link GestorePrenotazioni}: iscrizione, lista d'attesa e
 * avanzamento automatico della coda.
 * <p>
 * I DAO sono sostituiti da implementazioni in memoria: i test non richiedono il
 * database. La verifica contro MySQL reale è nella suite
 * {@code test.integration.GestorePrenotazioniIntegrationTest}.
 * </p>
 */
class GestorePrenotazioniTest {

    private static final String CONFERMATA = "Confermata";
    private static final String IN_LISTA_ATTESA = "InListaAttesa";

    private PrenotazioneDAOInMemoria prenotazioneDAO;
    private CorsoDAOInMemoria corsoDAO;
    private GestorePrenotazioni gestore;

    private Cliente mario;
    private Cliente lorenzo;

    @BeforeEach
    void setUp() {
        prenotazioneDAO = new PrenotazioneDAOInMemoria();
        corsoDAO = new CorsoDAOInMemoria();
        gestore = new GestorePrenotazioni(prenotazioneDAO, corsoDAO);

        mario = new Cliente("Mario", "Rossi", "mario.rossi@email.it", "RSSMRA80A01H501Z");
        mario.setId(3);
        lorenzo = new Cliente("Lorenzo", "Varano", "lorenzo@studenti.unipv.it", "VRNLRN99M21F205W");
        lorenzo.setId(4);
    }

    private Corso creaCorso(String id, int posti) {
        return new Corso(id, "Corso " + id, LocalDateTime.now().plusDays(1), posti, null);
    }

    @Test
    void testPrenotazioneSuccesso() throws Exception {
        Corso corso = creaCorso("1", 15);

        gestore.prenotaCorso(mario, corso);

        assertEquals(CONFERMATA, prenotazioneDAO.getStato("3", "1"));
        assertEquals(14, corso.getPostiDisponibili(), "La prenotazione deve occupare un posto");
        assertEquals(1, corsoDAO.getAggiornamentiPosti(), "La nuova disponibilità deve essere resa persistente");
    }

    @Test
    void testEccezioneDoppiaPrenotazione() throws Exception {
        Corso corso = creaCorso("1", 15);
        gestore.prenotaCorso(mario, corso);

        assertThrows(PrenotazioneGiaEffettuataException.class, () -> gestore.prenotaCorso(mario, corso));
        assertEquals(14, corso.getPostiDisponibili(), "Il secondo tentativo non deve occupare altri posti");
    }

    @Test
    void testCorsoPieno_InserimentoInListaDAttesa() throws Exception {
        Corso corso = creaCorso("5", 1);
        gestore.prenotaCorso(mario, corso);

        assertThrows(CorsoAlCompletoException.class, () -> gestore.prenotaCorso(lorenzo, corso));

        assertEquals(IN_LISTA_ATTESA, prenotazioneDAO.getStato("4", "5"));
        assertEquals(0, corso.getPostiDisponibili());
    }

    @Test
    void testListaAttesaESwap() throws Exception {
        Corso corso = creaCorso("5", 1);
        gestore.prenotaCorso(mario, corso);
        assertThrows(CorsoAlCompletoException.class, () -> gestore.prenotaCorso(lorenzo, corso));

        gestore.annullaPrenotazione(mario, corso);

        assertNull(prenotazioneDAO.getStato("3", "5"), "La prenotazione annullata deve essere rimossa");
        assertEquals(CONFERMATA, prenotazioneDAO.getStato("4", "5"),
                "Il primo cliente in lista d'attesa deve essere promosso");
        assertEquals(0, corso.getPostiDisponibili(), "Il posto liberato è stato subito riassegnato");
    }

    @Test
    void testAnnullamentoSenzaListaDAttesa_LiberaUnPosto() throws Exception {
        Corso corso = creaCorso("1", 15);
        gestore.prenotaCorso(mario, corso);

        gestore.annullaPrenotazione(mario, corso);

        assertFalse(prenotazioneDAO.esistePrenotazione("3", "1"));
        assertEquals(15, corso.getPostiDisponibili());
    }

    @Test
    void testAnnullamentoPrenotazioneInesistente_LanciaEccezione() {
        Corso corso = creaCorso("1", 15);

        assertThrows(PrenotazioneInesistenteException.class, () -> gestore.annullaPrenotazione(mario, corso));
    }

    @Test
    void testErroreDiPersistenza_NonOccupaIlPosto() {
        Corso corso = creaCorso("1", 15);
        prenotazioneDAO.setInserimentoFallisce(true);

        Exception errore = assertThrows(Exception.class, () -> gestore.prenotaCorso(mario, corso));

        assertFalse(errore instanceof CorsoAlCompletoException);
        assertEquals(15, corso.getPostiDisponibili());
        assertTrue(corsoDAO.findAll().isEmpty());
    }
}
