package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.controller.GestoreCorsi;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.domain.StatoCorso;
import it.unipv.poingsfw.exceptions.CorsoNonTrovatoException;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.SovrapposizioneOrarioException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.test.fake.CorsoDAOInMemoria;

/**
 * Test di unità del componente {@link GestoreCorsi} (UC3 - organizzazione del palinsesto).
 * <p>
 * Il gestore riceve un {@code CorsoDAO} in memoria: ogni test parte da uno stato pulito
 * e non richiede il database né operazioni di pulizia al termine. La verifica contro
 * MySQL reale è nella suite {@code test.integration.GestoreCorsiIntegrationTest}.
 * </p>
 *
 * @author Lorenzo
 * @version 4.0
 * @see GestoreCorsi
 */
class GestoreCorsiTest {

    private static final String TRAINER = "PT-10";
    private static final String ALTRO_TRAINER = "PT-11";

    private CorsoDAOInMemoria corsoDAO;
    private GestoreCorsi gestore;

    @BeforeEach
    void setUp() {
        // Nel sistema simulato esistono solo i trainer 10 e 11
        corsoDAO = new CorsoDAOInMemoria(Set.of("10", "11"));
        gestore = new GestoreCorsi(corsoDAO);
    }

    private Corso trovaPerNome(String nome) {
        for (Corso corso : gestore.getElencoCorsi()) {
            if (corso.getNome().equals(nome)) {
                return corso;
            }
        }
        return null;
    }

    // ==========================================
    // 1. TEST DI SUCCESSO (Flusso Principale)
    // ==========================================
    @Test
    void testOrganizzaNuovoCorso_Successo() throws Exception {
        LocalDateTime orario = LocalDateTime.of(2030, 11, 20, 10, 0);

        gestore.organizzaNuovoCorso("Yoga Mattutino", orario, 15, TRAINER);

        Corso creato = trovaPerNome("Yoga Mattutino");
        assertNotNull(creato, "Il corso deve essere stato salvato");
        assertEquals(orario, creato.getDataOra());
        assertEquals(15, creato.getCapienzaMassima());
        assertEquals(15, creato.getPostiDisponibili(), "Un corso nuovo ha tutti i posti disponibili");
        assertEquals(StatoCorso.ATTIVO, creato.getStato());
    }

    // ==========================================
    // 2. TEST DI FALLIMENTO (Prevenzione Sovrapposizioni)
    // ==========================================
    @Test
    void testSovrapposizioneOrario_LanciaEccezione() throws Exception {
        LocalDateTime orario = LocalDateTime.of(2030, 12, 10, 18, 0);
        gestore.organizzaNuovoCorso("Crossfit", orario, 20, TRAINER);

        assertThrows(SovrapposizioneOrarioException.class,
                () -> gestore.organizzaNuovoCorso("Pilates", orario, 20, TRAINER));
        assertEquals(1, gestore.getElencoCorsi().size(), "Il secondo corso non deve essere stato salvato");
    }

    @Test
    void testStessoOrarioTrainerDiversi_Consentito() throws Exception {
        LocalDateTime orario = LocalDateTime.of(2030, 12, 10, 18, 0);
        gestore.organizzaNuovoCorso("Crossfit", orario, 20, TRAINER);

        gestore.organizzaNuovoCorso("Pilates", orario, 20, ALTRO_TRAINER);

        assertEquals(2, gestore.getElencoCorsi().size());
    }

    // ==========================================
    // 3. TEST FLUSSO ALTERNATIVO (Trainer Non Valido)
    // ==========================================
    @Test
    void testOrganizzaNuovoCorso_TrainerInesistente_LanciaEccezione() {
        LocalDateTime orario = LocalDateTime.of(2031, 1, 15, 10, 0);

        // Verifica che l'Exception Translation intercetti la violazione di chiave esterna
        assertThrows(TrainerNonValidoException.class,
                () -> gestore.organizzaNuovoCorso("Zumba", orario, 20, "PT-999999"));
        assertTrue(gestore.getElencoCorsi().isEmpty());
    }

    @Test
    void testOrganizzaNuovoCorso_IdTrainerVuoto_LanciaEccezione() {
        LocalDateTime orario = LocalDateTime.of(2031, 1, 15, 10, 0);

        assertThrows(TrainerNonValidoException.class,
                () -> gestore.organizzaNuovoCorso("Zumba", orario, 20, "  "));
    }

    // ==========================================
    // 4. TEST CICLO DI VITA (Annullamento Corso)
    // ==========================================
    @Test
    void testAnnullaCorso_Successo() throws Exception {
        LocalDateTime orario = LocalDateTime.of(2031, 2, 10, 11, 0);
        gestore.organizzaNuovoCorso("Functional Training", orario, 10, TRAINER);
        String idCorso = trovaPerNome("Functional Training").getIdCorso();

        gestore.annullaCorso(idCorso);

        assertEquals(StatoCorso.CANCELLATO, trovaPerNome("Functional Training").getStato(),
                "Lo stato del corso deve essere aggiornato a CANCELLATO.");
        assertTrue(gestore.getPalinsestoCorsi().isEmpty(), "Un corso annullato non compare nel palinsesto");
    }

    @Test
    void testAnnullaCorso_Inesistente_LanciaEccezione() {
        assertThrows(CorsoNonTrovatoException.class, () -> gestore.annullaCorso("NON-ESISTE"));
    }

    // ==========================================
    // 5. INTEGRAZIONE CON LA GESTIONE DEL PERSONALE
    // ==========================================
    @Test
    void testHaCorsiAttiviOFuturi() throws Exception {
        gestore.organizzaNuovoCorso("Yoga", LocalDateTime.now().plusDays(10), 15, TRAINER);

        assertTrue(gestore.haCorsiAttiviOFuturi(TRAINER));
        assertFalse(gestore.haCorsiAttiviOFuturi(ALTRO_TRAINER));
        assertFalse(gestore.haCorsiImminenti(TRAINER), "Un corso tra dieci giorni non è imminente");
    }

    @Test
    void testSostituisciTrainerNeiCorsi() throws Exception {
        gestore.organizzaNuovoCorso("Yoga", LocalDateTime.now().plusDays(10), 15, TRAINER);
        gestore.organizzaNuovoCorso("Pilates", LocalDateTime.now().plusDays(11), 15, TRAINER);

        int riassegnati = gestore.sostituisciTrainerNeiCorsi(TRAINER, ALTRO_TRAINER);

        assertEquals(2, riassegnati);
        assertFalse(gestore.haCorsiAttiviOFuturi(TRAINER));
        assertTrue(gestore.haCorsiAttiviOFuturi(ALTRO_TRAINER));
    }

    @Test
    void testSostituisciTrainer_SostitutoUgualeAlTitolare_LanciaEccezione() {
        assertThrows(SostitutoNonValidoException.class,
                () -> gestore.sostituisciTrainerNeiCorsi(TRAINER, TRAINER));
    }

    @Test
    void testSostituisciTrainer_SostitutoGiaImpegnato_LanciaEccezione() throws Exception {
        LocalDateTime orario = LocalDateTime.now().plusDays(10).withNano(0);
        gestore.organizzaNuovoCorso("Yoga", orario, 15, TRAINER);
        gestore.organizzaNuovoCorso("Pilates", orario, 15, ALTRO_TRAINER);

        assertThrows(SostitutoNonValidoException.class,
                () -> gestore.sostituisciTrainerNeiCorsi(TRAINER, ALTRO_TRAINER));
    }

    @Test
    void testGetElencoCorsi_RestituisceTuttiICorsi() throws Exception {
        gestore.organizzaNuovoCorso("Yoga", LocalDateTime.of(2030, 5, 1, 9, 0), 15, TRAINER);
        gestore.organizzaNuovoCorso("Pilates", LocalDateTime.of(2030, 5, 2, 9, 0), 15, TRAINER);

        List<Corso> elenco = gestore.getElencoCorsi();

        assertEquals(2, elenco.size());
    }
}
