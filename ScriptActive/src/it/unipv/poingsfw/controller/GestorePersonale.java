package it.unipv.poingsfw.controller;

import java.util.List;
import java.util.Objects;

import it.unipv.poingsfw.controller.mapper.GestionePersonaleViewMapper;
import it.unipv.poingsfw.dto.DatiVisualizzazioneTrainer;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.service.ServizioContrattiPersonale;
import it.unipv.poingsfw.view.GestionePersonaleView;

/**
 * Controller della gestione del personale.
 *
 * La classe intercetta gli eventi della View, coordina il flusso del caso
 * d'uso e delega la logica applicativa al Service del Model.
 */
public final class GestorePersonale {

    private static final String[] TIPI_RETRIBUZIONE = {
            "FISSA_MENSILE",
            "A_LEZIONE"
    };

    private final GestionePersonaleView view;

    private final ServizioContrattiPersonale
            servizioContratti;

    /*
     * Dati attualmente rappresentati nella schermata.
     *
     * Il Controller li conserva per associare gli indici grafici restituiti
     * dalla View ai corrispondenti identificativi applicativi.
     */
    private List<DatiVisualizzazioneTrainer>
            trainerVisualizzati = List.of();

    private List<DatiVisualizzazioneTrainer>
            sostitutiVisualizzati = List.of();

    /**
     * Crea il Controller e registra gli eventi della View.
     *
     * Il costruttore non esegue accessi al database.
     *
     * @param view schermata della gestione del personale
     * @param servizioContratti Service applicativo del caso d'uso
     */
    public GestorePersonale(
            GestionePersonaleView view,
            ServizioContrattiPersonale servizioContratti) {

        this.view = Objects.requireNonNull(
                view,
                "La View non può essere null."
        );

        this.servizioContratti =
                Objects.requireNonNull(
                        servizioContratti,
                        "Il Service non può essere null."
                );

        registraListener();
    }

    /**
     * Inizializza i dati della schermata.
     */
    public void inizializza() {
        view.mostraTipiRetribuzione(
                TIPI_RETRIBUZIONE.clone()
        );

        aggiornaElencoPersonalTrainer();
    }

    /**
     * Registra nel Controller tutti gli eventi generati dalla View.
     */
    private void registraListener() {
        view.getBtnAggiorna().addActionListener(
                evento -> aggiornaElencoPersonalTrainer()
        );

        view.getBtnAssumi().addActionListener(
                evento -> gestisciAssunzione()
        );

        view.getBtnLicenziaSenzaSostituto()
                .addActionListener(
                        evento ->
                                gestisciLicenziamentoSenzaSostituto()
                );

        view.getBtnLicenziaConSostituto()
                .addActionListener(
                        evento ->
                                gestisciLicenziamentoConSostituto()
                );

        view.getBtnCalcolaRetribuzioni()
                .addActionListener(
                        evento ->
                                gestisciCalcoloRetribuzioni()
                );

        view.getTabellaPT()
                .getSelectionModel()
                .addListSelectionListener(evento -> {

                    if (!evento.getValueIsAdjusting()) {
                        gestisciSelezioneTrainer();
                    }
                });
    }

    /**
     * Recupera l'elenco dei trainer dal Model e ne coordina
     * la rappresentazione nella View.
     */
    private void aggiornaElencoPersonalTrainer() {
        try {
            List<DatiVisualizzazioneTrainer> trainer =
                    servizioContratti
                            .getElencoPersonalTrainer();

            trainerVisualizzati =
                    trainer == null
                            ? List.of()
                            : List.copyOf(trainer);

            sostitutiVisualizzati = List.of();

            Object[][] righe =
                    GestionePersonaleViewMapper
                            .creaRigheTrainer(
                                    trainerVisualizzati
                            );

            view.mostraRigheTrainer(righe);
            view.mostraSostituti(new String[0]);
            view.mostraIdTrainerDaLicenziare("");
            view.mostraStato("Elenco aggiornato.");

        } catch (RuntimeException e) {
            trainerVisualizzati = List.of();
            sostitutiVisualizzati = List.of();

            view.mostraRigheTrainer(
                    new Object[0][0]
            );

            view.mostraSostituti(
                    new String[0]
            );

            view.mostraErrore(
                    "Errore durante il caricamento "
                    + "dei Personal Trainer."
            );
        }
    }

    /**
     * Coordina il flusso di assunzione.
     */
    private void gestisciAssunzione() {
        try {
            double importoRetribuzione =
                    GestionePersonaleViewMapper
                            .convertiImporto(
                                    view
                                            .getImportoRetribuzioneInserito()
                            );

            servizioContratti.assumiPersonalTrainer(
                    view.getNomeInserito(),
                    view.getCognomeInserito(),
                    view.getEmailInserita(),
                    view.getSpecializzazioneInserita(),
                    view.getTipoRetribuzioneSelezionato(),
                    importoRetribuzione
            );

            view.pulisciFormAssunzione();

            aggiornaElencoPersonalTrainer();

            view.mostraSuccesso(
                    "Personal Trainer assunto correttamente."
            );

        } catch (NumberFormatException e) {
            view.mostraErrore(
                    "L'importo della retribuzione "
                    + "deve essere numerico."
            );

        } catch (TrainerGiaAssuntoException |
                 TrainerNonValidoException e) {

            view.mostraErrore(e.getMessage());

        } catch (RuntimeException e) {
            view.mostraErrore(
                    "Errore durante l'assunzione "
                    + "del Personal Trainer."
            );
        }
    }

    /**
     * Coordina la selezione di un trainer nella tabella.
     */
    private void gestisciSelezioneTrainer() {
        int indiceSelezionato =
                view.getIndiceTrainerSelezionato();

        String idTrainer =
                GestionePersonaleViewMapper
                        .trovaIdTrainer(
                                trainerVisualizzati,
                                indiceSelezionato
                        );

        if (idTrainer == null
                || idTrainer.isBlank()) {

            return;
        }

        view.mostraIdTrainerDaLicenziare(
                idTrainer
        );

        caricaSostitutiCompatibili(idTrainer);
    }

    /**
     * Richiede al Model i sostituti compatibili e ne coordina
     * la rappresentazione grafica.
     *
     * @param idTrainerDaLicenziare identificativo del trainer
     */
    private void caricaSostitutiCompatibili(
            String idTrainerDaLicenziare) {

        try {
            List<DatiVisualizzazioneTrainer>
                    sostituti =
                            servizioContratti
                                    .getSostitutiCompatibili(
                                            idTrainerDaLicenziare
                                    );

            sostitutiVisualizzati =
                    sostituti == null
                            ? List.of()
                            : List.copyOf(sostituti);

            String[] descrizioni =
                    GestionePersonaleViewMapper
                            .creaDescrizioniSostituti(
                                    sostitutiVisualizzati
                            );

            view.mostraSostituti(descrizioni);

            view.mostraStato(
                    "Personal Trainer selezionato."
            );

        } catch (TrainerNonValidoException e) {
            sostitutiVisualizzati = List.of();

            view.mostraSostituti(
                    new String[0]
            );

            view.mostraErrore(e.getMessage());

        } catch (RuntimeException e) {
            sostitutiVisualizzati = List.of();

            view.mostraSostituti(
                    new String[0]
            );

            view.mostraErrore(
                    "Errore durante il caricamento "
                    + "dei sostituti compatibili."
            );
        }
    }

    /**
     * Coordina il licenziamento senza sostituto.
     */
    private void gestisciLicenziamentoSenzaSostituto() {
        try {
            servizioContratti
                    .licenziaPersonalTrainerSenzaSostituto(
                            view.getIdTrainerDaLicenziare()
                    );

            view.pulisciFormLicenziamento();

            aggiornaElencoPersonalTrainer();

            view.mostraSuccesso(
                    "Personal Trainer licenziato "
                    + "correttamente senza sostituto."
            );

        } catch (TrainerNonValidoException |
                 TrainerNonLicenziabileException e) {

            view.mostraErrore(e.getMessage());

        } catch (RuntimeException e) {
            view.mostraErrore(
                    "Errore durante il licenziamento "
                    + "senza sostituto."
            );
        }
    }

    /**
     * Coordina il licenziamento con sostituto.
     */
    private void gestisciLicenziamentoConSostituto() {
        try {
            int indiceSostituto =
                    view.getIndiceSostitutoSelezionato();

            String idSostituto =
                    GestionePersonaleViewMapper
                            .trovaIdTrainer(
                                    sostitutiVisualizzati,
                                    indiceSostituto
                            );

            if (idSostituto == null
                    || idSostituto.isBlank()) {

                view.mostraErrore(
                        "Selezionare un sostituto valido."
                );

                return;
            }

            servizioContratti
                    .licenziaPersonalTrainerConSostituto(
                            view.getIdTrainerDaLicenziare(),
                            idSostituto
                    );

            view.pulisciFormLicenziamento();

            aggiornaElencoPersonalTrainer();

            view.mostraSuccesso(
                    "Personal Trainer licenziato "
                    + "correttamente con sostituto."
            );

        } catch (TrainerNonValidoException |
                 SostitutoNonValidoException e) {

            view.mostraErrore(e.getMessage());

        } catch (RuntimeException e) {
            view.mostraErrore(
                    "Errore durante il licenziamento "
                    + "con sostituto."
            );
        }
    }

    /**
     * Coordina il calcolo delle retribuzioni mensili.
     */
    private void gestisciCalcoloRetribuzioni() {
        try {
            double totale =
                    servizioContratti
                            .calcolaTotaleRetribuzioniMensili();

            view.mostraTotaleRetribuzioni(totale);

        } catch (RuntimeException e) {
            view.mostraErrore(
                    "Errore durante il calcolo "
                    + "delle retribuzioni mensili."
            );
        }
    }
}