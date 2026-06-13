package it.unipv.posfw.controller;

import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.domain.StatoCorso;
import it.unipv.posfw.exceptions.CorsoNonTrovatoException;
import it.unipv.posfw.exceptions.SostitutoNonValidoException;
import it.unipv.posfw.exceptions.SovrapposizioneOrarioException;
import it.unipv.posfw.exceptions.TrainerNonValidoException;
import it.unipv.posfw.service.ServizioSwapCorsi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class GestoreCorsi implements ServizioSwapCorsi {

    // --- PATTERN SINGLETON ---
    private static GestoreCorsi istanza;

    // --- DIPENDENZE (DEPENDENCY INVERSION) ---
    private CorsoDAO corsoDAO;

 // Spostati nella sezione del costruttore privato
    private GestoreCorsi() {
        /*
         * Raccordo UC3/MySQL: Disattiviamo il mock in memoria
         * e colleghiamo l'intera applicazione al database reale.
         */
        this.corsoDAO = new it.unipv.posfw.dao.CorsoDAOMySQL();
    }

    public static synchronized GestoreCorsi getInstance() {
        if (istanza == null) {
            istanza = new GestoreCorsi();
        }
        return istanza;
    }

    // =========================================================
    // UC3 - organizzazione del palinsesto corsi
    // =========================================================

    // RISPETTARE IL PATTERN MVC:
    public synchronized List<Corso> getPalinsestoCorsi() {
        // Il controller delega al DAO MySQL l'estrazione filtrata del palinsesto
        return corsoDAO.getPalinsesto();
    }
    
    public synchronized void organizzaNuovoCorso(String nome, LocalDateTime orario, int capienza, String idPT)
            throws TrainerNonValidoException, SovrapposizioneOrarioException {

        System.out.println("[CONTROLLER] Richiesta inserimento corso: " + nome + " in data " + orario);

        if (!convalidaTrainer(idPT)) {
            throw new TrainerNonValidoException(
                    "Errore: Il Personal Trainer con ID " + idPT + " non esiste o non è attivo.");
        }

        if (controllaSovrapposizioni(idPT, orario)) {
            throw new SovrapposizioneOrarioException(
                    "Errore: Il Trainer " + idPT + " è già occupato in questa fascia oraria.");
        }

        String idCorsoUnivoco = "CRS-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        PersonalTrainer ptAssegnato = new PersonalTrainer("NomePT", "CognomePT", "trainer@scriptactive.it", idPT);
        Corso nuovoCorso = new Corso(idCorsoUnivoco, nome, orario, capienza, ptAssegnato);

        try {
            // Proviamo a salvare il corso sul database
            corsoDAO.insert(nuovoCorso);
            System.out.println("[CONTROLLER] Nuovo corso registrato con ID: " + idCorsoUnivoco);
            
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
        System.out.println("[CONTROLLER] Richiesta annullamento corso ID: " + idCorso);

        Corso corso = corsoDAO.findById(idCorso);
        if (corso == null) {
            throw new CorsoNonTrovatoException("Errore: Corso con ID " + idCorso + " inesistente.");
        }

        corso.setStato(StatoCorso.CANCELLATO);
        corsoDAO.insert(corso);
        System.out.println("[CONTROLLER] Corso " + idCorso + " annullato correttamente. Notifiche inviate.");
    }

    public synchronized List<Corso> getElencoCorsi() {
        return corsoDAO.findAll();
    }

    // =========================================================
    // UC5 - integrazione con Gestione Contratti del Personale
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

                /*
                 * Merge UC5:
                 * prima Lorenzo cancellava i corsi con setStato(CANCELLATO).
                 * Ora il corso resta ATTIVO e viene riassegnato al sostituto.
                 */
                corso.setTrainerAssegnato(ptSostituto);
                corsoDAO.insert(corso);
                corsiAggiornati++;
            }
        }

        return corsiAggiornati;
    }

    /**
     * Metodo mantenuto solo per compatibilità con eventuali chiamate vecchie.
     * Non cancella più i corsi: UC5 richiede sempre un sostituto per fare lo swap.
     */
    @Deprecated
    public synchronized void rimuoviPtDaCorsi(PersonalTrainer pt) {
        if (pt == null) {
            return;
        }

        System.out.println("[CONTROLLER-INTEGRAZIONE] rimuoviPtDaCorsi non cancella più i corsi. "
                + "Usare sostituisciPtNeiCorsi(ptDaLicenziare, ptSostituto) per lo swap UC5. PT: "
                + pt.getIdTrainer());
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
                    && corso.getDataOra().equals(dataOra)
                    && corso.getStato() == StatoCorso.ATTIVO) {
                return true;
            }
        }
        return false;
    }

    private boolean corsoAssegnatoAlTrainer(Corso corso, String idTrainer) {
        return corso != null
                && corso.getTrainerAssegnato() != null
                && corso.getTrainerAssegnato().getIdTrainer() != null
                && corso.getTrainerAssegnato().getIdTrainer().equals(idTrainer);
    }
}

/*
 * NOTE MERGE UC3/UC5
 *
 * - GestoreCorsi mantiene le responsabilità del caso d'uso corsi: creare,
 *   annullare e consultare il palinsesto.
 * - Per UC5 espone anche l'interfaccia ServizioSwapCorsi, così GestorePersonale
 *   può dipendere da un'astrazione e non dal controller concreto.
 * - Il vecchio comportamento rimuoviPtDaCorsi cancellava i corsi del PT
 *   licenziato. È stato neutralizzato perché non era coerente con UC5.
 * - La regola corretta è: PT licenziato -> corsi futuri ATTIVI riassegnati al
 *   sostituto -> solo dopo il PT viene disattivato tramite soft delete.
 */
