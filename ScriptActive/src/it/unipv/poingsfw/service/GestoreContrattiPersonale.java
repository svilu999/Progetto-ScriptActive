package it.unipv.poingsfw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.unipv.poingsfw.dao.DirettoreDAO;
import it.unipv.poingsfw.dao.PersonalTrainerDAO;
import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.dto.DatiPersonalTrainer;
import it.unipv.poingsfw.dto.DatiVisualizzazioneTrainer;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.strategy.StrategiaRetribuzione;
import it.unipv.poingsfw.strategy.StrategiaRetribuzioneFactory;

/**
 * Gestore applicativo dei contratti del personale.
 *
 * La classe appartiene al Model logico del caso d'uso e contiene la logica
 * applicativa relativa alla gestione contrattuale dei Personal Trainer.
 *
 * Coordina gli oggetti di dominio, le Strategy, i DAO e gli altri servizi
 * interni al Model senza esporli al Controller.
 */
public class GestoreContrattiPersonale
        implements ServizioContrattiPersonale {

    private static final String STATO_CONTRATTO_ATTIVO =
            "ATTIVO";

    private static final String STATO_UTENTE_ATTIVO =
            "Attivo";

    private static final String PASSWORD_PREDEFINITA =
            "1234";

    private static final String TIPO_FISSA_MENSILE =
            "FISSA_MENSILE";

    private static final String TIPO_A_LEZIONE =
            "A_LEZIONE";

    private final PersonalTrainerDAO trainerDAO;
    private final DirettoreDAO direttoreDAO;
    private final ServizioSwapCorsi servizioSwapCorsi;
    private final ServizioRetribuzioni servizioRetribuzioni;

    /**
     * Crea il gestore dei contratti del personale ricevendo le dipendenze
     * dall'esterno.
     *
     * @param trainerDAO DAO per la persistenza dei Personal Trainer
     * @param direttoreDAO DAO utilizzato per individuare il Direttore
     *        associato al nuovo trainer
     * @param servizioSwapCorsi servizio per la gestione dei corsi assegnati
     * @param servizioRetribuzioni servizio per il calcolo delle retribuzioni
     */
    public GestoreContrattiPersonale(
            PersonalTrainerDAO trainerDAO,
            DirettoreDAO direttoreDAO,
            ServizioSwapCorsi servizioSwapCorsi,
            ServizioRetribuzioni servizioRetribuzioni) {

        this.trainerDAO = Objects.requireNonNull(
                trainerDAO,
                "trainerDAO non può essere null"
        );

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
     * Il metodo valida i dati, controlla eventuali duplicati, crea la Strategy,
     * costruisce l'oggetto di dominio e delega la persistenza al DAO.
     *
     * @param nome nome del Personal Trainer
     * @param cognome cognome del Personal Trainer
     * @param email email del Personal Trainer
     * @param specializzazione specializzazione professionale
     * @param tipoRetribuzione tipo di retribuzione selezionato
     * @param importoRetribuzione importo associato alla retribuzione
     * @throws TrainerGiaAssuntoException se esiste già un trainer con la
     *         stessa email
     * @throws TrainerNonValidoException se i dati ricevuti non sono validi
     */
    @Override
    public void assumiPersonalTrainer(
            String nome,
            String cognome,
            String email,
            String specializzazione,
            String tipoRetribuzione,
            double importoRetribuzione)
            throws TrainerGiaAssuntoException,
                   TrainerNonValidoException {

        String nomeNormalizzato =
                normalizzaCampoObbligatorio(
                        nome,
                        "Nome del Personal Trainer"
                );

        String cognomeNormalizzato =
                normalizzaCampoObbligatorio(
                        cognome,
                        "Cognome del Personal Trainer"
                );

        String emailNormalizzata =
                normalizzaEmailObbligatoria(email);

        String specializzazioneNormalizzata =
                normalizzaCampoObbligatorio(
                        specializzazione,
                        "Specializzazione del Personal Trainer"
                );

        if (trainerDAO.trovaPerEmail(
                emailNormalizzata) != null) {

            throw new TrainerGiaAssuntoException(
                    "Esiste già un Personal Trainer "
                    + "registrato con email "
                    + emailNormalizzata
                    + "."
            );
        }

        StrategiaRetribuzione strategia =
                StrategiaRetribuzioneFactory.crea(
                        tipoRetribuzione,
                        importoRetribuzione
                );

        PersonalTrainer nuovoTrainer =
                new PersonalTrainer(
                        nomeNormalizzato,
                        cognomeNormalizzato,
                        emailNormalizzata,
                        null,
                        specializzazioneNormalizzata,
                        strategia
                );

        DatiPersonalTrainer datiNuovoTrainer =
                creaDatiNuovoTrainer(
                        nuovoTrainer,
                        importoRetribuzione
                );

        trainerDAO.salva(datiNuovoTrainer);
    }

    /**
     * Restituisce i dati necessari alla visualizzazione dei Personal Trainer.
     *
     * I DTO persistenti restituiti dal DAO vengono convertiti in oggetti di
     * dominio e successivamente in DTO destinati al Controller.
     *
     * @return lista dei dati destinati alla schermata
     */
    @Override
    public List<DatiVisualizzazioneTrainer>
            getElencoPersonalTrainer() {

        List<DatiPersonalTrainer> datiPersistenti =
                trainerDAO.trovaTutti();

        List<PersonalTrainer> trainerDiDominio =
                convertiListaDatiInPersonalTrainer(
                        datiPersistenti
                );

        List<DatiVisualizzazioneTrainer>
                datiVisualizzazione =
                        new ArrayList<>();

        for (PersonalTrainer trainer :
                trainerDiDominio) {

            datiVisualizzazione.add(
                    convertiInDatiVisualizzazione(
                            trainer
                    )
            );
        }

        return datiVisualizzazione;
    }

    /**
     * Individua i Personal Trainer compatibili con quello da licenziare.
     *
     * La compatibilità viene verificata internamente al Service. Al Controller
     * vengono restituiti soltanto DTO destinati alla presentazione.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da sostituire
     * @return lista dei sostituti compatibili
     * @throws TrainerNonValidoException se l'identificativo non è valido, il
     *         trainer non esiste oppure non ha un contratto attivo
     */
    @Override
    public List<DatiVisualizzazioneTrainer>
            getSostitutiCompatibili(
                    String idTrainerDaLicenziare)
                    throws TrainerNonValidoException {

        Integer idTrainerNumerico =
                normalizzaIdObbligatorio(
                        idTrainerDaLicenziare,
                        "Identificativo del Personal Trainer "
                        + "da licenziare"
                );

        DatiPersonalTrainer datiTrainerDaLicenziare =
                trainerDAO.trovaPerId(
                        idTrainerNumerico
                );

        if (datiTrainerDaLicenziare == null) {
            throw new TrainerNonValidoException(
                    "Il Personal Trainer da licenziare "
                    + "non è stato trovato."
            );
        }

        PersonalTrainer trainerDaLicenziare =
                convertiDatiInPersonalTrainer(
                        datiTrainerDaLicenziare
                );

        validaTrainerLicenziabile(
                trainerDaLicenziare
        );

        List<PersonalTrainer> elencoTrainer =
                convertiListaDatiInPersonalTrainer(
                        trainerDAO.trovaTutti()
                );

        List<DatiVisualizzazioneTrainer>
                sostitutiCompatibili =
                        new ArrayList<>();

        for (PersonalTrainer possibileSostituto :
                elencoTrainer) {

            if (isSostitutoCompatibile(
                    trainerDaLicenziare,
                    possibileSostituto)) {

                sostitutiCompatibili.add(
                        convertiInDatiVisualizzazione(
                                possibileSostituto
                        )
                );
            }
        }

        return sostitutiCompatibili;
    }

    /**
     * Gestisce il licenziamento di un Personal Trainer senza sostituto.
     *
     * @param idTrainer identificativo del trainer da licenziare
     * @throws TrainerNonValidoException se il trainer non esiste, non è valido
     *         oppure risulta già licenziato
     * @throws TrainerNonLicenziabileException se il trainer possiede corsi
     *         attivi o futuri
     */
    @Override
    public void licenziaPersonalTrainerSenzaSostituto(
            String idTrainer)
            throws TrainerNonValidoException,
                   TrainerNonLicenziabileException {

        Integer idTrainerNumerico =
                normalizzaIdObbligatorio(
                        idTrainer,
                        "Identificativo del Personal Trainer "
                        + "da licenziare"
                );

        DatiPersonalTrainer datiTrainer =
                trainerDAO.trovaPerId(
                        idTrainerNumerico
                );

        if (datiTrainer == null) {
            throw new TrainerNonValidoException(
                    "Il Personal Trainer da licenziare "
                    + "non esiste."
            );
        }

        PersonalTrainer trainerDaLicenziare =
                convertiDatiInPersonalTrainer(
                        datiTrainer
                );

        validaTrainerLicenziabile(
                trainerDaLicenziare
        );

        boolean possiedeCorsi =
                servizioSwapCorsi
                        .haCorsiAttiviOFuturi(
                                String.valueOf(
                                        idTrainerNumerico
                                )
                        );

        if (possiedeCorsi) {
            throw new TrainerNonLicenziabileException(
                    "Il Personal Trainer ha corsi "
                    + "attivi o futuri assegnati."
            );
        }

        trainerDAO.disattiva(
                idTrainerNumerico
        );
    }

    /**
     * Gestisce il licenziamento di un Personal Trainer con sostituto.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da licenziare
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @throws TrainerNonValidoException se il trainer da licenziare non esiste,
     *         non è valido oppure risulta già licenziato
     * @throws SostitutoNonValidoException se il sostituto non esiste oppure
     *         non è compatibile
     */
    @Override
    public void licenziaPersonalTrainerConSostituto(
            String idTrainerDaLicenziare,
            String idTrainerSostituto)
            throws TrainerNonValidoException,
                   SostitutoNonValidoException {

        Integer idDaLicenziare =
                normalizzaIdObbligatorio(
                        idTrainerDaLicenziare,
                        "Identificativo del Personal Trainer "
                        + "da licenziare"
                );

        Integer idSostituto =
                normalizzaIdObbligatorio(
                        idTrainerSostituto,
                        "Identificativo del Personal Trainer "
                        + "sostituto"
                );

        DatiPersonalTrainer datiTrainerDaLicenziare =
                trainerDAO.trovaPerId(
                        idDaLicenziare
                );

        if (datiTrainerDaLicenziare == null) {
            throw new TrainerNonValidoException(
                    "Il Personal Trainer da licenziare "
                    + "non esiste."
            );
        }

        PersonalTrainer trainerDaLicenziare =
                convertiDatiInPersonalTrainer(
                        datiTrainerDaLicenziare
                );

        validaTrainerLicenziabile(
                trainerDaLicenziare
        );

        DatiPersonalTrainer datiSostituto =
                trainerDAO.trovaPerId(
                        idSostituto
                );

        if (datiSostituto == null) {
            throw new SostitutoNonValidoException(
                    "Il Personal Trainer sostituto "
                    + "non esiste."
            );
        }

        PersonalTrainer sostituto =
                convertiDatiInPersonalTrainer(
                        datiSostituto
                );

        if (!isSostitutoCompatibile(
                trainerDaLicenziare,
                sostituto)) {

            throw new SostitutoNonValidoException(
                    "Il sostituto non è compatibile "
                    + "con il trainer da licenziare."
            );
        }

        servizioSwapCorsi
                .sostituisciTrainerNeiCorsi(
                        String.valueOf(
                                idDaLicenziare
                        ),
                        String.valueOf(
                                idSostituto
                        )
                );

    }

    /**
     * Calcola il totale mensile delle retribuzioni dei Personal Trainer attivi.
     *
     * @return totale mensile delle retribuzioni
     */
    @Override
    public double calcolaTotaleRetribuzioniMensili() {
        return servizioRetribuzioni
                .calcolaTotaleRetribuzioniMensili();
    }

    /**
     * Prepara il DTO persistente partendo dal Personal Trainer di dominio.
     *
     * Il metodo estrae dalla Strategy il tipo di retribuzione, prepara i valori
     * persistenti e associa il trainer a un Direttore presente nel sistema.
     *
     * @param trainer Personal Trainer da convertire
     * @param importoRetribuzione importo associato alla Strategy
     * @return DTO pronto per essere passato al DAO
     */
    private DatiPersonalTrainer creaDatiNuovoTrainer(
            PersonalTrainer trainer,
            double importoRetribuzione) {

        StrategiaRetribuzione strategia =
                trainer.getStrategia();

        if (strategia == null) {
            throw new IllegalStateException(
                    "Il Personal Trainer deve avere "
                    + "una strategia di retribuzione."
            );
        }

        String tipoRetribuzione =
                strategia.getTipoRetribuzione();

        double stipendioMensile;
        Double compensoPerLezione;

        if (TIPO_FISSA_MENSILE.equalsIgnoreCase(
                tipoRetribuzione)) {

            trainer.setTipoContratto(
                    "Fisso"
            );

            stipendioMensile =
                    importoRetribuzione;

            compensoPerLezione =
                    null;

        } else if (TIPO_A_LEZIONE.equalsIgnoreCase(
                tipoRetribuzione)) {

            trainer.setTipoContratto(
                    "Provvigione"
            );

            stipendioMensile =
                    0.00;

            compensoPerLezione =
                    importoRetribuzione;

        } else {
            throw new IllegalStateException(
                    "Tipo di retribuzione non gestito: "
                    + tipoRetribuzione
            );
        }

        Integer idDirettore =
                direttoreDAO
                        .trovaIdDirettoreDisponibile();

        if (idDirettore == null) {
            throw new IllegalStateException(
                    "Nessun Direttore disponibile "
                    + "per associare il Personal Trainer."
            );
        }

        return new DatiPersonalTrainer(
                null,
                generaCodiceFiscaleTecnico(
                        trainer.getEmail(),
                        trainer.getNome(),
                        trainer.getCognome()
                ),
                trainer.getNome(),
                trainer.getCognome(),
                trainer.getEmail(),
                PASSWORD_PREDEFINITA,
                STATO_UTENTE_ATTIVO,
                trainer.getSpecializzazione(),
                trainer.getTipoContratto(),
                trainer.getStatoContratto(),
                trainer.isAttivo(),
                tipoRetribuzione,
                stipendioMensile,
                compensoPerLezione,
                idDirettore
        );
    }

    /**
     * Converte un Personal Trainer di dominio nel DTO destinato alla
     * presentazione.
     *
     * @param trainer Personal Trainer da convertire
     * @return DTO contenente i dati necessari alla schermata
     */
    private DatiVisualizzazioneTrainer
            convertiInDatiVisualizzazione(
                    PersonalTrainer trainer) {

        Objects.requireNonNull(
                trainer,
                "trainer non può essere null"
        );

        return new DatiVisualizzazioneTrainer(
                trainer.getIdTrainer(),
                trainer.getNomeCompleto(),
                trainer.getEmail(),
                trainer.getSpecializzazione(),
                trainer.getStatoContratto(),
                trainer.isAttivo()
        );
    }

    /**
     * Converte un DTO persistente in un Personal Trainer di dominio.
     *
     * La Strategy viene ricostruita tramite la relativa Factory.
     *
     * @param datiTrainer DTO letto dal DAO
     * @return Personal Trainer di dominio oppure {@code null}
     */
    private PersonalTrainer convertiDatiInPersonalTrainer(
            DatiPersonalTrainer datiTrainer) {

        if (datiTrainer == null) {
            return null;
        }

        try {
            double importoRetribuzione =
                    selezionaImportoRetribuzione(
                            datiTrainer
                    );

            StrategiaRetribuzione strategia =
                    StrategiaRetribuzioneFactory.crea(
                            datiTrainer
                                    .getTipoRetribuzione(),
                            importoRetribuzione
                    );

            PersonalTrainer trainer =
                    new PersonalTrainer(
                            datiTrainer.getNome(),
                            datiTrainer.getCognome(),
                            datiTrainer.getEmail(),
                            String.valueOf(
                                    datiTrainer
                                            .getIdTrainer()
                            ),
                            datiTrainer
                                    .getSpecializzazione(),
                            strategia
                    );

            trainer.setTipoContratto(
                    datiTrainer.getTipoContratto()
            );

            trainer.setStatoContratto(
                    datiTrainer.getStatoContratto()
            );

            trainer.setAttivo(
                    datiTrainer.isAttivo()
            );

            return trainer;

        } catch (TrainerNonValidoException e) {
            throw new IllegalStateException(
                    "I dati retributivi memorizzati "
                    + "per il Personal Trainer "
                    + "non sono validi.",
                    e
            );
        }
    }

    /**
     * Converte una lista di DTO persistenti in una lista di Personal Trainer.
     *
     * @param datiTrainer lista dei DTO restituiti dal DAO
     * @return lista dei Personal Trainer di dominio
     */
    private List<PersonalTrainer>
            convertiListaDatiInPersonalTrainer(
                    List<DatiPersonalTrainer> datiTrainer) {

        Objects.requireNonNull(
                datiTrainer,
                "La lista dei dati trainer "
                + "non può essere null"
        );

        List<PersonalTrainer> trainerDiDominio =
                new ArrayList<>();

        for (DatiPersonalTrainer dati :
                datiTrainer) {

            trainerDiDominio.add(
                    convertiDatiInPersonalTrainer(
                            dati
                    )
            );
        }

        return trainerDiDominio;
    }

    /**
     * Seleziona l'importo da utilizzare per ricostruire la Strategy.
     *
     * @param datiTrainer dati persistenti del trainer
     * @return stipendio mensile oppure compenso per lezione
     */
    private double selezionaImportoRetribuzione(
            DatiPersonalTrainer datiTrainer) {

        String tipoRetribuzione =
                datiTrainer.getTipoRetribuzione();

        if (TIPO_FISSA_MENSILE.equalsIgnoreCase(
                tipoRetribuzione)) {

            double stipendio =
                    datiTrainer.getStipendioMensile();

            if (stipendio < 0) {
                throw new IllegalStateException(
                        "Lo stipendio mensile memorizzato "
                        + "non può essere negativo."
                );
            }

            return stipendio;
        }

        if (TIPO_A_LEZIONE.equalsIgnoreCase(
                tipoRetribuzione)) {

            Double compenso =
                    datiTrainer
                            .getCompensoPerLezione();

            if (compenso == null) {
                throw new IllegalStateException(
                        "Il compenso per lezione "
                        + "non è presente nel database."
                );
            }

            if (compenso < 0) {
                throw new IllegalStateException(
                        "Il compenso per lezione "
                        + "non può essere negativo."
                );
            }

            return compenso;
        }

        throw new IllegalStateException(
                "Tipo di retribuzione non valido: "
                + tipoRetribuzione
        );
    }

    /**
     * Verifica che il trainer possa essere licenziato.
     *
     * @param trainerDaLicenziare trainer da verificare
     * @throws TrainerNonValidoException se il trainer risulta già licenziato
     *         oppure non attivo
     */
    private void validaTrainerLicenziabile(
            PersonalTrainer trainerDaLicenziare)
            throws TrainerNonValidoException {

        if (!trainerDaLicenziare.isAttivo()
                || !STATO_CONTRATTO_ATTIVO
                        .equalsIgnoreCase(
                                trainerDaLicenziare
                                        .getStatoContratto()
                        )) {

            throw new TrainerNonValidoException(
                    "Il Personal Trainer risulta "
                    + "già licenziato o non attivo."
            );
        }
    }

    /**
     * Normalizza e converte un identificativo numerico obbligatorio.
     *
     * @param idTrainer identificativo ricevuto dal Controller
     * @param nomeCampo nome logico del campo
     * @return identificativo numerico positivo
     * @throws TrainerNonValidoException se l'identificativo non è valido
     */
    private Integer normalizzaIdObbligatorio(
            String idTrainer,
            String nomeCampo)
            throws TrainerNonValidoException {

        String valoreNormalizzato =
                normalizzaCampoObbligatorio(
                        idTrainer,
                        nomeCampo
                );

        if (!valoreNormalizzato.matches(
                "\\d+")) {

            throw new TrainerNonValidoException(
                    nomeCampo + " non valido."
            );
        }

        try {
            int idNumerico =
                    Integer.parseInt(
                            valoreNormalizzato
                    );

            if (idNumerico <= 0) {
                throw new TrainerNonValidoException(
                        nomeCampo
                        + " deve essere maggiore di zero."
                );
            }

            return idNumerico;

        } catch (NumberFormatException e) {
            throw new TrainerNonValidoException(
                    nomeCampo
                    + " supera il valore numerico consentito."
            );
        }
    }

    /**
     * Normalizza e valida l'email obbligatoria.
     *
     * @param email email ricevuta dal Controller
     * @return email normalizzata
     * @throws TrainerNonValidoException se l'email non è valida
     */
    private String normalizzaEmailObbligatoria(
            String email)
            throws TrainerNonValidoException {

        String emailNormalizzata =
                normalizzaCampoObbligatorio(
                        email,
                        "Email del Personal Trainer"
                ).toLowerCase();

        if (!emailNormalizzata.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new TrainerNonValidoException(
                    "Email del Personal Trainer "
                    + "non valida."
            );
        }

        return emailNormalizzata;
    }

    /**
     * Normalizza un campo obbligatorio.
     *
     * @param valore valore ricevuto dal Controller
     * @param nomeCampo nome logico del campo
     * @return valore senza spazi esterni
     * @throws TrainerNonValidoException se il valore è nullo o vuoto
     */
    private String normalizzaCampoObbligatorio(
            String valore,
            String nomeCampo)
            throws TrainerNonValidoException {

        if (valore == null
                || valore.trim().isEmpty()) {

            throw new TrainerNonValidoException(
                    nomeCampo + " non indicato."
            );
        }

        return valore.trim();
    }

    /**
     * Genera un codice fiscale tecnico stabile per rispettare i vincoli dello
     * schema corrente.
     *
     * @param email email del trainer
     * @param nome nome del trainer
     * @param cognome cognome del trainer
     * @return codice fiscale tecnico
     */
    private String generaCodiceFiscaleTecnico(
            String email,
            String nome,
            String cognome) {

        String base = email;

        if (base == null || base.isBlank()) {
            base = nome
                    + "."
                    + cognome
                    + "."
                    + System.nanoTime();
        }

        long hash =
                Math.abs(
                        (long) base.hashCode()
                );

        String numeri =
                String.format(
                        "%014d",
                        hash
                        % 100000000000000L
                );

        return "PT" + numeri;
    }

    /**
     * Verifica se un Personal Trainer può essere utilizzato come sostituto.
     *
     * @param trainerDaLicenziare trainer da sostituire
     * @param possibileSostituto trainer candidato
     * @return {@code true} se il candidato è compatibile
     */
    private boolean isSostitutoCompatibile(
            PersonalTrainer trainerDaLicenziare,
            PersonalTrainer possibileSostituto) {

        if (trainerDaLicenziare == null
                || possibileSostituto == null) {

            return false;
        }

        if (!possibileSostituto.isAttivo()) {
            return false;
        }

        if (!STATO_CONTRATTO_ATTIVO
                .equalsIgnoreCase(
                        possibileSostituto
                                .getStatoContratto()
                )) {

            return false;
        }

        if (stessoTesto(
                trainerDaLicenziare
                        .getIdTrainer(),
                possibileSostituto
                        .getIdTrainer())) {

            return false;
        }

        return stessoTesto(
                trainerDaLicenziare
                        .getSpecializzazione(),
                possibileSostituto
                        .getSpecializzazione()
        );
    }

    /**
     * Confronta due stringhe ignorando maiuscole, minuscole e spazi esterni.
     *
     * @param primo primo valore
     * @param secondo secondo valore
     * @return {@code true} se i valori sono equivalenti
     */
    private boolean stessoTesto(
            String primo,
            String secondo) {

        if (primo == null || secondo == null) {
            return false;
        }

        return primo.trim()
                .equalsIgnoreCase(
                        secondo.trim()
                );
    }
}