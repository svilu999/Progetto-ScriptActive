package it.unipv.poingsfw.service;

import it.unipv.poingsfw.dao.PersonalTrainerDAO;
import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.dto.DatiPersonalTrainer;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.strategy.StrategiaRetribuzione;
import it.unipv.poingsfw.strategy.StrategiaRetribuzioneFactory;
import it.unipv.poingsfw.dao.DirettoreDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gestore applicativo dei contratti del personale.
 *
 * Questa classe appartiene al Model logico del caso d'uso. Contiene la logica
 * applicativa relativa alla gestione contrattuale dei Personal Trainer e fa da
 * ponte tra gli oggetti di dominio e i DTO usati dal livello DAO.
 */
public class GestoreContrattiPersonale implements ServizioContrattiPersonale {

    private static final String STATO_CONTRATTO_ATTIVO = "ATTIVO";
    private static final String STATO_UTENTE_ATTIVO = "Attivo";
    private static final String PASSWORD_PREDEFINITA = "1234";

    private static final String TIPO_FISSA_MENSILE = "FISSA_MENSILE";
    private static final String TIPO_A_LEZIONE = "A_LEZIONE";

    private final PersonalTrainerDAO trainerDAO;
    private final DirettoreDAO direttoreDAO;
    private final ServizioSwapCorsi servizioSwapCorsi;
    private final ServizioRetribuzioni servizioRetribuzioni;

    /**
     * Crea il gestore dei contratti del personale ricevendo le dipendenze
     * dall'esterno.
     *
     * @param trainerDAO DAO usato per la persistenza dei Personal Trainer
     * @param servizioSwapCorsi servizio usato per gestire i corsi collegati ai trainer
     * @param servizioRetribuzioni servizio usato per calcolare le retribuzioni mensili
     */
    public GestoreContrattiPersonale(
            PersonalTrainerDAO trainerDAO,
            DirettoreDAO direttoreDAO,
            ServizioSwapCorsi servizioSwapCorsi,
            ServizioRetribuzioni servizioRetribuzioni) {

        this.trainerDAO = Objects.requireNonNull(trainerDAO, "trainerDAO non può essere null");
        this.direttoreDAO = Objects.requireNonNull(
                direttoreDAO,
                "direttoreDAO non può essere null"
        );
        this.servizioSwapCorsi = Objects.requireNonNull(
                servizioSwapCorsi,
                "servizioSwapCorsi non può essere null"
        );
        this.servizioRetribuzioni = Objects.requireNonNull(
                servizioRetribuzioni,
                "servizioRetribuzioni non può essere null"
        );
    }

    /**
     * Gestisce il flusso di assunzione di un nuovo Personal Trainer.
     *
     * Il metodo valida i dati ricevuti dal controller, controlla eventuali
     * duplicati, prepara il DTO persistente e delega il salvataggio al DAO.
     *
     * @param nome nome del Personal Trainer
     * @param cognome cognome del Personal Trainer
     * @param email email del Personal Trainer
     * @param idTrainer identificativo del Personal Trainer, oppure AUTO
     * @param specializzazione specializzazione professionale
     * @param tipoRetribuzione tipo di retribuzione scelto
     * @param importoRetribuzione importo associato alla retribuzione
     * @throws TrainerGiaAssuntoException se esiste già un trainer con stesso ID o email
     * @throws TrainerNonValidoException se i dati del trainer non sono validi
     */
    @Override
    public void assumiPersonalTrainer(
            String nome,
            String cognome,
            String email,
            String idTrainer,
            String specializzazione,
            String tipoRetribuzione,
            double importoRetribuzione)
            throws TrainerGiaAssuntoException, TrainerNonValidoException {

        String nomeNormalizzato = normalizzaCampoObbligatorio(nome, "nome");
        String cognomeNormalizzato = normalizzaCampoObbligatorio(cognome, "cognome");
        String emailNormalizzata = normalizzaEmailObbligatoria(email);
        String specializzazioneNormalizzata = normalizzaCampoObbligatorio(
                specializzazione,
                "specializzazione"
        );

        Integer idTrainerNumerico = normalizzaIdAssunzione(idTrainer);

        if (idTrainerNumerico != null && trainerDAO.trovaPerId(idTrainerNumerico) != null) {
            throw new TrainerGiaAssuntoException(
                    "OPERAZIONE ANNULLATA: il PT con ID "
                            + idTrainerNumerico
                            + " è già registrato."
            );
        }

        if (trainerDAO.trovaPerEmail(emailNormalizzata) != null) {
            throw new TrainerGiaAssuntoException(
                    "OPERAZIONE ANNULLATA: esiste già un Personal Trainer registrato con email "
                            + emailNormalizzata + "."
            );
        }

        StrategiaRetribuzioneFactory.crea(tipoRetribuzione, importoRetribuzione);

        DatiPersonalTrainer datiNuovoTrainer = creaDatiNuovoTrainer(
                idTrainerNumerico,
                nomeNormalizzato,
                cognomeNormalizzato,
                emailNormalizzata,
                specializzazioneNormalizzata,
                tipoRetribuzione,
                importoRetribuzione
        );

        trainerDAO.salva(datiNuovoTrainer);
    }

    /**
     * Restituisce l'elenco dei Personal Trainer presenti nel sistema.
     *
     * Il DAO restituisce DTO persistenti. Il Service li converte in oggetti
     * di dominio prima di restituirli al Controller.
     *
     * @return lista dei Personal Trainer presenti nel sistema
     */
    @Override
    public List<PersonalTrainer> getElencoPersonalTrainer() {
        return convertiListaDatiInPersonalTrainer(trainerDAO.trovaTutti());
    }

    /**
     * Restituisce i Personal Trainer compatibili come sostituti.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da sostituire
     * @return lista dei Personal Trainer compatibili
     * @throws TrainerNonValidoException se il trainer da licenziare non esiste o non è valido
     */
    @Override
    public List<PersonalTrainer> getSostitutiCompatibili(String idTrainerDaLicenziare)
            throws TrainerNonValidoException {

        Integer idDaLicenziare = normalizzaIdObbligatorio(
                idTrainerDaLicenziare,
                "identificativo del Personal Trainer da licenziare"
        );

        PersonalTrainer trainerDaLicenziare = convertiDatiInPersonalTrainer(
                trainerDAO.trovaPerId(idDaLicenziare)
        );

        if (trainerDaLicenziare == null) {
            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: il Personal Trainer da licenziare non esiste."
            );
        }

        List<PersonalTrainer> sostitutiCompatibili = new ArrayList<>();

        for (PersonalTrainer possibileSostituto : getElencoPersonalTrainer()) {
            if (isSostitutoCompatibile(trainerDaLicenziare, possibileSostituto)) {
                sostitutiCompatibili.add(possibileSostituto);
            }
        }

        return sostitutiCompatibili;
    }

    /**
     * Gestisce il licenziamento di un Personal Trainer senza sostituto.
     *
     * @param idTrainer identificativo del Personal Trainer da licenziare
     * @throws TrainerNonValidoException se il trainer non esiste o non è valido
     * @throws TrainerNonLicenziabileException se il trainer ha corsi attivi o futuri
     */
    @Override
    public void licenziaPersonalTrainerSenzaSostituto(String idTrainer)
            throws TrainerNonValidoException, TrainerNonLicenziabileException {

        Integer idTrainerNumerico = normalizzaIdObbligatorio(
                idTrainer,
                "identificativo del Personal Trainer da licenziare"
        );

        PersonalTrainer trainerDaLicenziare = convertiDatiInPersonalTrainer(
                trainerDAO.trovaPerId(idTrainerNumerico)
        );

        if (trainerDaLicenziare == null) {
            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: il Personal Trainer da licenziare non esiste."
            );
        }

        validaTrainerLicenziabile(trainerDaLicenziare);

        if (servizioSwapCorsi.haCorsiAttiviOFuturi(String.valueOf(idTrainerNumerico))) {
            throw new TrainerNonLicenziabileException(
                    "OPERAZIONE ANNULLATA: il Personal Trainer ha corsi attivi o futuri assegnati."
            );
        }

        trainerDAO.elimina(idTrainerNumerico);
    }

    /**
     * Gestisce il licenziamento di un Personal Trainer con sostituto.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da licenziare
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @throws TrainerNonValidoException se il trainer da licenziare non esiste o non è valido
     * @throws SostitutoNonValidoException se il sostituto non è valido o non è compatibile
     */
    @Override
    public void licenziaPersonalTrainerConSostituto(
            String idTrainerDaLicenziare,
            String idTrainerSostituto)
            throws TrainerNonValidoException, SostitutoNonValidoException {

        Integer idDaLicenziare = normalizzaIdObbligatorio(
                idTrainerDaLicenziare,
                "identificativo del Personal Trainer da licenziare"
        );

        Integer idSostituto = normalizzaIdObbligatorio(
                idTrainerSostituto,
                "identificativo del Personal Trainer sostituto"
        );

        PersonalTrainer trainerDaLicenziare = convertiDatiInPersonalTrainer(
                trainerDAO.trovaPerId(idDaLicenziare)
        );

        if (trainerDaLicenziare == null) {
            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: il Personal Trainer da licenziare non esiste."
            );
        }

        validaTrainerLicenziabile(trainerDaLicenziare);

        PersonalTrainer sostituto = convertiDatiInPersonalTrainer(
                trainerDAO.trovaPerId(idSostituto)
        );

        if (sostituto == null) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il Personal Trainer sostituto non esiste."
            );
        }

        if (!isSostitutoCompatibile(trainerDaLicenziare, sostituto)) {
            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto non è compatibile con il trainer da licenziare."
            );
        }

        servizioSwapCorsi.sostituisciTrainerNeiCorsi(
                String.valueOf(idDaLicenziare),
                String.valueOf(idSostituto)
        );

        trainerDAO.elimina(idDaLicenziare);
    }

    /**
     * Calcola il totale mensile delle retribuzioni dei Personal Trainer attivi.
     *
     * @return totale mensile delle retribuzioni
     */
    @Override
    public double calcolaTotaleRetribuzioniMensili() {
        return servizioRetribuzioni.calcolaTotaleRetribuzioniMensili();
    }

    /**
     * Crea il DTO persistente da usare per salvare un nuovo Personal Trainer.
     *
     * @param idTrainer id numerico del trainer, oppure null se generato dal database
     * @param nome nome normalizzato
     * @param cognome cognome normalizzato
     * @param email email normalizzata
     * @param specializzazione specializzazione normalizzata
     * @param tipoRetribuzione tipo di retribuzione
     * @param importoRetribuzione importo retributivo
     * @return DTO pronto per il DAO
     */
    private DatiPersonalTrainer creaDatiNuovoTrainer(
            Integer idTrainer,
            String nome,
            String cognome,
            String email,
            String specializzazione,
            String tipoRetribuzione,
            double importoRetribuzione) {

        String tipoRetribuzioneNormalizzato = tipoRetribuzione.trim().toUpperCase();

        String tipoContratto;
        double stipendioMensile;
        Double compensoPerLezione;

        if (TIPO_FISSA_MENSILE.equalsIgnoreCase(tipoRetribuzioneNormalizzato)) {
            tipoContratto = "Fisso";
            stipendioMensile = importoRetribuzione;
            compensoPerLezione = null;

        } else if (TIPO_A_LEZIONE.equalsIgnoreCase(tipoRetribuzioneNormalizzato)) {
            tipoContratto = "Provvigione";
            stipendioMensile = 0.00;
            compensoPerLezione = importoRetribuzione;

        } else {
            throw new IllegalArgumentException("Tipo retribuzione non valido: " + tipoRetribuzione);
        }
        
        Integer idDirettore = direttoreDAO.trovaIdDirettoreDisponibile();

    	if (idDirettore == null) {
    	    throw new IllegalStateException(
    	            "Nessun Direttore disponibile nel database per associare il Personal Trainer."
    	    );
    	}

        return new DatiPersonalTrainer(
                idTrainer,
                generaCodiceFiscaleTecnico(email, nome, cognome),
                nome,
                cognome,
                email,
                PASSWORD_PREDEFINITA,
                STATO_UTENTE_ATTIVO,
                specializzazione,
                tipoContratto,
                STATO_CONTRATTO_ATTIVO,
                true,
                tipoRetribuzioneNormalizzato,
                stipendioMensile,
                compensoPerLezione,
                idDirettore
        );
    }

    /**
     * Converte una lista di DTO in una lista di Personal Trainer di dominio.
     *
     * @param datiTrainers lista di DTO letti dal DAO
     * @return lista di Personal Trainer di dominio
     */
    private List<PersonalTrainer> convertiListaDatiInPersonalTrainer(
            List<DatiPersonalTrainer> datiTrainers) {

        List<PersonalTrainer> trainers = new ArrayList<>();

        for (DatiPersonalTrainer datiTrainer : datiTrainers) {
            PersonalTrainer trainer = convertiDatiInPersonalTrainer(datiTrainer);

            if (trainer != null) {
                trainers.add(trainer);
            }
        }

        return trainers;
    }

    /**
     * Converte un DTO persistente in un Personal Trainer di dominio.
     *
     * Il metodo ricostruisce la Strategy tramite Factory. Se nel database è presente
     * un tipo di retribuzione non valido, viene sollevata un'eccezione runtime
     * perché il dato persistente non è coerente.
     *
     * @param datiTrainer DTO letto dal DAO
     * @return Personal Trainer di dominio, oppure null se il DTO è null
     */
    private PersonalTrainer convertiDatiInPersonalTrainer(DatiPersonalTrainer datiTrainer) {
        if (datiTrainer == null) {
            return null;
        }

        try {
            double importoRetribuzione = selezionaImportoRetribuzione(datiTrainer);

            StrategiaRetribuzione strategia = StrategiaRetribuzioneFactory.crea(
                    datiTrainer.getTipoRetribuzione(),
                    importoRetribuzione
            );

            PersonalTrainer trainer = new PersonalTrainer(
                    datiTrainer.getNome(),
                    datiTrainer.getCognome(),
                    datiTrainer.getEmail(),
                    String.valueOf(datiTrainer.getIdTrainer()),
                    datiTrainer.getSpecializzazione(),
                    strategia
            );

            trainer.setStatoContratto(datiTrainer.getStatoContratto());
            trainer.setAttivo(datiTrainer.isAttivo());

            return trainer;

        } catch (TrainerNonValidoException e) {
            throw new RuntimeException(
                    "Dati retributivi non validi nel database per il Personal Trainer.",
                    e
            );
        }
    }

    /**
     * Seleziona l'importo corretto da passare alla Strategy Factory.
     *
     * @param datiTrainer dati persistenti del trainer
     * @return stipendio mensile o compenso per lezione
     */
    private double selezionaImportoRetribuzione(DatiPersonalTrainer datiTrainer) {
        if (TIPO_FISSA_MENSILE.equalsIgnoreCase(datiTrainer.getTipoRetribuzione())) {
            return datiTrainer.getStipendioMensile();
        }

        if (TIPO_A_LEZIONE.equalsIgnoreCase(datiTrainer.getTipoRetribuzione())) {
            return datiTrainer.getCompensoPerLezione() == null
                    ? 0.00
                    : datiTrainer.getCompensoPerLezione();
        }

        throw new IllegalArgumentException(
                "Tipo retribuzione non valido: " + datiTrainer.getTipoRetribuzione()
        );
    }

    /**
     * Verifica che il trainer possa essere licenziato.
     *
     * @param trainerDaLicenziare trainer da verificare
     * @throws TrainerNonValidoException se il trainer è già licenziato o non attivo
     */
    private void validaTrainerLicenziabile(PersonalTrainer trainerDaLicenziare)
            throws TrainerNonValidoException {

        if (!trainerDaLicenziare.isAttivo()
                || !STATO_CONTRATTO_ATTIVO.equalsIgnoreCase(trainerDaLicenziare.getStatoContratto())) {

            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: il Personal Trainer risulta già licenziato o non attivo."
            );
        }
    }

    /**
     * Normalizza l'identificativo del Personal Trainer in fase di assunzione.
     *
     * @param idTrainer identificativo ricevuto dal controller
     * @return identificativo numerico oppure null se AUTO
     * @throws TrainerNonValidoException se l'identificativo non è valido
     */
    private Integer normalizzaIdAssunzione(String idTrainer) throws TrainerNonValidoException {
        if (idTrainer == null || idTrainer.trim().isEmpty()) {
            return null;
        }

        if ("AUTO".equalsIgnoreCase(idTrainer.trim())) {
            return null;
        }

        return normalizzaIdObbligatorio(idTrainer, "identificativo del Personal Trainer");
    }

    /**
     * Normalizza un identificativo obbligatorio.
     *
     * @param idTrainer identificativo ricevuto
     * @param nomeCampo nome logico del campo
     * @return identificativo numerico
     * @throws TrainerNonValidoException se l'identificativo non è valido
     */
    private Integer normalizzaIdObbligatorio(String idTrainer, String nomeCampo)
            throws TrainerNonValidoException {

        String valoreNormalizzato = normalizzaCampoObbligatorio(idTrainer, nomeCampo);
        String soloNumeri = valoreNormalizzato.replaceAll("[^0-9]", "");

        if (soloNumeri.isBlank()) {
            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: " + nomeCampo + " non valido."
            );
        }

        return Integer.parseInt(soloNumeri);
    }

    /**
     * Normalizza e valida l'email obbligatoria del Personal Trainer.
     *
     * @param email email ricevuta dal controller
     * @return email normalizzata
     * @throws TrainerNonValidoException se l'email è assente o non valida
     */
    private String normalizzaEmailObbligatoria(String email) throws TrainerNonValidoException {
        String emailNormalizzata = normalizzaCampoObbligatorio(email, "email").toLowerCase();

        if (!emailNormalizzata.contains("@")) {
            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: email del Personal Trainer non valida."
            );
        }

        return emailNormalizzata;
    }

    /**
     * Normalizza un campo obbligatorio verificando che non sia vuoto.
     *
     * @param valore valore ricevuto dal controller
     * @param nomeCampo nome logico del campo usato nel messaggio di errore
     * @return valore normalizzato
     * @throws TrainerNonValidoException se il valore è nullo o vuoto
     */
    private String normalizzaCampoObbligatorio(String valore, String nomeCampo)
            throws TrainerNonValidoException {

        if (valore == null || valore.trim().isEmpty()) {
            throw new TrainerNonValidoException(
                    "OPERAZIONE ANNULLATA: " + nomeCampo + " del Personal Trainer non indicato."
            );
        }

        return valore.trim();
    }

    /**
     * Genera un codice fiscale tecnico stabile per rispettare i vincoli dello schema.
     *
     * @param email email del trainer
     * @param nome nome del trainer
     * @param cognome cognome del trainer
     * @return codice fiscale tecnico
     */
    private String generaCodiceFiscaleTecnico(String email, String nome, String cognome) {
        String base = email;

        if (base == null || base.isBlank()) {
            base = nome + "." + cognome + "." + System.nanoTime();
        }

        long hash = Math.abs((long) base.hashCode());
        String numeri = String.format("%014d", hash % 100000000000000L);

        return "PT" + numeri;
    }

    /**
     * Verifica se un Personal Trainer può essere usato come sostituto.
     *
     * @param trainerDaLicenziare trainer che deve essere sostituito
     * @param possibileSostituto trainer candidato alla sostituzione
     * @return true se il candidato è compatibile, false altrimenti
     */
    private boolean isSostitutoCompatibile(
            PersonalTrainer trainerDaLicenziare,
            PersonalTrainer possibileSostituto) {

        if (trainerDaLicenziare == null || possibileSostituto == null) {
            return false;
        }

        if (!possibileSostituto.isAttivo()) {
            return false;
        }

        if (!STATO_CONTRATTO_ATTIVO.equalsIgnoreCase(possibileSostituto.getStatoContratto())) {
            return false;
        }

        if (stessoTesto(trainerDaLicenziare.getIdTrainer(), possibileSostituto.getIdTrainer())) {
            return false;
        }

        return stessoTesto(
                trainerDaLicenziare.getSpecializzazione(),
                possibileSostituto.getSpecializzazione()
        );
    }

    /**
     * Confronta due stringhe ignorando maiuscole, minuscole e spazi esterni.
     *
     * @param primo primo valore da confrontare
     * @param secondo secondo valore da confrontare
     * @return true se i valori sono uguali, false altrimenti
     */
    private boolean stessoTesto(String primo, String secondo) {
        if (primo == null || secondo == null) {
            return false;
        }

        return primo.trim().equalsIgnoreCase(secondo.trim());
    }
}