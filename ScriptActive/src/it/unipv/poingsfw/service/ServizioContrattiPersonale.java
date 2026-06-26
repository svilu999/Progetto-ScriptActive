package it.unipv.poingsfw.service;

import java.util.List;

import it.unipv.poingsfw.domain.PersonalTrainer;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;
import it.unipv.poingsfw.exceptions.TrainerNonLicenziabileException;

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
            String idTrainer,
            String specializzazione,
            String tipoRetribuzione,
            double importoRetribuzione)
            throws TrainerGiaAssuntoException, TrainerNonValidoException;
    
    /**
     * Restituisce l'elenco dei Personal Trainer presenti nel sistema.
     *
     * Il controller userà questi dati per aggiornare la tabella della View,
     * senza permettere alla View di accedere direttamente al Model o al DAO.
     *
     * @return lista dei Personal Trainer presenti nel sistema
     */
    List<PersonalTrainer> getElencoPersonalTrainer();
    
    /**
     * Restituisce i Personal Trainer compatibili come sostituti.
     *
     * Un sostituto è considerato compatibile se è attivo, non coincide con il
     * trainer da licenziare e possiede la stessa specializzazione.
     *
     * @param idTrainerDaLicenziare identificativo del trainer da sostituire
     * @return lista dei Personal Trainer compatibili
     * @throws TrainerNonValidoException se il trainer da licenziare non esiste o non è valido
     */
    List<PersonalTrainer> getSostitutiCompatibili(String idTrainerDaLicenziare)
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