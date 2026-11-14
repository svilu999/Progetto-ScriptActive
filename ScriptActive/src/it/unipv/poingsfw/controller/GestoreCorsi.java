package it.unipv.poingsfw.controller;

import it.unipv.poingsfw.dao.CorsoDAO;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.domain.StatoCorso;
import it.unipv.poingsfw.exceptions.CorsoNonTrovatoException;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.SovrapposizioneOrarioException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.service.ServizioSwapCorsi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Controller dello Use Case UC3 (organizzazione del palinsesto corsi).
 * <p>
 * Riceve il {@link CorsoDAO} dal costruttore (<i>Dependency Injection</i>): conosce
 * solo l'interfaccia di persistenza e mai l'implementazione MySQL. L'istanza è creata
 * una sola volta nel Composition Root, quindi non serve un Singleton.
 * </p>
 */
public class GestoreCorsi implements ServizioSwapCorsi {

    private static final Logger LOG = Logger.getLogger(GestoreCorsi.class.getName());

    // --- DIPENDENZE (DEPENDENCY INVERSION) ---
    private final CorsoDAO corsoDAO;

    public GestoreCorsi(CorsoDAO corsoDAO) {
        this.corsoDAO = Objects.requireNonNull(corsoDAO, "corsoDAO non può essere null");
    }

    // =========================================================
    // UC3 - organizzazione del palinsesto corsi
    // =========================================================

    public synchronized List<Corso> getPalinsestoCorsi() {
        // Il controller delega al DAO l'estrazione filtrata del palinsesto
        return corsoDAO.getPalinsesto();
    }
    
    public synchronized void organizzaNuovoCorso(String nome, LocalDateTime orario, int capienza, String idPT)
            throws TrainerNonValidoException, SovrapposizioneOrarioException {

        LOG.fine(() -> "Richiesta inserimento corso: " + nome + " in data " + orario);

        if (!convalidaTrainer(idPT)) {
            throw new TrainerNonValidoException(
                    "Errore: Il Personal Trainer con ID " + idPT + " non esiste o non è attivo.");
        }

        if (controllaSovrapposizioni(idPT, orario)) {
            throw new SovrapposizioneOrarioException(
                    "Errore: Il Trainer " + idPT + " è già occupato in questa fascia oraria.");
        }

        /*
         * Un corso nuovo nasce senza identificativo: lo assegna il livello di persistenza
         * al salvataggio. Generarlo qui rischierebbe di farlo coincidere con quello di un
         * corso già esistente, che verrebbe sovrascritto.
         */

        PersonalTrainer ptAssegnato = new PersonalTrainer("NomePT", "CognomePT", "trainer@scriptactive.it", idPT);
        Corso nuovoCorso = new Corso(null, nome, orario, capienza, ptAssegnato);

        try {
            // Proviamo a salvare il corso sul database
            corsoDAO.insert(nuovoCorso);
            LOG.info("Nuovo corso registrato con ID: " + nuovoCorso.getIdCorso());
            
        } catch (RuntimeException ex) {
            /* * EXCEPTION TRANSLATION:
             * Se il DB rifiuta l'inserimento perché la Foreign Key fallisce (es. il trainer non esiste),
             * catturiamo l'errore SQL e lanciamo la nostra eccezione di dominio personalizzata.
             */
            if (ex.getCause() != null && ex.getCause().getMessage().contains("foreign key constraint fails")) {
                throw new TrainerNonValidoException(
                        "OPERAZIONE BLOCCATA: Il Personal Trainer con ID " + idPT + " non esiste nel sistema! Inserire un ID valido."
                );
            }
            
            // Se è un errore diverso (es. server spento), lo rilanciamo normalmente
            throw ex; 
        }
    }

    public synchronized void annullaCorso(String idCorso) throws CorsoNonTrovatoException {

        Corso corso = corsoDAO.findById(idCorso);
        if (corso == null) {
            throw new CorsoNonTrovatoException("Errore: Corso con ID " + idCorso + " inesistente.");
        }

        corso.setStato(StatoCorso.CANCELLATO);
        corsoDAO.insert(corso);
        LOG.info("Corso " + idCorso + " annullato.");
    }

    public synchronized List<Corso> getElencoCorsi() {
        return corsoDAO.findAll();
    }

    // =========================================================
    //Integrazione con Gestione Contratti del Personale
    // =========================================================

    @Override
    public synchronized boolean haCorsiAttiviOFuturi(String idTrainer) {
        LocalDateTime adesso = LocalDateTime.now();

        for (Corso corso : corsoDAO.findAll()) {
            if (corsoAssegnatoAlTrainer(corso, idTrainer)
                    && corso.getStato() == StatoCorso.ATTIVO
                    && !corso.getDataOra().isBefore(adesso)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public synchronized boolean haCorsiImminenti(String idTrainer) {
        LocalDateTime adesso = LocalDateTime.now();
        LocalDateTime limite = adesso.plusHours(24);

        for (Corso corso : corsoDAO.findAll()) {
            if (corsoAssegnatoAlTrainer(corso, idTrainer)
                    && corso.getStato() == StatoCorso.ATTIVO
                    && !corso.getDataOra().isBefore(adesso)
                    && !corso.getDataOra().isAfter(limite)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public synchronized int sostituisciTrainerNeiCorsi(String idTrainerDaSostituire, String idTrainerSostituto)
            throws SostitutoNonValidoException {

        if (idTrainerDaSostituire == null || idTrainerDaSostituire.trim().isEmpty()
                || idTrainerSostituto == null || idTrainerSostituto.trim().isEmpty()) {
            throw new SostitutoNonValidoException("ID del trainer da sostituire o del sostituto non valido.");
        }

        if (idTrainerDaSostituire.equals(idTrainerSostituto)) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto non può coincidere con il PT da licenziare.");
        }

        PersonalTrainer sostituto = new PersonalTrainer(
                "PT", "Sostituto", "sostituto@scriptactive.it", idTrainerSostituto);

        return sostituisciPtNeiCorsi(idTrainerDaSostituire, sostituto);
    }

    public synchronized int sostituisciPtNeiCorsi(PersonalTrainer ptDaLicenziare, PersonalTrainer ptSostituto)
            throws SostitutoNonValidoException {

        if (ptDaLicenziare == null || ptSostituto == null) {
            throw new SostitutoNonValidoException("PT da licenziare o PT sostituto non valido.");
        }

        return sostituisciPtNeiCorsi(ptDaLicenziare.getIdTrainer(), ptSostituto);
    }

    private int sostituisciPtNeiCorsi(String idTrainerDaSostituire, PersonalTrainer ptSostituto)
            throws SostitutoNonValidoException {

        int corsiAggiornati = 0;
        LocalDateTime adesso = LocalDateTime.now();

        for (Corso corso : corsoDAO.findAll()) {
            boolean corsoDaRiassegnare = corsoAssegnatoAlTrainer(corso, idTrainerDaSostituire)
                    && corso.getStato() == StatoCorso.ATTIVO
                    && !corso.getDataOra().isBefore(adesso);

            if (corsoDaRiassegnare) {
                if (controllaSovrapposizioni(ptSostituto.getIdTrainer(), corso.getDataOra())) {
                    throw new SostitutoNonValidoException(
                            "OPERAZIONE ANNULLATA: il sostituto " + ptSostituto.getIdTrainer()
                                    + " ha già un corso nello stesso orario del corso " + corso.getIdCorso() + ".");
                }

                corso.setTrainerAssegnato(ptSostituto);
                corsoDAO.insert(corso);
                corsiAggiornati++;
            }
        }

        return corsiAggiornati;
    }



    // =========================================================
    // Validazioni interne
    // =========================================================

    private boolean convalidaTrainer(String idPT) {
        return idPT != null && !idPT.trim().isEmpty() && !idPT.toUpperCase().contains("ERR");
    }

 
    private boolean controllaSovrapposizioni(String idPT, LocalDateTime dataOra) {
        for (Corso corso : corsoDAO.findAll()) {
            if (corsoAssegnatoAlTrainer(corso, idPT)
                    && corso.getDataOra().isEqual(dataOra)
                    && corso.getStato() == StatoCorso.ATTIVO) {
                return true;
            }
        }
        return false;
    }

    private boolean corsoAssegnatoAlTrainer(Corso corso, String idTrainerInput) {
        // Prevenzione NullPointerException
        if (corso == null || corso.getTrainerAssegnato() == null 
                || corso.getTrainerAssegnato().getIdTrainer() == null || idTrainerInput == null) {
            return false;
        }
        
        // Estraiamo solo i numeri per bypassare il problema del prefisso "PT-" vs "10" del DB
        String idDalDb = corso.getTrainerAssegnato().getIdTrainer().replaceAll("[^0-9]", "");
        String idDallInput = idTrainerInput.replaceAll("[^0-9]", "");
        
        return idDalDb.equals(idDallInput);
    }
}

