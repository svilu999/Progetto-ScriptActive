package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.dao.DirettoreDAO;
import it.unipv.poingsfw.dao.PersonalTrainerDAO;
import it.unipv.poingsfw.dto.DatiPersonalTrainer;
import it.unipv.poingsfw.dto.DatiVisualizzazioneTrainer;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.service.GestoreContrattiPersonale;
import it.unipv.poingsfw.service.ServizioRetribuzioni;
import it.unipv.poingsfw.service.ServizioSwapCorsi;

/**
 * Test unitari del Service applicativo per la gestione
 * dei contratti del personale.
 *
 * I test utilizzano implementazioni fake delle dipendenze
 * e non accedono al database reale.
 */
class GestoreContrattiPersonaleTest {

    private PersonalTrainerDAOFake trainerDAO;
    private DirettoreDAOFake direttoreDAO;
    private ServizioSwapCorsiFake servizioSwap;
    private ServizioRetribuzioniFake servizioRetribuzioni;

    private GestoreContrattiPersonale servizioContratti;

    /**
     * Prepara una nuova configurazione isolata prima di ogni test.
     */
    @BeforeEach
    void setUp() {
        trainerDAO =
                new PersonalTrainerDAOFake();

        direttoreDAO =
                new DirettoreDAOFake();

        servizioSwap =
                new ServizioSwapCorsiFake();

        servizioRetribuzioni =
                new ServizioRetribuzioniFake();

        servizioContratti =
                new GestoreContrattiPersonale(
                        trainerDAO,
                        direttoreDAO,
                        servizioSwap,
                        servizioRetribuzioni
                );
    }

    /**
     * Verifica l'assunzione corretta di un Personal Trainer
     * con retribuzione fissa.
     */
    @Test
    void assumiPersonalTrainerConDatiValidiSalvaIlTrainer()
            throws Exception {

        assertDoesNotThrow(() ->
                servizioContratti.assumiPersonalTrainer(
                        "Mario",
                        "Rossi",
                        "mario.rossi@test.it",
                        "Pilates",
                        "FISSA_MENSILE",
                        1500.00
                )
        );

        DatiPersonalTrainer salvato =
                trainerDAO.getTrainerSalvato();

        assertNotNull(salvato);

        assertEquals(
                "Mario",
                salvato.getNome()
        );

        assertEquals(
                "Rossi",
                salvato.getCognome()
        );

        assertEquals(
                "mario.rossi@test.it",
                salvato.getEmail()
        );

        assertEquals(
                "Pilates",
                salvato.getSpecializzazione()
        );

        assertEquals(
                "FISSA_MENSILE",
                salvato.getTipoRetribuzione()
        );

        assertEquals(
                1500.00,
                salvato.getStipendioMensile(),
                0.001
        );

        assertNull(
                salvato.getCompensoPerLezione()
        );

        assertEquals(
                Integer.valueOf(1),
                salvato.getIdDirettore()
        );

        assertTrue(
                salvato.isAttivo()
        );

        assertEquals(
                "ATTIVO",
                salvato.getStatoContratto()
        );
    }

    /**
     * Verifica l'assunzione corretta con retribuzione a lezione.
     */
    @Test
    void assumiPersonalTrainerALezioneSalvaIlCompenso()
            throws Exception {

        servizioContratti.assumiPersonalTrainer(
                "Luca",
                "Bianchi",
                "luca.bianchi@test.it",
                "Zumba",
                "A_LEZIONE",
                35.00
        );

        DatiPersonalTrainer salvato =
                trainerDAO.getTrainerSalvato();

        assertNotNull(salvato);

        assertEquals(
                "A_LEZIONE",
                salvato.getTipoRetribuzione()
        );

        assertEquals(
                0.00,
                salvato.getStipendioMensile(),
                0.001
        );

        assertEquals(
                35.00,
                salvato.getCompensoPerLezione(),
                0.001
        );
    }

    /**
     * Verifica che non sia possibile assumere un trainer
     * con un'email già associata a un altro trainer.
     */
    @Test
    void assumiPersonalTrainerConEmailDuplicataGeneraEccezione() {
        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        10,
                        "Mario",
                        "Rossi",
                        "duplicata@test.it",
                        "Pilates",
                        true,
                        "ATTIVO"
                )
        );

        assertThrows(
                TrainerGiaAssuntoException.class,
                () ->
                        servizioContratti
                                .assumiPersonalTrainer(
                                        "Luigi",
                                        "Verdi",
                                        "duplicata@test.it",
                                        "Pilates",
                                        "FISSA_MENSILE",
                                        1400.00
                                )
        );

        assertNull(
                trainerDAO.getTrainerSalvato()
        );
    }

    /**
     * Verifica che un'email formalmente non valida venga rifiutata.
     */
    @Test
    void assumiPersonalTrainerConEmailNonValidaGeneraEccezione() {
        assertThrows(
                TrainerNonValidoException.class,
                () ->
                        servizioContratti
                                .assumiPersonalTrainer(
                                        "Mario",
                                        "Rossi",
                                        "email-non-valida",
                                        "Pilates",
                                        "FISSA_MENSILE",
                                        1500.00
                                )
        );

        assertNull(
                trainerDAO.getTrainerSalvato()
        );
    }

    /**
     * Verifica il licenziamento senza sostituto quando
     * il trainer non possiede corsi attivi o futuri.
     */
    @Test
    void licenziaSenzaSostitutoDisattivaIlTrainer()
            throws Exception {

        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        20,
                        "Mario",
                        "Rossi",
                        "mario@test.it",
                        "Pilates",
                        true,
                        "ATTIVO"
                )
        );

        servizioSwap.setCorsiAttiviOFuturi(
                false
        );

        assertDoesNotThrow(() ->
                servizioContratti
                        .licenziaPersonalTrainerSenzaSostituto(
                                "20"
                        )
        );

        assertEquals(
                Integer.valueOf(20),
                trainerDAO.getIdTrainerDisattivato()
        );
    }

    /**
     * Verifica che il licenziamento senza sostituto venga
     * bloccato quando il trainer possiede corsi attivi o futuri.
     */
    @Test
    void licenziaSenzaSostitutoConCorsiAttiviGeneraEccezione() {
        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        21,
                        "Mario",
                        "Rossi",
                        "mario@test.it",
                        "Pilates",
                        true,
                        "ATTIVO"
                )
        );

        servizioSwap.setCorsiAttiviOFuturi(
                true
        );

        assertThrows(
                TrainerNonLicenziabileException.class,
                () ->
                        servizioContratti
                                .licenziaPersonalTrainerSenzaSostituto(
                                        "21"
                                )
        );

        assertNull(
                trainerDAO.getIdTrainerDisattivato()
        );
    }

    /**
     * Verifica il licenziamento con un sostituto compatibile.
     */
    @Test
    void licenziaConSostitutoCompatibileEsegueLoSwap()
            throws Exception {

        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        30,
                        "Mario",
                        "Rossi",
                        "mario@test.it",
                        "Pilates",
                        true,
                        "ATTIVO"
                )
        );

        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        31,
                        "Luca",
                        "Bianchi",
                        "luca@test.it",
                        "Pilates",
                        true,
                        "ATTIVO"
                )
        );

        assertDoesNotThrow(() ->
                servizioContratti
                        .licenziaPersonalTrainerConSostituto(
                                "30",
                                "31"
                        )
        );

        assertTrue(
                servizioSwap.isSwapEseguito()
        );

        assertEquals(
                "30",
                servizioSwap.getIdTrainerDaSostituire()
        );

        assertEquals(
                "31",
                servizioSwap.getIdTrainerSostituto()
        );

        /*
         * Nel flusso con sostituto il trainerDAO non deve essere
         * richiamato separatamente, perché la disattivazione viene
         * eseguita atomicamente dallo SwapCorsiDAO.
         */
        assertNull(
                trainerDAO.getIdTrainerDisattivato()
        );
    }

    /**
     * Verifica che un trainer con specializzazione differente
     * non possa essere usato come sostituto.
     */
    @Test
    void licenziaConSostitutoIncompatibileGeneraEccezione() {
        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        40,
                        "Mario",
                        "Rossi",
                        "mario@test.it",
                        "Pilates",
                        true,
                        "ATTIVO"
                )
        );

        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        41,
                        "Luca",
                        "Bianchi",
                        "luca@test.it",
                        "Sala pesi",
                        true,
                        "ATTIVO"
                )
        );

        assertThrows(
                SostitutoNonValidoException.class,
                () ->
                        servizioContratti
                                .licenziaPersonalTrainerConSostituto(
                                        "40",
                                        "41"
                                )
        );

        assertFalse(
                servizioSwap.isSwapEseguito()
        );
    }

    /**
     * Verifica che nella lista dei sostituti compaiano soltanto
     * i trainer attivi e con la stessa specializzazione.
     */
    @Test
    void getSostitutiCompatibiliFiltraCorrettamente() throws Exception {
        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        50,
                        "Mario",
                        "Rossi",
                        "mario@test.it",
                        "Pilates",
                        true,
                        "ATTIVO"
                )
        );

        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        51,
                        "Luca",
                        "Bianchi",
                        "luca@test.it",
                        "Pilates",
                        true,
                        "ATTIVO"
                )
        );

        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        52,
                        "Anna",
                        "Verdi",
                        "anna@test.it",
                        "Pilates",
                        false,
                        "LICENZIATO"
                )
        );

        trainerDAO.aggiungiTrainer(
                creaTrainer(
                        53,
                        "Paolo",
                        "Neri",
                        "paolo@test.it",
                        "Zumba",
                        true,
                        "ATTIVO"
                )
        );

        List<DatiVisualizzazioneTrainer> sostituti =
                servizioContratti
                        .getSostitutiCompatibili(
                                "50"
                        );

        assertEquals(
                1,
                sostituti.size()
        );

        assertEquals(
                "51",
                sostituti.get(0).getIdTrainer()
        );
    }

    /**
     * Verifica che il calcolo delle retribuzioni venga
     * delegato al Service specializzato.
     */
    @Test
    void calcolaRetribuzioniDelegaAlServizioRetribuzioni() {
        servizioRetribuzioni.setTotale(
                4250.50
        );

        double risultato =
                servizioContratti
                        .calcolaTotaleRetribuzioniMensili();

        assertEquals(
                4250.50,
                risultato,
                0.001
        );

        assertTrue(
                servizioRetribuzioni.isCalcoloRichiesto()
        );
    }

    /**
     * Costruisce un DTO persistente utilizzabile nei test.
     */
    private DatiPersonalTrainer creaTrainer(
            int idTrainer,
            String nome,
            String cognome,
            String email,
            String specializzazione,
            boolean attivo,
            String statoContratto) {

        return new DatiPersonalTrainer(
                idTrainer,
                "PT00000000000001",
                nome,
                cognome,
                email,
                "passwordHash",
                attivo ? "Attivo" : "Inattivo",
                specializzazione,
                "Fisso",
                statoContratto,
                attivo,
                "FISSA_MENSILE",
                1500.00,
                null,
                1
        );
    }

    /**
     * Implementazione fake del DAO dei Personal Trainer.
     */
    private static class PersonalTrainerDAOFake
            implements PersonalTrainerDAO {

        private final List<DatiPersonalTrainer> trainer =
                new ArrayList<>();

        private DatiPersonalTrainer trainerSalvato;
        private Integer idTrainerDisattivato;

        @Override
        public void salva(
                DatiPersonalTrainer datiTrainer) {

            trainerSalvato =
                    datiTrainer;
        }

        @Override
        public DatiPersonalTrainer trovaPerId(
                Integer idTrainer) {

            for (DatiPersonalTrainer dati : trainer) {
                if (dati.getIdTrainer().equals(
                        idTrainer)) {

                    return dati;
                }
            }

            return null;
        }

        @Override
        public DatiPersonalTrainer trovaPerEmail(
                String email) {

            for (DatiPersonalTrainer dati : trainer) {
                if (dati.getEmail()
                        .equalsIgnoreCase(email)) {

                    return dati;
                }
            }

            return null;
        }

        @Override
        public void disattiva(
                Integer idTrainer) {

            idTrainerDisattivato =
                    idTrainer;
        }

        @Override
        public void aggiorna(
                DatiPersonalTrainer datiTrainer) {

            for (int indice = 0;
                    indice < trainer.size();
                    indice++) {

                if (trainer.get(indice)
                        .getIdTrainer()
                        .equals(
                                datiTrainer.getIdTrainer()
                        )) {

                    trainer.set(
                            indice,
                            datiTrainer
                    );

                    return;
                }
            }
        }

        @Override
        public List<DatiPersonalTrainer> trovaTutti() {
            return new ArrayList<>(
                    trainer
            );
        }

        void aggiungiTrainer(
                DatiPersonalTrainer datiTrainer) {

            trainer.add(
                    datiTrainer
            );
        }

        DatiPersonalTrainer getTrainerSalvato() {
            return trainerSalvato;
        }

        Integer getIdTrainerDisattivato() {
            return idTrainerDisattivato;
        }
    }

    /**
     * Implementazione fake del DAO del Direttore.
     */
    private static class DirettoreDAOFake
            implements DirettoreDAO {

        @Override
        public Integer trovaIdDirettoreDisponibile() {
            return 1;
        }
    }

    /**
     * Implementazione fake del Service di swap.
     */
    private static class ServizioSwapCorsiFake
            implements ServizioSwapCorsi {

        private boolean corsiAttiviOFuturi;
        private boolean swapEseguito;

        private String idTrainerDaSostituire;
        private String idTrainerSostituto;

        @Override
        public boolean haCorsiAttiviOFuturi(
                String idTrainer) {

            return corsiAttiviOFuturi;
        }

        @Override
        public boolean haCorsiImminenti(
                String idTrainer) {

            return false;
        }

        @Override
        public int sostituisciTrainerNeiCorsi(
                String idTrainerDaSostituire,
                String idTrainerSostituto)
                throws SostitutoNonValidoException {

            this.swapEseguito = true;

            this.idTrainerDaSostituire =
                    idTrainerDaSostituire;

            this.idTrainerSostituto =
                    idTrainerSostituto;

            return 1;
        }

        void setCorsiAttiviOFuturi(
                boolean corsiAttiviOFuturi) {

            this.corsiAttiviOFuturi =
                    corsiAttiviOFuturi;
        }

        boolean isSwapEseguito() {
            return swapEseguito;
        }

        String getIdTrainerDaSostituire() {
            return idTrainerDaSostituire;
        }

        String getIdTrainerSostituto() {
            return idTrainerSostituto;
        }
    }

    /**
     * Implementazione fake del Service delle retribuzioni.
     */
    private static class ServizioRetribuzioniFake
            implements ServizioRetribuzioni {

        private double totale;
        private boolean calcoloRichiesto;

        @Override
        public double calcolaTotaleRetribuzioniMensili() {
            calcoloRichiesto = true;
            return totale;
        }

        void setTotale(
                double totale) {

            this.totale =
                    totale;
        }

        boolean isCalcoloRichiesto() {
            return calcoloRichiesto;
        }
    }
}