package it.unipv.poingsfw.service;

import java.util.List;

import it.unipv.poingsfw.dto.DatiVisualizzazioneTrainer;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;

/**
 * Servizio applicativo per la gestione dei contratti del personale.
 *
 * L'interfaccia espone al Controller le operazioni del caso d'uso senza
 * mostrare i dettagli interni del Model, come DAO, database, oggetti di
 * dominio e strategie retributive.
 */
public interface ServizioContrattiPersonale {

    /**
     * Gestisce l'assunzione di un nuovo Personal Trainer.
     *
     * La creazione degli oggetti di dominio, la scelta della strategia
     * retributiva e il salvataggio sono responsabilità del Model.
     *
     * @param nome nome del Personal Trainer
     * @param cognome cognome del Personal Trainer
     * @param email email del Personal Trainer
     * @param specializzazione specializzazione professionale
     * @param tipoRetribuzione tipo di retribuzione selezionato
     * @param importoRetribuzione importo associato alla retribuzione
     * @throws TrainerGiaAssuntoException se il trainer risulta già assunto
     * @throws TrainerNonValidoException se i dati ricevuti non sono validi
     */
    void assumiPersonalTrainer(
            String nome,
            String cognome,
            String email,
            String specializzazione,
            String tipoRetribuzione,
            double importoRetribuzione)
            throws TrainerGiaAssuntoException,
                   TrainerNonValidoException;

    /**
     * Restituisce i dati necessari alla visualizzazione dei Personal Trainer.
     *
     * @return lista dei dati destinati alla schermata; mai {@code null}
     */
    List<DatiVisualizzazioneTrainer> getElencoPersonalTrainer();

    /**
     * Restituisce i Personal Trainer utilizzabili come sostituti del trainer
     * indicato.
     *
     * Le regole di compatibilità sono applicate dal Model.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da sostituire
     * @return lista dei sostituti compatibili; mai {@code null}
     * @throws TrainerNonValidoException se l'identificativo non è valido
     *         oppure il trainer non esiste
     */
    List<DatiVisualizzazioneTrainer> getSostitutiCompatibili(
            String idTrainerDaLicenziare)
            throws TrainerNonValidoException;

    /**
     * Gestisce il licenziamento di un Personal Trainer senza sostituto.
     *
     * L'operazione è consentita soltanto se il trainer esiste, ha un contratto
     * attivo e non possiede corsi attivi o futuri assegnati.
     *
     * @param idTrainer identificativo del trainer da licenziare
     * @throws TrainerNonValidoException se il trainer non esiste, non è attivo
     *         oppure l'identificativo non è valido
     * @throws TrainerNonLicenziabileException se il trainer possiede corsi
     *         attivi o futuri
     */
    void licenziaPersonalTrainerSenzaSostituto(
            String idTrainer)
            throws TrainerNonValidoException,
                   TrainerNonLicenziabileException;

    /**
     * Gestisce il licenziamento di un Personal Trainer con sostituzione.
     *
     * Le verifiche sul trainer, sul sostituto, sulla compatibilità e sui
     * conflitti dei corsi sono responsabilità del Model.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da licenziare
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @throws TrainerNonValidoException se il trainer da licenziare non esiste,
     *         non è attivo oppure l'identificativo non è valido
     * @throws SostitutoNonValidoException se il sostituto non è valido,
     *         non è compatibile oppure presenta conflitti
     */
    void licenziaPersonalTrainerConSostituto(
            String idTrainerDaLicenziare,
            String idTrainerSostituto)
            throws TrainerNonValidoException,
                   SostitutoNonValidoException;

    /**
     * Calcola il totale mensile delle retribuzioni dei Personal Trainer attivi.
     *
     * @return totale mensile delle retribuzioni
     */
    double calcolaTotaleRetribuzioniMensili();
}