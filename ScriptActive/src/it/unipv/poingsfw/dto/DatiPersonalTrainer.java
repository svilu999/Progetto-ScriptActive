package it.unipv.poingsfw.dto;

/**
 * DTO usato per trasferire i dati persistenti di un Personal Trainer
 * tra Service e DAO.
 *
 * Non contiene logica applicativa, logica di dominio, accesso al database
 * o riferimenti a Strategy.
 */
public class DatiPersonalTrainer {

    private final Integer idTrainer;
    private final String codiceFiscale;
    private final String nome;
    private final String cognome;
    private final String email;
    private final String passwordHash;
    private final String statoUtente;

    private final String specializzazione;
    private final String tipoContratto;
    private final String statoContratto;
    private final Boolean attivo;
    private final String tipoRetribuzione;
    private final Double stipendioMensile;
    private final Double compensoPerLezione;
    private final Integer idDirettore;

    /**
     * Crea un DTO con i dati persistenti del Personal Trainer.
     *
     * @param idTrainer identificativo del trainer
     * @param codiceFiscale codice fiscale o codice tecnico associato all'utente
     * @param nome nome del trainer
     * @param cognome cognome del trainer
     * @param email email del trainer
     * @param passwordHash password cifrata o valore previsto dallo schema
     * @param statoUtente stato dell'utente nella tabella Utente
     * @param specializzazione specializzazione del trainer
     * @param tipoContratto tipo contrattuale descrittivo
     * @param statoContratto stato contrattuale del trainer
     * @param attivo indica se il trainer è attivo
     * @param tipoRetribuzione tipo di retribuzione salvato nel database
     * @param stipendioMensile stipendio mensile fisso
     * @param compensoPerLezione compenso per singola lezione
     * @param idDirettore direttore associato al trainer
     */
    public DatiPersonalTrainer(
            Integer idTrainer,
            String codiceFiscale,
            String nome,
            String cognome,
            String email,
            String passwordHash,
            String statoUtente,
            String specializzazione,
            String tipoContratto,
            String statoContratto,
            boolean attivo,
            String tipoRetribuzione,
            double stipendioMensile,
            Double compensoPerLezione,
            Integer idDirettore) {

        this.idTrainer = idTrainer;
        this.codiceFiscale = codiceFiscale;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.passwordHash = passwordHash;
        this.statoUtente = statoUtente;
        this.specializzazione = specializzazione;
        this.tipoContratto = tipoContratto;
        this.statoContratto = statoContratto;
        this.attivo = attivo;
        this.tipoRetribuzione = tipoRetribuzione;
        this.stipendioMensile = stipendioMensile;
        this.compensoPerLezione = compensoPerLezione;
        this.idDirettore = idDirettore;
    }

    public Integer getIdTrainer() {
        return idTrainer;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getStatoUtente() {
        return statoUtente;
    }

    public String getSpecializzazione() {
        return specializzazione;
    }

    public String getTipoContratto() {
        return tipoContratto;
    }

    public String getStatoContratto() {
        return statoContratto;
    }

    public boolean isAttivo() {
        return attivo;
    }

    public String getTipoRetribuzione() {
        return tipoRetribuzione;
    }

    public double getStipendioMensile() {
        return stipendioMensile;
    }

    public Double getCompensoPerLezione() {
        return compensoPerLezione;
    }

    public Integer getIdDirettore() {
        return idDirettore;
    }
}