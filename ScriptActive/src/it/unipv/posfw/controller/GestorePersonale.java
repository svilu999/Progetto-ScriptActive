package it.unipv.posfw.controller;

import it.unipv.posfw.dao.PersonalTrainerDAO;
import it.unipv.posfw.database.PersonalTrainerDAOMySQL;
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
 * Il controller non contiene query SQL.
 * Le query SQL stanno dentro:
 * - PersonalTrainerDAOImplMySQL;
 * - ServizioSwapCorsiMySQL;
 * - ServizioRetribuzioniMySQL.
 */
public class GestorePersonale {

    private static GestorePersonale istanza;

    private PersonalTrainerDAO trainerDAO;
    private ServizioSwapCorsi servizioSwapCorsi;
    private ServizioRetribuzioni servizioRetribuzioni;

    /**
     * Costruttore privato del Singleton.
     */
    private GestorePersonale() {
        this.trainerDAO = new PersonalTrainerDAOMySQL();
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
     *Flusso di assunzione.
     *
     * Il Direttore inserisce i dati del nuovo Personal Trainer.
     */
    public void assumiPT(
            String nome,
            String cognome,
            String email,
            String idPT,
            String specializzazione,
            StrategiaRetribuzione contratto) throws TrainerGiaAssuntoException {

        String idTecnico = normalizzaIdAssunzione(idPT);

        if (!"AUTO".equalsIgnoreCase(idTecnico) && trainerDAO.trovaPerId(idTecnico) != null) {
            throw new TrainerGiaAssuntoException(
                    "OPERAZIONE ANNULLATA: il PT con ID " + idTecnico + " è già registrato.");
        }

        PersonalTrainer nuovoTrainer = new PersonalTrainer(
                nome,
                cognome,
                email,
                idTecnico,
                specializzazione,
                contratto
        );

        trainerDAO.salva(nuovoTrainer);

        System.out.println(" Record creato e attivato per il PT: " + nuovoTrainer.getNomeCompleto());
    }

    /**
     *Licenziamento senza sostituto.
     *
     * Consentito solo se il PT non ha corsi attivi o futuri.
     */
    public void licenziaPT(String idDaLicenziare)
            throws SostitutoNonValidoException, TrainerNonLicenziabileException {

        idDaLicenziare = normalizzaIdObbligatorio(idDaLicenziare, "Personal Trainer da licenziare");

        PersonalTrainer ptDaLicenziare = recuperaTrainerDaLicenziare(idDaLicenziare);

        if (servizioSwapCorsi.haCorsiAttiviOFuturi(idDaLicenziare)) {
            throw new TrainerNonLicenziabileException(
                    "OPERAZIONE ANNULLATA: il PT ha corsi attivi o futuri. "
                            + "Indicare un sostituto compatibile prima del licenziamento.");
        }

        disattivaRecordPersonale(ptDaLicenziare);
    }

    /*
     * Flusso del licenziamento con sostituzione:
     * 1. valida gli ID ricevuti;
     * 2. recupera il PT da licenziare;
     * 3. recupera il PT sostituto;
     * 4. controlla che il sostituto sia diverso, attivo e compatibile;
     * 5. se il PT ha corsi futuri, esegue lo swap;
     * 6. solo dopo lo swap disattiva il PT licenziato.
     */
    public void licenziaPT(String idDaLicenziare, String idSostituto)
            throws SostitutoNonValidoException, TrainerNonLicenziabileException {

        idDaLicenziare = normalizzaIdObbligatorio(idDaLicenziare, "Personal Trainer da licenziare");
        idSostituto = normalizzaIdObbligatorio(idSostituto, "Personal Trainer sostituto");

        if (idDaLicenziare.equalsIgnoreCase(idSostituto)) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto non può coincidere con il PT da licenziare.");
        }

        PersonalTrainer ptDaLicenziare = recuperaTrainerDaLicenziare(idDaLicenziare);
        PersonalTrainer ptSostituto = recuperaTrainerSostituto(idSostituto);

        verificaCompatibilitaSostituto(ptDaLicenziare, ptSostituto);

        boolean haCorsiAttiviOFuturi = servizioSwapCorsi.haCorsiAttiviOFuturi(idDaLicenziare);

        if (!haCorsiAttiviOFuturi) {
            System.out.println("Il PT non ha corsi futuri: nessuno swap necessario.");
            disattivaRecordPersonale(ptDaLicenziare);
            return;
        }

        if (servizioSwapCorsi.haCorsiImminenti(idDaLicenziare)) {
            System.out.println("Il PT ha corsi imminenti: lo swap viene eseguito prima della disattivazione.");
        }

        int corsiAggiornati = servizioSwapCorsi.sostituisciTrainerNeiCorsi(
                idDaLicenziare,
                idSostituto
        );

        if (corsiAggiornati <= 0) {
            throw new TrainerNonLicenziabileException(
                    "OPERAZIONE ANNULLATA: il PT risultava avere corsi futuri, "
                            + "ma nessun corso è stato riassegnato. Verificare il palinsesto.");
        }

        System.out.println("Swap completato. Corsi riassegnati: " + corsiAggiornati);

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

        System.out.println("Record di " + ptDaLicenziare.getNomeCompleto()
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

        System.out.println("Totale retribuzioni mensili PT attivi: €" + totale);
        return totale;
    }

    private String normalizzaIdAssunzione(String idPT) {
        if (idPT == null || idPT.trim().isEmpty()) {
            return "AUTO";
        }

        return idPT.trim();
    }

    private String normalizzaIdObbligatorio(String id, String nomeCampo) throws SostitutoNonValidoException {
        if (id == null || id.trim().isEmpty()) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: " + nomeCampo + " non indicato.");
        }

        return id.trim();
    }

    private PersonalTrainer recuperaTrainerDaLicenziare(String idDaLicenziare)
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

        return ptDaLicenziare;
    }

    private PersonalTrainer recuperaTrainerSostituto(String idSostituto)
            throws SostitutoNonValidoException {

        PersonalTrainer ptSostituto = trainerDAO.trovaPerId(idSostituto);

        if (ptSostituto == null) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto indicato non esiste.");
        }

        if (!ptSostituto.isAttivo()) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto indicato non è attivo.");
        }

        return ptSostituto;
    }

    private void verificaCompatibilitaSostituto(
            PersonalTrainer ptDaLicenziare,
            PersonalTrainer ptSostituto) throws SostitutoNonValidoException {

        String specializzazioneDaLicenziare = ptDaLicenziare.getSpecializzazione();
        String specializzazioneSostituto = ptSostituto.getSpecializzazione();

        if (specializzazioneDaLicenziare == null
                || specializzazioneSostituto == null
                || !specializzazioneDaLicenziare.trim().equalsIgnoreCase(specializzazioneSostituto.trim())) {

            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto deve avere la stessa specializzazione del PT da licenziare."
            );
        }
    }
}
