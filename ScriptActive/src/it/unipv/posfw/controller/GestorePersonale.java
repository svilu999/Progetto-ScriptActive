package it.unipv.posfw.controller;

import it.unipv.posfw.dao.PersonalTrainerDAO;
import it.unipv.posfw.dao.PersonalTrainerDAOImplMySQL;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.exceptions.SostitutoNonValidoException;
import it.unipv.posfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.posfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.posfw.service.ServizioRetribuzioni;
import it.unipv.posfw.service.ServizioRetribuzioniMySQL;
import it.unipv.posfw.service.ServizioSwapCorsi;
import it.unipv.posfw.service.ServizioSwapCorsiMySQL;
import it.unipv.posfw.strategy.StrategiaRetribuzione;

import java.util.List;

/**
 * Controller GRASP del caso d'uso UC5 - Gestione dei Contratti del Personale.
 *
 * Responsabilità principali:
 * - assumere un nuovo Personal Trainer;
 * - impedire doppie registrazioni dello stesso PT;
 * - licenziare un PT tramite soft delete;
 * - bloccare il licenziamento se il PT ha corsi attivi/futuri e manca un sostituto;
 * - richiedere lo swap dei corsi tramite il servizio corsi;
 * - delegare il salvataggio dei dati al DAO MySQL.
 *
 * Questa versione è collegata al database MySQL.
 *
 * Il controller NON contiene query SQL.
 * Le query SQL stanno dentro:
 * - PersonalTrainerDAOImplMySQL
 * - ServizioSwapCorsiMySQL
 * - ServizioRetribuzioniMySQL
 *
 * In questo modo GestorePersonale resta responsabile solo della logica del caso d'uso.
 */
public class GestorePersonale {

    private static GestorePersonale istanza;

    private PersonalTrainerDAO trainerDAO;
    private ServizioSwapCorsi servizioSwapCorsi;
    private ServizioRetribuzioni servizioRetribuzioni;

    /**
     * Costruttore privato del Singleton.
     *
     * Questa versione usa direttamente il database MySQL.
     */
    private GestorePersonale() {
        this.trainerDAO = new PersonalTrainerDAOImplMySQL();
        this.servizioSwapCorsi = new ServizioSwapCorsiMySQL();
        this.servizioRetribuzioni = new ServizioRetribuzioniMySQL();
    }

    /**
     * Costruttore utile per test o integrazione.
     */
    public GestorePersonale(PersonalTrainerDAO trainerDAO, ServizioSwapCorsi servizioSwapCorsi) {
        this.trainerDAO = trainerDAO;
        this.servizioSwapCorsi = servizioSwapCorsi;
        this.servizioRetribuzioni = new ServizioRetribuzioniMySQL();
    }

    /**
     * Costruttore completo utile per test o futura integrazione.
     */
    public GestorePersonale(
            PersonalTrainerDAO trainerDAO,
            ServizioSwapCorsi servizioSwapCorsi,
            ServizioRetribuzioni servizioRetribuzioni) {

        this.trainerDAO = trainerDAO;
        this.servizioSwapCorsi = servizioSwapCorsi;
        this.servizioRetribuzioni = servizioRetribuzioni;
    }

    /**
     * Metodo Singleton.
     */
    public static GestorePersonale getInstance() {
        if (istanza == null) {
            istanza = new GestorePersonale();
        }
        return istanza;
    }

    /**
     * UC5 - Flusso di assunzione.
     *
     * Il Direttore inserisce i dati del nuovo Personal Trainer.
     *
     * A livello database, il DAO MySQL inserisce i dati nelle tabelle:
     * - utente
     * - personal_trainer
     * - contratto_personale
     */
    public void assumiPT(
            String nome,
            String cognome,
            String email,
            String idPT,
            String specializzazione,
            StrategiaRetribuzione contratto) throws TrainerGiaAssuntoException {

        if (trainerDAO.trovaPerId(idPT) != null) {
            throw new TrainerGiaAssuntoException(
                    "OPERAZIONE ANNULLATA: il PT con ID " + idPT + " è già registrato.");
        }

        PersonalTrainer nuovoTrainer = new PersonalTrainer(
                nome,
                cognome,
                email,
                idPT,
                specializzazione,
                contratto
        );

        trainerDAO.salva(nuovoTrainer);

        System.out.println("[UC5] Record creato e attivato per il PT: " + nuovoTrainer.getNomeCompleto());
    }

    /**
     * UC5 - Licenziamento senza sostituto.
     *
     * Consentito solo se il PT non ha corsi attivi o futuri.
     */
    public void licenziaPT(String idDaLicenziare)
            throws SostitutoNonValidoException, TrainerNonLicenziabileException {

        PersonalTrainer ptDaLicenziare = trainerDAO.trovaPerId(idDaLicenziare);

        if (ptDaLicenziare == null) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: Personal Trainer da licenziare non trovato.");
        }

        if (!ptDaLicenziare.isAttivo()) {
            throw new TrainerNonLicenziabileException(
                    "OPERAZIONE ANNULLATA: il Personal Trainer risulta già inattivo o licenziato.");
        }

        if (servizioSwapCorsi.haCorsiAttiviOFuturi(idDaLicenziare)) {
            throw new TrainerNonLicenziabileException(
                    "OPERAZIONE ANNULLATA: il PT ha corsi attivi o futuri. "
                            + "Indicare un sostituto e usare licenziaPT(idDaLicenziare, idSostituto).");
        }

        disattivaRecordPersonale(ptDaLicenziare);
    }

    /**
     * UC5 - Licenziamento con sostituzione.
     *
     * Flusso:
     * 1. recupera il PT da licenziare;
     * 2. recupera il PT sostituto;
     * 3. controlla che il sostituto esista, sia attivo e sia diverso;
     * 4. controlla che il sostituto abbia la stessa specializzazione;
     * 5. controlla eventuali corsi imminenti;
     * 6. chiede al servizio corsi di fare lo swap;
     * 7. solo dopo lo swap disattiva il PT licenziato.
     */
    public void licenziaPT(String idDaLicenziare, String idSostituto)
            throws SostitutoNonValidoException, TrainerNonLicenziabileException {

        PersonalTrainer ptDaLicenziare = trainerDAO.trovaPerId(idDaLicenziare);

        if (ptDaLicenziare == null) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: Personal Trainer da licenziare non trovato.");
        }

        if (!ptDaLicenziare.isAttivo()) {
            throw new TrainerNonLicenziabileException(
                    "OPERAZIONE ANNULLATA: il Personal Trainer risulta già inattivo o licenziato.");
        }

        PersonalTrainer ptSostituto = trainerDAO.trovaPerId(idSostituto);

        if (ptSostituto == null) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto indicato non esiste.");
        }

        if (!ptSostituto.isAttivo()) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto indicato non è attivo.");
        }

        if (idDaLicenziare.equals(idSostituto)) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto non può coincidere con il PT da licenziare.");
        }

        String specializzazioneDaLicenziare = ptDaLicenziare.getSpecializzazione();
        String specializzazioneSostituto = ptSostituto.getSpecializzazione();

        if (specializzazioneDaLicenziare == null
                || specializzazioneSostituto == null
                || !specializzazioneDaLicenziare.equalsIgnoreCase(specializzazioneSostituto)) {

            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto deve avere la stessa specializzazione del PT da licenziare."
            );
        }

        if (servizioSwapCorsi.haCorsiImminenti(idDaLicenziare)) {
            System.out.println("[UC5] Il PT ha corsi imminenti: lo swap viene eseguito prima della disattivazione.");
        }

        int corsiAggiornati = servizioSwapCorsi.sostituisciTrainerNeiCorsi(
                idDaLicenziare,
                idSostituto
        );

        System.out.println("[UC5] Swap completato. Corsi riassegnati: " + corsiAggiornati);

        disattivaRecordPersonale(ptDaLicenziare);
    }

    /**
     * Soft delete del Personal Trainer.
     *
     * Il PT non viene eliminato fisicamente dal database.
     */
    private void disattivaRecordPersonale(PersonalTrainer ptDaLicenziare) {
        ptDaLicenziare.setStatoContratto("LICENZIATO");
        ptDaLicenziare.setAttivo(false);

        trainerDAO.aggiorna(ptDaLicenziare);

        System.out.println("[UC5] Record di " + ptDaLicenziare.getNomeCompleto()
                + " disattivato con successo tramite soft delete.");
    }

    /**
     * Restituisce l'elenco dei Personal Trainer.
     */
    public List<PersonalTrainer> getElencoPersonalTrainer() {
        return trainerDAO.trovaTutti();
    }

    /**
     * Calcola il totale mensile delle retribuzioni dei PT attivi.
     */
    public double calcolaTotaleStipendiMensili() {
        double totale = servizioRetribuzioni.calcolaTotaleRetribuzioniMensili();

        System.out.println("[UC5] Totale retribuzioni mensili PT attivi: €" + totale);
        return totale;
    }
}