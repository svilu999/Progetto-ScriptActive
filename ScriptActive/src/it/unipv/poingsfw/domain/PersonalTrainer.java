package it.unipv.poingsfw.domain;

import it.unipv.poingsfw.strategy.StrategiaRetribuzione;

/**
 * Rappresenta un PersonalTrainer del sistema.
 *
 * La classe estende Utente e aggiunge informazioni specifiche del trainer,
 * come identificativo, specializzazione, stato contrattuale, stato di attivazione
 * e strategia di retribuzione.
 */
public class PersonalTrainer extends Utente {

    private String idTrainer;
    private String specializzazione;
    private String tipoContratto;

    private StrategiaRetribuzione strategia;
    private String statoContratto;
    private boolean isAttivo;

    /**
     * Costruisce un PersonalTrainer completo di specializzazione e strategia
     * retributiva.
     *
     * @param nome nome del PersonalTrainer
     * @param cognome cognome del PersonalTrainer
     * @param email email del PersonalTrainer
     * @param idTrainer identificativo del PersonalTrainer
     * @param specializzazione specializzazione del PersonalTrainer
     * @param strategia strategia usata per calcolare la retribuzione
     */
    public PersonalTrainer(String nome, String cognome, String email, String idTrainer, String specializzazione, StrategiaRetribuzione strategia) {
        super(nome, cognome, email);
        this.idTrainer = idTrainer;
        this.specializzazione = specializzazione;
        this.strategia = strategia;
        this.statoContratto = "ATTIVO";
        this.isAttivo = true;
    }

    /**
     * Costruisce un PersonalTrainer con i dati essenziali.
     *
     * Questo costruttore viene usato quando non sono ancora disponibili tutti i
     * dati contrattuali o retributivi. I campi mancanti vengono inizializzati con
     * valori di default per evitare riferimenti nulli non gestiti.
     *
     * @param nome nome del PersonalTrainer
     * @param cognome cognome del PersonalTrainer
     * @param email email del PersonalTrainer
     * @param idTrainer identificativo del PersonalTrainer
     */
    public PersonalTrainer(String nome, String cognome, String email, String idTrainer) {
        super(nome, cognome, email);
        this.idTrainer = idTrainer;
        this.specializzazione = "Non definita";
        this.strategia = null;
        this.statoContratto = "ATTIVO";
        this.isAttivo = true;
    }

    public String getIdTrainer() { return idTrainer; }
    public void setIdTrainer(String idTrainer) { this.idTrainer = idTrainer; }

    public String getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(String specializzazione) { this.specializzazione = specializzazione; }

    public String getTipoContratto() { return tipoContratto; }
    public void setTipoContratto(String tipoContratto) { this.tipoContratto = tipoContratto; }

    public StrategiaRetribuzione getStrategia() { return strategia; }
    public void setStrategia(StrategiaRetribuzione strategia) { this.strategia = strategia; }

    public String getStatoContratto() { return statoContratto; }
    public void setStatoContratto(String statoContratto) { this.statoContratto = statoContratto; }

    public boolean isAttivo() { return isAttivo; }
    public void setAttivo(boolean isAttivo) { this.isAttivo = isAttivo; }

    /**
     * Apre l'area riservata del PersonalTrainer tramite il LoginController.
     *
     * @param router controller incaricato di aprire la dashboard corretta
     */
    @Override
    public void accediAreaRiservata(it.unipv.poingsfw.controller.LoginController router) {
        router.apriDashboardTrainer(this);
    }

    /**
     * Calcola lo stipendio mensile usando la strategia retributiva associata.
     *
     * @param corsi numero di corsi o lezioni da considerare nel calcolo
     * @return stipendio mensile calcolato, oppure 0.0 se non è presente una strategia
     */
    public double calcolaStipendioMensile(int corsi) {
        if (strategia != null) {
            return strategia.calcolaStipendio(corsi);
        }
        return 0.0;
    }
}
