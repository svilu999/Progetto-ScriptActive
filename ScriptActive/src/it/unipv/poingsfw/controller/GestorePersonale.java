package it.unipv.poingsfw.controller;

import java.util.List;
import java.util.Objects;

import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.service.ServizioContrattiPersonale;
import it.unipv.poingsfw.view.GestionePersonaleView;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;

/**
 * Controller della gestione del personale.
 *
 * Questa classe intercetta gli eventi generati dalla View, coordina il flusso
 * applicativo e delega la logica del caso d'uso al servizio del Model.
 * Non contiene logica di business, non accede ai DAO e non conosce il database.
 */
public class GestorePersonale {

    private final GestionePersonaleView view;
    private final ServizioContrattiPersonale servizioContratti;

    /**
     * Crea il controller della gestione del personale.
     *
     * @param view schermata grafica della gestione del personale
     * @param servizioContratti servizio applicativo dei contratti del personale
     */
    public GestorePersonale(
            GestionePersonaleView view,
            ServizioContrattiPersonale servizioContratti) {

        this.view = Objects.requireNonNull(view, "view non può essere null");
        this.servizioContratti = Objects.requireNonNull(
                servizioContratti,
                "servizioContratti non può essere null"
        );

        inizializzaListeners();
        aggiornaElencoPersonalTrainer();
    }

    /**
     * Registra gli ActionListener sui componenti esposti dalla View.
     *
     * La View non richiama mai il Controller: il Controller si collega ai
     * componenti grafici e intercetta gli eventi dell'utente.
     */
    private void inizializzaListeners() {
        view.getBtnAggiorna().addActionListener(e -> aggiornaElencoPersonalTrainer());
        view.getBtnAssumi().addActionListener(e -> gestisciAssunzionePersonalTrainer());
        view.getBtnLicenziaSenzaSostituto().addActionListener(e -> gestisciLicenziamentoSenzaSostituto());
        view.getBtnLicenziaConSostituto().addActionListener(e -> gestisciLicenziamentoConSostituto());
        view.getBtnCalcolaRetribuzioni().addActionListener(e -> gestisciCalcoloRetribuzioni());
        
        view.getTabellaPT().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                gestisciSelezionePersonalTrainer();
            }
        });
    }

    /**
     * Aggiorna la tabella grafica dei Personal Trainer.
     *
     * Il metodo recupera i dati dal servizio applicativo e li trasferisce nel
     * modello della tabella della View. La View non accede al Model.
     */
    private void aggiornaElencoPersonalTrainer() {
        try {
            List<PersonalTrainer> listaTrainer = servizioContratti.getElencoPersonalTrainer();

            view.getModelloTabella().setRowCount(0);

            for (PersonalTrainer trainer : listaTrainer) {
                Object[] riga = {
                        trainer.getIdTrainer(),
                        trainer.getNomeCompleto(),
                        trainer.getEmail(),
                        trainer.getSpecializzazione(),
                        trainer.getStatoContratto(),
                        trainer.isAttivo() ? "Sì" : "No"
                };

                view.getModelloTabella().addRow(riga);
            }

            view.getLblStato().setText("Elenco aggiornato. PT caricati: " + listaTrainer.size());

        } catch (Exception e) {
            view.getLblStato().setText("Errore durante il caricamento dei Personal Trainer.");
            e.printStackTrace();
        }
    }
    
    /**
     * Gestisce il flusso di assunzione di un Personal Trainer.
     *
     * Il Controller legge i dati dai componenti grafici della View, effettua solo
     * la conversione tecnica dell'importo e delega la logica applicativa al
     * servizio del Model.
     */
    private void gestisciAssunzionePersonalTrainer() {
        try {
            double importoRetribuzione = Double.parseDouble(
                    view.getTxtImportoRetribuzione().getText().trim()
            );

            servizioContratti.assumiPersonalTrainer(
                    view.getTxtNome().getText(),
                    view.getTxtCognome().getText(),
                    view.getTxtEmail().getText(),
                    "AUTO",
                    view.getTxtSpecializzazione().getText(),
                    (String) view.getComboTipoRetribuzione().getSelectedItem(),
                    importoRetribuzione
            );

            pulisciCampiAssunzione();
            aggiornaElencoPersonalTrainer();

            mostraSuccesso("Personal Trainer assunto correttamente.");

        } catch (NumberFormatException e) {
        	mostraErrore("Errore: l'importo della retribuzione deve essere numerico.");

        } catch (TrainerGiaAssuntoException | TrainerNonValidoException e) {
        	mostraErrore(e.getMessage());

        } catch (Exception e) {
        	mostraErrore("Errore durante l'assunzione del Personal Trainer.");
        }
    }
    
    /**
     * Ripulisce i campi grafici del form di assunzione.
     *
     * Si tratta di un aggiornamento della View coordinato dal Controller dopo
     * l'esito positivo dell'operazione.
     */
    private void pulisciCampiAssunzione() {
        view.getTxtNome().setText("");
        view.getTxtCognome().setText("");
        view.getTxtEmail().setText("");
        view.getTxtIdTrainer().setText("Generato automaticamente");
        view.getTxtSpecializzazione().setText("");
        view.getTxtImportoRetribuzione().setText("");
        view.getComboTipoRetribuzione().setSelectedIndex(0);
    }
    
    /**
     * Gestisce la selezione di un Personal Trainer dalla tabella.
     *
     * Il Controller legge l'identificativo dalla riga selezionata, aggiorna il campo
     * grafico del trainer da licenziare e richiede al servizio i sostituti compatibili.
     */
    private void gestisciSelezionePersonalTrainer() {
        int rigaSelezionata = view.getTabellaPT().getSelectedRow();

        if (rigaSelezionata < 0) {
            return;
        }

        int rigaModello = view.getTabellaPT().convertRowIndexToModel(rigaSelezionata);

        String idTrainer = view.getModelloTabella()
                .getValueAt(rigaModello, 0)
                .toString();

        view.getTxtIdDaLicenziare().setText(idTrainer);
        caricaSostitutiCompatibili(idTrainer);
    }
    
    /**
     * Carica nella combo box i Personal Trainer compatibili come sostituti.
     *
     * Il Controller non applica direttamente le regole di compatibilità: richiede
     * l'elenco al servizio applicativo e aggiorna solo la componente grafica.
     *
     * @param idTrainerDaLicenziare identificativo del trainer selezionato
     */
    private void caricaSostitutiCompatibili(String idTrainerDaLicenziare) {
        try {
            List<PersonalTrainer> sostituti = servizioContratti.getSostitutiCompatibili(idTrainerDaLicenziare);

            view.getComboSostituto().removeAllItems();
            view.getComboSostituto().addItem("Seleziona sostituto...");

            for (PersonalTrainer sostituto : sostituti) {
                view.getComboSostituto().addItem(
                        sostituto.getIdTrainer()
                                + " - "
                                + sostituto.getNomeCompleto()
                                + " - "
                                + sostituto.getSpecializzazione()
                );
            }

            if (sostituti.isEmpty()) {
                view.getComboSostituto().addItem("Nessun sostituto compatibile");
            }

            view.getLblStato().setText("PT selezionato: " + idTrainerDaLicenziare);

        } catch (TrainerNonValidoException e) {
            view.getComboSostituto().removeAllItems();
            view.getComboSostituto().addItem("Seleziona sostituto...");
            view.getLblStato().setText(e.getMessage());

        } catch (Exception e) {
            view.getComboSostituto().removeAllItems();
            view.getComboSostituto().addItem("Seleziona sostituto...");
            view.getLblStato().setText("Errore durante il caricamento dei sostituti compatibili.");
            e.printStackTrace();
        }
    }
    
    /**
     * Gestisce il licenziamento di un Personal Trainer senza sostituto.
     *
     * Il Controller legge l'identificativo dalla View e delega al servizio la
     * verifica delle regole applicative. La disattivazione logica del trainer
     * viene eseguita dal Model tramite il DAO.
     */
    private void gestisciLicenziamentoSenzaSostituto() {
        try {
            String idTrainer = view.getTxtIdDaLicenziare().getText().trim();

            servizioContratti.licenziaPersonalTrainerSenzaSostituto(idTrainer);

            pulisciCampiLicenziamento();
            aggiornaElencoPersonalTrainer();

            mostraSuccesso("Personal Trainer licenziato correttamente senza sostituto.");

        } catch (TrainerNonValidoException | TrainerNonLicenziabileException e) {
        	mostraErrore(e.getMessage());

        } catch (Exception e) {
        	mostraErrore("Errore durante il licenziamento senza sostituto.");
            e.printStackTrace();
        }
    }
    
    /**
     * Gestisce il licenziamento di un Personal Trainer con sostituto.
     *
     * Il Controller legge dalla View il trainer da licenziare e il sostituto scelto,
     * poi delega al servizio le verifiche applicative e lo swap sui corsi.
     */
    private void gestisciLicenziamentoConSostituto() {
        try {
            String idTrainerDaLicenziare = view.getTxtIdDaLicenziare().getText().trim();
            String idSostituto = estraiIdSostitutoSelezionato();

            if (idSostituto == null || idSostituto.isBlank()) {
                view.getLblStato().setText("Selezionare un sostituto valido.");
                return;
            }

            servizioContratti.licenziaPersonalTrainerConSostituto(
                    idTrainerDaLicenziare,
                    idSostituto
            );

            pulisciCampiLicenziamento();
            aggiornaElencoPersonalTrainer();

            mostraSuccesso("Personal Trainer licenziato correttamente con sostituto.");

        } catch (TrainerNonValidoException | SostitutoNonValidoException e) {
        	mostraErrore(e.getMessage());

        } catch (Exception e) {
        	mostraErrore("Selezionare un sostituto valido.");
            e.printStackTrace();
        }
    }
    
    /**
     * Estrae l'identificativo del sostituto dalla voce selezionata nella combo box.
     *
     * La combo mostra una stringa descrittiva del tipo "ID - Nome - Specializzazione";
     * al servizio viene passato solo l'identificativo tecnico del trainer.
     *
     * @return identificativo del sostituto, oppure null se la selezione non è valida
     */
    private String estraiIdSostitutoSelezionato() {
        Object elementoSelezionato = view.getComboSostituto().getSelectedItem();

        if (elementoSelezionato == null) {
            return null;
        }

        String testoSelezionato = elementoSelezionato.toString().trim();

        if (testoSelezionato.isBlank()
                || testoSelezionato.equals("Seleziona sostituto...")
                || testoSelezionato.equals("Nessun sostituto compatibile")) {
            return null;
        }

        return testoSelezionato.split(" - ")[0].trim();
    }
    
    /**
     * Gestisce il calcolo del totale mensile delle retribuzioni.
     *
     * Il Controller non calcola direttamente le retribuzioni: richiede il risultato
     * al servizio applicativo e aggiorna soltanto la label di stato della View.
     */
    private void gestisciCalcoloRetribuzioni() {
        try {
            double totaleRetribuzioni = servizioContratti.calcolaTotaleRetribuzioniMensili();

            mostraSuccesso(
                    String.format("Totale retribuzioni mensili: %.2f €", totaleRetribuzioni)
            );

        } catch (Exception e) {
        	mostraErrore("Errore durante il calcolo delle retribuzioni mensili.");
            e.printStackTrace();
        }
    }
    
    /**
     * Ripulisce i campi grafici usati per il licenziamento.
     *
     * Il metodo aggiorna soltanto lo stato dei componenti della View dopo una
     * operazione coordinata dal Controller.
     */
    private void pulisciCampiLicenziamento() {
        view.getTxtIdDaLicenziare().setText("");
        view.getComboSostituto().removeAllItems();
        view.getComboSostituto().addItem("Seleziona sostituto...");
    }
    
    /**
     * Mostra un esito positivo nella label di stato e tramite popup.
     *
     * @param messaggio messaggio da mostrare
     */
    private void mostraSuccesso(String messaggio) {
        view.getLblStato().setText(messaggio);
        view.mostraMessaggioInformativo(messaggio);
    }

    /**
     * Mostra un errore nella label di stato e tramite popup.
     *
     * @param messaggio messaggio da mostrare
     */
    private void mostraErrore(String messaggio) {
        view.getLblStato().setText(messaggio);
        view.mostraMessaggioErrore(messaggio);
    }
}