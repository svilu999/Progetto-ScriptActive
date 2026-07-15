package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.dao.DirettoreDAO;
import it.unipv.poingsfw.dao.PersonalTrainerDAO;
import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.dto.DatiPersonalTrainer;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.service.GestoreContrattiPersonale;
import it.unipv.poingsfw.service.ServizioRetribuzioni;
import it.unipv.poingsfw.service.ServizioSwapCorsi;
import it.unipv.poingsfw.dto.DatiVisualizzazioneTrainer;

/**
 * Test JUnit del Service applicativo GestoreContrattiPersonale.
 *
 * La classe verifica i principali flussi dell'UC5 senza usare database reale,
 * View o Controller. Le dipendenze vengono sostituite con implementazioni fake
 * in memoria, così il test controlla solo la logica applicativa del Service.
 */
public class GestoreContrattiPersonaleTest {

    private FakePersonalTrainerDAO trainerDAO;
    private FakeDirettoreDAO direttoreDAO;
    private FakeServizioSwapCorsi servizioSwapCorsi;
    private FakeServizioRetribuzioni servizioRetribuzioni;
    private GestoreContrattiPersonale servizioContratti;

    /**
     * Inizializza il Service e le dipendenze fake prima di ogni test.
     */
    @BeforeEach
    public void setUp() {
        trainerDAO = new FakePersonalTrainerDAO();
        direttoreDAO = new FakeDirettoreDAO();
        servizioSwapCorsi = new FakeServizioSwapCorsi();
        servizioRetribuzioni = new FakeServizioRetribuzioni();

        servizioContratti = new GestoreContrattiPersonale(
                trainerDAO,
                direttoreDAO,
                servizioSwapCorsi,
                servizioRetribuzioni
        );
    }

    /**
     * Verifica che un Personal Trainer venga assunto correttamente con ID automatico.
     *
     * @throws TrainerGiaAssuntoException se il trainer risulta già presente
     * @throws TrainerNonValidoException se i dati del trainer non sono validi
     */
    @Test
    public void testAssumiPersonalTrainer_SuccessoConIdAutomatico()
            throws TrainerGiaAssuntoException, TrainerNonValidoException {

        servizioContratti.assumiPersonalTrainer(
                "Mario",
                "Rossi",
                "Mario.Rossi@Test.it",
                "Sala pesi",
                "FISSA_MENSILE",
                1300.0
        );

        assertEquals(1, trainerDAO.numeroSalvataggi);

        DatiPersonalTrainer salvato = trainerDAO.trovaPerEmail("mario.rossi@test.it");

        assertNotNull(salvato);
        assertNotNull(salvato.getIdTrainer());
        assertEquals("Mario", salvato.getNome());
        assertEquals("Rossi", salvato.getCognome());
        assertEquals("mario.rossi@test.it", salvato.getEmail());
        assertEquals("Sala pesi", salvato.getSpecializzazione());
        assertEquals("ATTIVO", salvato.getStatoContratto());
        assertTrue(salvato.isAttivo());
        assertEquals("FISSA_MENSILE", salvato.getTipoRetribuzione());
        assertEquals(1300.0, salvato.getStipendioMensile(), 0.001);
        assertNull(salvato.getCompensoPerLezione());
        assertEquals(99, salvato.getIdDirettore());
    }

    /**
     * Verifica che l'assunzione venga bloccata se esiste già un trainer
     * con la stessa email.
     */
    @Test
    public void testAssumiPersonalTrainer_EmailDuplicata_LanciaEccezione() {
        trainerDAO.salva(creaDatiTrainer(
                10,
                "Luca",
                "Bianchi",
                "luca.bianchi@test.it",
                "Sala pesi",
                true
        ));

        assertThrows(TrainerGiaAssuntoException.class, () -> servizioContratti.assumiPersonalTrainer(
                "Luca",
                "Bianchi",
                "luca.bianchi@test.it",
                "Sala pesi",
                "FISSA_MENSILE",
                1300.0
        ));

        assertEquals(1, trainerDAO.trovaTutti().size());
    }

    
    /**
     * Verifica che un trainer senza corsi attivi o futuri venga licenziato
     * correttamente senza sostituto.
     *
     * @throws TrainerNonValidoException se il trainer non è valido
     * @throws TrainerNonLicenziabileException se il trainer non è licenziabile
     */
    @Test
    public void testLicenziaSenzaSostituto_SenzaCorsi_DisattivaTrainer()
            throws TrainerNonValidoException, TrainerNonLicenziabileException {

        trainerDAO.salva(creaDatiTrainer(
                10,
                "Luca",
                "Bianchi",
                "luca.bianchi@test.it",
                "Sala pesi",
                true
        ));

        servizioSwapCorsi.haCorsiAttiviOFuturi = false;

        servizioContratti.licenziaPersonalTrainerSenzaSostituto("10");

        DatiPersonalTrainer aggiornato = trainerDAO.trovaPerId(10);

        assertFalse(aggiornato.isAttivo());
        assertEquals("LICENZIATO", aggiornato.getStatoContratto());
        assertEquals(1, trainerDAO.numeroEliminazioni);
    }

    /**
     * Verifica che il licenziamento senza sostituto venga bloccato se il trainer
     * ha corsi attivi o futuri.
     */
    @Test
    public void testLicenziaSenzaSostituto_ConCorsiAttivi_LanciaEccezione() {
        trainerDAO.salva(creaDatiTrainer(
                10,
                "Luca",
                "Bianchi",
                "luca.bianchi@test.it",
                "Sala pesi",
                true
        ));

        servizioSwapCorsi.haCorsiAttiviOFuturi = true;

        assertThrows(TrainerNonLicenziabileException.class,
                () -> servizioContratti.licenziaPersonalTrainerSenzaSostituto("10"));

        assertTrue(trainerDAO.trovaPerId(10).isAttivo());
        assertEquals(0, trainerDAO.numeroEliminazioni);
    }

    /**
     * Verifica che il licenziamento con sostituto compatibile esegua lo swap
     * e poi disattivi il trainer uscente.
     *
     * @throws TrainerNonValidoException se il trainer da licenziare non è valido
     * @throws SostitutoNonValidoException se il sostituto non è valido
     */
    @Test
    public void testLicenziaConSostituto_Compatibile_EsegueSwapEDisattiva()
            throws TrainerNonValidoException, SostitutoNonValidoException {

        trainerDAO.salva(creaDatiTrainer(
                10,
                "Luca",
                "Bianchi",
                "luca.bianchi@test.it",
                "Sala pesi",
                true
        ));

        trainerDAO.salva(creaDatiTrainer(
                11,
                "Giulia",
                "Neri",
                "giulia.neri@test.it",
                "Sala pesi",
                true
        ));

        servizioContratti.licenziaPersonalTrainerConSostituto("10", "11");

        assertEquals(1, servizioSwapCorsi.numeroSwapEseguiti);
        assertEquals("10", servizioSwapCorsi.idTrainerDaSostituireRicevuto);
        assertEquals("11", servizioSwapCorsi.idTrainerSostitutoRicevuto);

        assertFalse(trainerDAO.trovaPerId(10).isAttivo());
        assertEquals("LICENZIATO", trainerDAO.trovaPerId(10).getStatoContratto());
        assertTrue(trainerDAO.trovaPerId(11).isAttivo());
    }

    /**
     * Verifica che un sostituto con specializzazione diversa venga rifiutato.
     */
    @Test
    public void testLicenziaConSostituto_NonCompatibile_LanciaEccezione() {
        trainerDAO.salva(creaDatiTrainer(
                10,
                "Luca",
                "Bianchi",
                "luca.bianchi@test.it",
                "Sala pesi",
                true
        ));

        trainerDAO.salva(creaDatiTrainer(
                11,
                "Giulia",
                "Neri",
                "giulia.neri@test.it",
                "Pilates",
                true
        ));

        assertThrows(SostitutoNonValidoException.class,
                () -> servizioContratti.licenziaPersonalTrainerConSostituto("10", "11"));

        assertEquals(0, servizioSwapCorsi.numeroSwapEseguiti);
        assertTrue(trainerDAO.trovaPerId(10).isAttivo());
    }

    /**
     * Verifica che vengano restituiti solo i sostituti compatibili.
     *
     * @throws TrainerNonValidoException se il trainer da licenziare non esiste
     */
    @Test
    public void testGetSostitutiCompatibili_FiltraCorrettamente()
            throws TrainerNonValidoException {

        trainerDAO.salva(creaDatiTrainer(
                10,
                "Luca",
                "Bianchi",
                "luca.bianchi@test.it",
                "Sala pesi",
                true
        ));

        trainerDAO.salva(creaDatiTrainer(
                11,
                "Giulia",
                "Neri",
                "giulia.neri@test.it",
                "Sala pesi",
                true
        ));

        trainerDAO.salva(creaDatiTrainer(
                12,
                "Marco",
                "Verdi",
                "marco.verdi@test.it",
                "Sala pesi",
                false
        ));

        trainerDAO.salva(creaDatiTrainer(
                13,
                "Anna",
                "Rosa",
                "anna.rosa@test.it",
                "Pilates",
                true
        ));

        List<DatiVisualizzazioneTrainer> compatibili = servizioContratti.getSostitutiCompatibili("10");

        assertEquals(1, compatibili.size());
        assertEquals("11", compatibili.get(0).getIdTrainer());
        assertEquals("Giulia Verdi", compatibili.get(0).getNomeCompleto());
        }

    /**
     * Verifica che il calcolo delle retribuzioni venga delegato al servizio dedicato.
     */
    @Test
    public void testCalcolaTotaleRetribuzioniMensili_DelegaAlServizioRetribuzioni() {
        servizioRetribuzioni.totaleDaRestituire = 2750.0;

        double totale = servizioContratti.calcolaTotaleRetribuzioniMensili();

        assertEquals(2750.0, totale, 0.001);
        assertEquals(1, servizioRetribuzioni.numeroChiamate);
    }

    /**
     * Crea un DTO di supporto per i test.
     *
     * @param idTrainer identificativo del trainer
     * @param nome nome del trainer
     * @param cognome cognome del trainer
     * @param email email del trainer
     * @param specializzazione specializzazione del trainer
     * @param attivo stato logico del trainer
     * @return DTO configurato per il test
     */
    private DatiPersonalTrainer creaDatiTrainer(
            Integer idTrainer,
            String nome,
            String cognome,
            String email,
            String specializzazione,
            boolean attivo) {

        return new DatiPersonalTrainer(
                idTrainer,
                "PTTEST" + idTrainer,
                nome,
                cognome,
                email,
                "1234",
                attivo ? "Attivo" : "Inattivo",
                specializzazione,
                "Fisso",
                attivo ? "ATTIVO" : "LICENZIATO",
                attivo,
                "FISSA_MENSILE",
                1400.0,
                null,
                99
        );
    }

    /**
     * Copia un DTO modificando alcuni campi di stato.
     *
     * @param dati DTO di partenza
     * @param attivo nuovo stato logico
     * @param statoContratto nuovo stato contrattuale
     * @param statoUtente nuovo stato utente
     * @return DTO aggiornato
     */
    private static DatiPersonalTrainer copiaConStato(
            DatiPersonalTrainer dati,
            boolean attivo,
            String statoContratto,
            String statoUtente) {

        return new DatiPersonalTrainer(
                dati.getIdTrainer(),
                dati.getCodiceFiscale(),
                dati.getNome(),
                dati.getCognome(),
                dati.getEmail(),
                dati.getPasswordHash(),
                statoUtente,
                dati.getSpecializzazione(),
                dati.getTipoContratto(),
                statoContratto,
                attivo,
                dati.getTipoRetribuzione(),
                dati.getStipendioMensile(),
                dati.getCompensoPerLezione(),
                dati.getIdDirettore()
        );
    }

    /**
     * Copia un DTO assegnando un identificativo.
     *
     * @param dati DTO di partenza
     * @param idTrainer identificativo da assegnare
     * @return DTO con identificativo valorizzato
     */
    private static DatiPersonalTrainer copiaConId(
            DatiPersonalTrainer dati,
            Integer idTrainer) {

        return new DatiPersonalTrainer(
                idTrainer,
                dati.getCodiceFiscale(),
                dati.getNome(),
                dati.getCognome(),
                dati.getEmail(),
                dati.getPasswordHash(),
                dati.getStatoUtente(),
                dati.getSpecializzazione(),
                dati.getTipoContratto(),
                dati.getStatoContratto(),
                dati.isAttivo(),
                dati.getTipoRetribuzione(),
                dati.getStipendioMensile(),
                dati.getCompensoPerLezione(),
                dati.getIdDirettore()
        );
    }

    /**
     * DAO fake che salva i DTO dei Personal Trainer in memoria.
     */
    private static class FakePersonalTrainerDAO implements PersonalTrainerDAO {

        private final Map<Integer, DatiPersonalTrainer> trainers = new LinkedHashMap<>();
        private int prossimoId = 1;
        private int numeroSalvataggi;
        private int numeroEliminazioni;

        @Override
        public void salva(DatiPersonalTrainer datiTrainer) {
            DatiPersonalTrainer datiDaSalvare = datiTrainer;

            if (datiTrainer.getIdTrainer() == null) {
                datiDaSalvare = copiaConId(datiTrainer, prossimoId);
                prossimoId++;
            } else if (datiTrainer.getIdTrainer() >= prossimoId) {
                prossimoId = datiTrainer.getIdTrainer() + 1;
            }

            trainers.put(datiDaSalvare.getIdTrainer(), datiDaSalvare);
            numeroSalvataggi++;
        }

        @Override
        public DatiPersonalTrainer trovaPerId(Integer idTrainer) {
            return trainers.get(idTrainer);
        }

        @Override
        public DatiPersonalTrainer trovaPerEmail(String email) {
            if (email == null) {
                return null;
            }

            for (DatiPersonalTrainer datiTrainer : trainers.values()) {
                if (datiTrainer.getEmail() != null
                        && datiTrainer.getEmail().equalsIgnoreCase(email.trim())) {
                    return datiTrainer;
                }
            }

            return null;
        }

        @Override
        public void disattiva(Integer idTrainer) {
            DatiPersonalTrainer datiTrainer = trainers.get(idTrainer);

            if (datiTrainer != null) {
                trainers.put(
                        idTrainer,
                        copiaConStato(datiTrainer, false, "LICENZIATO", "Inattivo")
                );
                numeroEliminazioni++;
            }
        }

        @Override
        public void aggiorna(DatiPersonalTrainer datiTrainer) {
            trainers.put(datiTrainer.getIdTrainer(), datiTrainer);
        }

        @Override
        public List<DatiPersonalTrainer> trovaTutti() {
            return new ArrayList<>(trainers.values());
        }
    }

    /**
     * DAO fake che restituisce un Direttore valido.
     */
    private static class FakeDirettoreDAO implements DirettoreDAO {

        @Override
        public Integer trovaIdDirettoreDisponibile() {
            return 99;
        }
    }

    /**
     * Servizio fake che simula controllo e swap dei corsi.
     */
    private static class FakeServizioSwapCorsi implements ServizioSwapCorsi {

        private boolean haCorsiAttiviOFuturi;
        private int numeroSwapEseguiti;
        private String idTrainerDaSostituireRicevuto;
        private String idTrainerSostitutoRicevuto;

        @Override
        public boolean haCorsiAttiviOFuturi(String idTrainer) {
            return haCorsiAttiviOFuturi;
        }

        @Override
        public boolean haCorsiImminenti(String idTrainer) {
            return false;
        }

        @Override
        public int sostituisciTrainerNeiCorsi(
                String idTrainerDaSostituire,
                String idTrainerSostituto)
                throws SostitutoNonValidoException {

            numeroSwapEseguiti++;
            idTrainerDaSostituireRicevuto = idTrainerDaSostituire;
            idTrainerSostitutoRicevuto = idTrainerSostituto;
            return 1;
        }
    }

    /**
     * Servizio fake che restituisce un totale retributivo controllato dal test.
     */
    private static class FakeServizioRetribuzioni implements ServizioRetribuzioni {

        private double totaleDaRestituire;
        private int numeroChiamate;

        @Override
        public double calcolaTotaleRetribuzioniMensili() {
            numeroChiamate++;
            return totaleDaRestituire;
        }
    }
}