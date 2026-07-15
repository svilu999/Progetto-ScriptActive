package it.unipv.poingsfw.dto;

/**
 * DTO usato per trasferire al Controller i dati di un Personal Trainer
 * necessari alla visualizzazione.
 *
 * La classe non espone oggetti di dominio né informazioni tecniche
 * di persistenza. Contiene esclusivamente i valori utilizzati dalla
 * schermata di gestione del personale.
 */
public class DatiVisualizzazioneTrainer {

    private final String idTrainer;
    private final String nomeCompleto;
    private final String email;
    private final String specializzazione;
    private final String statoContratto;
    private final boolean attivo;

    /**
     * Crea un DTO con i dati necessari alla presentazione del trainer.
     *
     * @param idTrainer identificativo del Personal Trainer
     * @param nomeCompleto nome e cognome del Personal Trainer
     * @param email email del Personal Trainer
     * @param specializzazione specializzazione professionale
     * @param statoContratto stato attuale del contratto
     * @param attivo indica se il Personal Trainer è attivo
     */
    public DatiVisualizzazioneTrainer(
            String idTrainer,
            String nomeCompleto,
            String email,
            String specializzazione,
            String statoContratto,
            boolean attivo) {

        this.idTrainer = idTrainer;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.specializzazione = specializzazione;
        this.statoContratto = statoContratto;
        this.attivo = attivo;
    }

    public String getIdTrainer() {
        return idTrainer;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public String getSpecializzazione() {
        return specializzazione;
    }

    public String getStatoContratto() {
        return statoContratto;
    }

    public boolean isAttivo() {
        return attivo;
    }
}