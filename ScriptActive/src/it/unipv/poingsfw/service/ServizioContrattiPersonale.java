package it.unipv.poingsfw.service;

import java.util.List;

import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.poingsfw.dto.DatiVisualizzazioneTrainer;

/**
 * Interfaccia del servizio applicativo per la gestione dei contratti del personale.
 *
 * Espone al controller le operazioni del caso d'uso UC5 senza mostrare i dettagli
 * interni del Model, come DAO, database, domain o strategy.
 */
public interface ServizioContrattiPersonale {

    /**
     * Gestisce l'assunzione di un nuovo Personal Trainer.
     *
     * Il metodo riceve dati semplici dal controller. La creazione degli oggetti
     * di dominio, la scelta della strategia retributiva e il salvataggio vengono
     * gestiti nello strato Model.
     *
     * @param nome nome del Personal Trainer
     * @param cognome cognome del Personal Trainer
     * @param email email del Personal Trainer
     * @param idTrainer identificativo del Personal Trainer, oppure AUTO
     * @param specializzazione specializzazione professionale
     * @param tipoRetribuzione tipo di retribuzione scelto
     * @param importoRetribuzione importo associato alla retribuzione
     * @throws TrainerGiaAssuntoException se il trainer è già presente nel sistema
     * @throws TrainerNonValidoException se i dati del trainer non sono validi
     */
    void assumiPersonalTrainer(
            String nome,
            String cognome,
            String email,
            String specializzazione,
            String tipoRetribuzione,
            double importoRetribuzione)
            throws TrainerGiaAssuntoException, TrainerNonValidoException;
    
    /**
     * Restituisce i dati dei Personal Trainer necessari alla visualizzazione.
     *
     * Il Controller riceve esclusivamente un DTO di presentazione e non
     * accede direttamente agli oggetti di dominio o ai DTO di persistenza.
     *
     * @return lista dei dati destinati alla schermata di gestione personale
     */
    List<DatiVisualizzazioneTrainer> getElencoPersonalTrainer();
    
    /**
     * Restituisce i dati dei Personal Trainer compatibili con il trainer
     * indicato e utilizzabili come sostituti.
     *
     * Il Controller riceve esclusivamente DTO destinati alla presentazione
     * e non accede agli oggetti di dominio.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da sostituire
     * @return lista dei sostituti compatibili destinata alla presentazione
     * @throws TrainerNonValidoException se l'identificativo non è valido
     *         o il trainer non esiste
     */
    List<DatiVisualizzazioneTrainer> getSostitutiCompatibili(
            String idTrainerDaLicenziare)
            throws TrainerNonValidoException;
    
    /**
     * Gestisce il licenziamento di un Personal Trainer senza sostituto.
     *
     * L'operazione è consentita solo se il trainer esiste, ha un contratto attivo
     * e non possiede corsi attivi o futuri assegnati.
     *
     * @param idTrainer identificativo del Personal Trainer da licenziare
     * @throws TrainerNonValidoException se il trainer non esiste o non è valido
     * @throws TrainerNonLicenziabileException se il trainer ha corsi attivi o futuri
     */
    void licenziaPersonalTrainerSenzaSostituto(String idTrainer)
            throws TrainerNonValidoException, TrainerNonLicenziabileException;
    
    /**
     * Gestisce il licenziamento di un Personal Trainer assegnando un sostituto.
     *
     * Il metodo viene usato quando il trainer da licenziare può avere corsi attivi
     * o futuri che devono essere riassegnati a un altro Personal Trainer compatibile.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da licenziare
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @throws TrainerNonValidoException se il trainer da licenziare non esiste o non è valido
     * @throws SostitutoNonValidoException se il sostituto non è valido o non è compatibile
     */
    void licenziaPersonalTrainerConSostituto(
            String idTrainerDaLicenziare,
            String idTrainerSostituto)
            throws TrainerNonValidoException, SostitutoNonValidoException;
    
    /**
     * Calcola il totale mensile delle retribuzioni dei Personal Trainer attivi.
     *
     * Il controller usa questo metodo senza conoscere i dettagli di calcolo
     * o le query necessarie per ottenere il risultato.
     *
     * @return totale mensile delle retribuzioni dei Personal Trainer attivi
     */
    double calcolaTotaleRetribuzioniMensili();
}