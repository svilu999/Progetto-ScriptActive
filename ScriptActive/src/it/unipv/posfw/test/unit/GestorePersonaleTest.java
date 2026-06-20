package it.unipv.posfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.posfw.controller.GestorePersonale;
import it.unipv.posfw.dao.PersonalTrainerDAO;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.exceptions.SostitutoNonValidoException;
import it.unipv.posfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.posfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.posfw.service.ServizioRetribuzioni;
import it.unipv.posfw.service.ServizioSwapCorsi;
import it.unipv.posfw.strategy.RetribuzioneFissa;

/**
 * Test JUnit per il controller GestorePersonale.
 *
 * La classe verifica i principali scenari del caso d'uso di gestione del
 * personale: assunzione, licenziamento, sostituzione e calcolo retribuzioni.
 * I test usano implementazioni interne di supporto, così non dipendono dal
 * database reale.
 */
public class GestorePersonaleTest {

    private FakePersonalTrainerDAO trainerDAO;
    private FakeServizioSwapCorsi servizioSwapCorsi;
    private FakeServizioRetribuzioni servizioRetribuzioni;
    private GestorePersonale gestorePersonale;

    /**
     * Inizializza controller e dipendenze di test prima di ogni scenario.
     */
    @BeforeEach
    public void setUp() {
        trainerDAO = new FakePersonalTrainerDAO();
        servizioSwapCorsi = new FakeServizioSwapCorsi();
        servizioRetribuzioni = new FakeServizioRetribuzioni();

        gestorePersonale = new GestorePersonale(
                trainerDAO,
                servizioSwapCorsi,
                servizioRetribuzioni
        );
    }

    /**
     * Verifica che un nuovo Personal Trainer venga assunto correttamente.
     *
     * @throws TrainerGiaAssuntoException se il trainer risulta già presente
     */
    @Test
    public void testAssumiPT_SuccessoConIdAutomatico() throws TrainerGiaAssuntoException {
        gestorePersonale.assumiPT(
                "Mario",
                "Rossi",
                "mario.rossi@test.it",
                "AUTO",
                "Sala pesi",
                new RetribuzioneFissa(1400.0)
        );

        assertEquals(1, trainerDAO.trainers.size());
        PersonalTrainer salvato = trainerDAO.trovaPerId("AUTO");

        assertNotNull(salvato);
        assertEquals("Mario", salvato.getNome());
        assertEquals("Rossi", salvato.getCognome());
        assertEquals("Sala pesi", salvato.getSpecializzazione());
        assertTrue(salvato.isAttivo());
        assertEquals("ATTIVO", salvato.getStatoContratto());
    }

    /**
     * Verifica che l'assunzione venga bloccata se l'ID è già presente.
     */
    @Test
    public void testAssumiPT_IdGiaPresente_LanciaEccezione() {
        trainerDAO.salva(creaTrainer("10", "Luca", "Bianchi", "Sala pesi", true));

        assertThrows(TrainerGiaAssuntoException.class, () -> gestorePersonale.assumiPT(
                "Mario",
                "Rossi",
                "mario.rossi@test.it",
                "10",
                "Sala pesi",
                new RetribuzioneFissa(1400.0)
        ));

        assertEquals(1, trainerDAO.trainers.size());
    }

    /**
     * Verifica il licenziamento senza sostituto quando non ci sono corsi attivi.
     *
     * @throws SostitutoNonValidoException se l'ID del trainer non è valido
     * @throws TrainerNonLicenziabileException se il trainer non può essere licenziato
     */
    @Test
    public void testLicenziaPT_SenzaCorsiAttivi_DisattivaTrainer()
            throws SostitutoNonValidoException, TrainerNonLicenziabileException {

        PersonalTrainer trainer = creaTrainer("10", "Luca", "Bianchi", "Sala pesi", true);
        trainerDAO.salva(trainer);
        servizioSwapCorsi.haCorsiAttiviOFuturi = false;

        gestorePersonale.licenziaPT("10");

        PersonalTrainer aggiornato = trainerDAO.trovaPerId("10");
        assertFalse(aggiornato.isAttivo());
        assertEquals("LICENZIATO", aggiornato.getStatoContratto());
        assertEquals(1, trainerDAO.numeroAggiornamenti);
    }

    /**
     * Verifica che il licenziamento senza sostituto venga bloccato se ci sono corsi attivi.
     */
    @Test
    public void testLicenziaPT_SenzaSostitutoMaConCorsiAttivi_LanciaEccezione() {
        trainerDAO.salva(creaTrainer("10", "Luca", "Bianchi", "Sala pesi", true));
        servizioSwapCorsi.haCorsiAttiviOFuturi = true;

        assertThrows(TrainerNonLicenziabileException.class, () -> gestorePersonale.licenziaPT("10"));

        assertTrue(trainerDAO.trovaPerId("10").isAttivo());
        assertEquals(0, trainerDAO.numeroAggiornamenti);
    }

    /**
     * Verifica il licenziamento con sostituto compatibile.
     *
     * @throws SostitutoNonValidoException se il sostituto non è valido
     * @throws TrainerNonLicenziabileException se il trainer non può essere licenziato
     */
    @Test
    public void testLicenziaPT_ConSostitutoCompatibile_EsegueSwapEDisattiva()
            throws SostitutoNonValidoException, TrainerNonLicenziabileException {

        trainerDAO.salva(creaTrainer("10", "Luca", "Bianchi", "Sala pesi", true));
        trainerDAO.salva(creaTrainer("11", "Giulia", "Neri", "Sala pesi", true));

        servizioSwapCorsi.haCorsiAttiviOFuturi = true;
        servizioSwapCorsi.numeroCorsiDaAggiornare = 2;

        gestorePersonale.licenziaPT("10", "11");

        assertEquals("10", servizioSwapCorsi.idTrainerDaSostituireRicevuto);
        assertEquals("11", servizioSwapCorsi.idTrainerSostitutoRicevuto);
        assertFalse(trainerDAO.trovaPerId("10").isAttivo());
        assertEquals("LICENZIATO", trainerDAO.trovaPerId("10").getStatoContratto());
        assertTrue(trainerDAO.trovaPerId("11").isAttivo());
    }

    /**
     * Verifica che il sostituto non possa coincidere con il trainer da licenziare.
     */
    @Test
    public void testLicenziaPT_ConStessoIdComeSostituto_LanciaEccezione() {
        trainerDAO.salva(creaTrainer("10", "Luca", "Bianchi", "Sala pesi", true));

        assertThrows(SostitutoNonValidoException.class, () -> gestorePersonale.licenziaPT("10", "10"));

        assertEquals(0, servizioSwapCorsi.numeroSwapEseguiti);
        assertTrue(trainerDAO.trovaPerId("10").isAttivo());
    }

    /**
     * Verifica che un sostituto con specializzazione diversa venga rifiutato.
     */
    @Test
    public void testLicenziaPT_SostitutoNonCompatibile_LanciaEccezione() {
        trainerDAO.salva(creaTrainer("10", "Luca", "Bianchi", "Sala pesi", true));
        trainerDAO.salva(creaTrainer("11", "Giulia", "Neri", "Pilates", true));

        assertThrows(SostitutoNonValidoException.class, () -> gestorePersonale.licenziaPT("10", "11"));

        assertEquals(0, servizioSwapCorsi.numeroSwapEseguiti);
        assertTrue(trainerDAO.trovaPerId("10").isAttivo());
    }

    /**
     * Verifica che il calcolo retribuzioni venga delegato al servizio dedicato.
     */
    @Test
    public void testCalcolaTotaleStipendiMensili_DelegaAlServizioRetribuzioni() {
        servizioRetribuzioni.totaleDaRestituire = 2750.0;

        double totale = gestorePersonale.calcolaTotaleStipendiMensili();

        assertEquals(2750.0, totale, 0.001);
        assertEquals(1, servizioRetribuzioni.numeroChiamate);
    }

    /**
     * Crea un Personal Trainer di supporto per i test.
     *
     * @param id identificativo del trainer
     * @param nome nome del trainer
     * @param cognome cognome del trainer
     * @param specializzazione specializzazione del trainer
     * @param attivo stato logico del trainer
     * @return trainer configurato per il test
     */
    private PersonalTrainer creaTrainer(
            String id,
            String nome,
            String cognome,
            String specializzazione,
            boolean attivo) {

        PersonalTrainer trainer = new PersonalTrainer(
                nome,
                cognome,
                nome.toLowerCase() + "." + cognome.toLowerCase() + "@test.it",
                id,
                specializzazione,
                new RetribuzioneFissa(1400.0)
        );

        trainer.setAttivo(attivo);
        trainer.setStatoContratto(attivo ? "ATTIVO" : "LICENZIATO");
        return trainer;
    }

    /**
     * DAO di supporto che salva i Personal Trainer in memoria.
     */
    private static class FakePersonalTrainerDAO implements PersonalTrainerDAO {
        private final Map<String, PersonalTrainer> trainers = new LinkedHashMap<>();
        private int numeroAggiornamenti;

        @Override
        public void salva(PersonalTrainer pt) {
            trainers.put(pt.getIdTrainer(), pt);
        }

        @Override
        public PersonalTrainer trovaPerId(String idPT) {
            return trainers.get(idPT);
        }

        @Override
        public void elimina(String idPT) {
            PersonalTrainer trainer = trainers.get(idPT);
            if (trainer != null) {
                trainer.setAttivo(false);
                trainer.setStatoContratto("LICENZIATO");
            }
        }

        @Override
        public void aggiorna(PersonalTrainer pt) {
            trainers.put(pt.getIdTrainer(), pt);
            numeroAggiornamenti++;
        }

        @Override
        public List<PersonalTrainer> trovaTutti() {
            return new ArrayList<>(trainers.values());
        }
    }

    /**
     * Servizio di supporto che simula controllo e swap dei corsi.
     */
    private static class FakeServizioSwapCorsi implements ServizioSwapCorsi {
        private boolean haCorsiAttiviOFuturi;
        private boolean haCorsiImminenti;
        private int numeroCorsiDaAggiornare;
        private int numeroSwapEseguiti;
        private String idTrainerDaSostituireRicevuto;
        private String idTrainerSostitutoRicevuto;

        @Override
        public boolean haCorsiAttiviOFuturi(String idTrainer) {
            return haCorsiAttiviOFuturi;
        }

        @Override
        public boolean haCorsiImminenti(String idTrainer) {
            return haCorsiImminenti;
        }

        @Override
        public int sostituisciTrainerNeiCorsi(String idTrainerDaSostituire, String idTrainerSostituto) {
            numeroSwapEseguiti++;
            idTrainerDaSostituireRicevuto = idTrainerDaSostituire;
            idTrainerSostitutoRicevuto = idTrainerSostituto;
            return numeroCorsiDaAggiornare;
        }
    }

    /**
     * Servizio di supporto che restituisce un totale retributivo controllato dal test.
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
