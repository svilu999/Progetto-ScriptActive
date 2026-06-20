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
 * Controller applicativo del caso d'uso UC5 - Gestione dei Contratti del Personale.
 *
 * La classe coordina le operazioni richieste dalla view e delega ai DAO e ai
 * servizi applicativi le attività di persistenza, controllo dei corsi e calcolo
 * delle retribuzioni.
 *
 * Responsabilità principali:
 * <ul>
 *   <li>assumere un nuovo Personal Trainer;</li>
 *   <li>impedire doppie registrazioni dello stesso PT;</li>
 *   <li>licenziare un PT tramite soft delete;</li>
 *   <li>bloccare il licenziamento se il PT ha corsi attivi o futuri e manca un sostituto;</li>
 *   <li>richiedere lo swap dei corsi tramite il servizio corsi;</li>
 *   <li>delegare il salvataggio dei dati al DAO MySQL.</li>
 * </ul>
 *
 * Il controller non contiene query SQL: l'accesso al database è incapsulato in
 * {@link PersonalTrainerDAOMySQL}, {@link ServizioSwapCorsiMySQL} e
 * {@link ServizioRetribuzioniMySQL}.
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
     *
     * @param trainerDAO DAO da usare per la persistenza dei Personal Trainer
     * @param servizioSwapCorsi servizio per il controllo e lo swap dei corsi
     */
    public GestorePersonale(PersonalTrainerDAO trainerDAO, ServizioSwapCorsi servizioSwapCorsi) {
        this.trainerDAO = trainerDAO;
        this.servizioSwapCorsi = servizioSwapCorsi;
        this.servizioRetribuzioni = new ServizioRetribuzioniMySQL();
    }

    /**
     * Costruttore completo utile per test o futura integrazione.
     *
     * @param trainerDAO DAO da usare per la persistenza dei Personal Trainer
     * @param servizioSwapCorsi servizio per il controllo e lo swap dei corsi
     * @param servizioRetribuzioni servizio per il calcolo delle retribuzioni
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
     * Restituisce l'istanza Singleton del gestore del personale.
     *
     * @return istanza unica di GestorePersonale
     */
    public static GestorePersonale getInstance() {
        if (istanza == null) {
            istanza = new GestorePersonale();
        }
        return istanza;
    }

    /**
     * Gestisce il flusso di assunzione di un nuovo Personal Trainer.
     *
     * Il metodo normalizza l'identificativo, controlla eventuali duplicati
     * e delega al DAO il salvataggio del nuovo trainer.
     *
     * @param nome nome del Personal Trainer
     * @param cognome cognome del Personal Trainer
     * @param email email del Personal Trainer
     * @param idPT identificativo del Personal Trainer, oppure AUTO per la generazione automatica
     * @param specializzazione specializzazione professionale del Personal Trainer
     * @param contratto strategia di retribuzione associata al Personal Trainer
     * @throws TrainerGiaAssuntoException se esiste già un trainer con lo stesso identificativo
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
     * Gestisce il licenziamento di un Personal Trainer senza sostituto.
     *
     * Il licenziamento senza sostituto è consentito solo se il trainer non ha
     * corsi attivi o futuri assegnati. In caso contrario il metodo interrompe
     * l'operazione lanciando un'eccezione applicativa.
     *
     * @param idDaLicenziare identificativo del Personal Trainer da licenziare
     * @throws SostitutoNonValidoException se l'identificativo non è valido o il trainer non esiste
     * @throws TrainerNonLicenziabileException se il trainer ha corsi attivi o futuri
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

    /**
     * Gestisce il licenziamento di un Personal Trainer con sostituzione.
     *
     * Il metodo valida gli identificativi ricevuti, recupera il trainer da
     * licenziare e il trainer sostituto, verifica che il sostituto sia diverso,
     * attivo e compatibile per specializzazione, esegue lo swap dei corsi attivi
     * o futuri e solo alla fine disattiva logicamente il trainer licenziato.
     *
     * @param idDaLicenziare identificativo del Personal Trainer da licenziare
     * @param idSostituto identificativo del Personal Trainer sostituto
     * @throws SostitutoNonValidoException se il sostituto non è valido o non è compatibile
     * @throws TrainerNonLicenziabileException se il trainer non può essere licenziato in sicurezza
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
     * Esegue il soft delete del Personal Trainer.
     *
     * Il trainer non viene eliminato fisicamente dal database: il suo stato
     * contrattuale viene impostato a LICENZIATO e il flag di attivazione viene
     * impostato a false.
     *
     * @param ptDaLicenziare Personal Trainer da disattivare logicamente
     */
    private void disattivaRecordPersonale(PersonalTrainer ptDaLicenziare) {
        ptDaLicenziare.setStatoContratto("LICENZIATO");
        ptDaLicenziare.setAttivo(false);

        trainerDAO.aggiorna(ptDaLicenziare);

        System.out.println("Record di " + ptDaLicenziare.getNomeCompleto()
                + " disattivato con successo tramite soft delete.");
    }

    /**
     * Restituisce l'elenco dei Personal Trainer presenti nel sistema.
     *
     * @return lista dei Personal Trainer recuperati dal DAO
     */
    public List<PersonalTrainer> getElencoPersonalTrainer() {
        return trainerDAO.trovaTutti();
    }

    /**
     * Calcola il totale mensile delle retribuzioni dei Personal Trainer attivi.
     *
     * @return totale mensile delle retribuzioni
     */
    public double calcolaTotaleStipendiMensili() {
        double totale = servizioRetribuzioni.calcolaTotaleRetribuzioniMensili();

        System.out.println("Totale retribuzioni mensili PT attivi: €" + totale);
        return totale;
    }

    /**
     * Normalizza l'identificativo inserito in fase di assunzione.
     *
     * Se l'ID non viene specificato, viene restituito il valore tecnico AUTO
     * per indicare al DAO che l'identificativo deve essere generato automaticamente
     * tramite l'inserimento dell'utente nel database.
     *
     * @param idPT identificativo inserito nella view
     * @return identificativo normalizzato oppure AUTO
     */
    private String normalizzaIdAssunzione(String idPT) {
        if (idPT == null || idPT.trim().isEmpty()) {
            return "AUTO";
        }

        return idPT.trim();
    }

    /**
     * Normalizza un identificativo obbligatorio e verifica che sia presente.
     *
     * @param id identificativo da controllare
     * @param nomeCampo nome logico del campo usato nel messaggio di errore
     * @return identificativo ripulito dagli spazi iniziali e finali
     * @throws SostitutoNonValidoException se l'identificativo è nullo o vuoto
     */
    private String normalizzaIdObbligatorio(String id, String nomeCampo) throws SostitutoNonValidoException {
        if (id == null || id.trim().isEmpty()) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: " + nomeCampo + " non indicato.");
        }

        return id.trim();
    }

    /**
     * Recupera dal DAO il Personal Trainer da licenziare e ne verifica lo stato.
     *
     * @param idDaLicenziare identificativo del trainer da licenziare
     * @return Personal Trainer recuperato dal sistema
     * @throws SostitutoNonValidoException se il trainer non esiste
     * @throws TrainerNonLicenziabileException se il trainer è già inattivo o licenziato
     */
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

    /**
     * Recupera dal DAO il Personal Trainer sostituto e verifica che sia attivo.
     *
     * @param idSostituto identificativo del trainer sostituto
     * @return Personal Trainer sostituto recuperato dal sistema
     * @throws SostitutoNonValidoException se il sostituto non esiste o non è attivo
     */
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

    /**
     * Verifica la compatibilità tra il trainer da licenziare e il sostituto.
     *
     * Nel caso d'uso UC5 il sostituto è considerato valido solo se possiede
     * la stessa specializzazione del Personal Trainer da licenziare.
     *
     * @param ptDaLicenziare trainer che deve essere licenziato
     * @param ptSostituto trainer candidato alla sostituzione
     * @throws SostitutoNonValidoException se le specializzazioni non coincidono
     */
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
