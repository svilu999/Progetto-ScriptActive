package it.unipv.poingsfw.dto;

/**
 * DTO usato per trasferire dal DAO al Service i dati necessari
 * al calcolo della retribuzione mensile di un Personal Trainer.
 *
 * La classe non contiene logica applicativa e non accede al database.
 */
public class DatiRetribuzioneTrainer {

    private final String tipoRetribuzione;
    private final double stipendioMensile;
    private final double compensoPerLezione;
    private final int numeroLezioniCompletate;

    /**
     * Crea un oggetto dati per il calcolo della retribuzione.
     *
     * @param tipoRetribuzione tipo di retribuzione del Personal Trainer
     * @param stipendioMensile importo fisso mensile
     * @param compensoPerLezione importo previsto per ogni lezione completata
     * @param numeroLezioniCompletate numero di lezioni completate nel mese corrente
     */
    public DatiRetribuzioneTrainer(
            String tipoRetribuzione,
            double stipendioMensile,
            double compensoPerLezione,
            int numeroLezioniCompletate) {

        this.tipoRetribuzione = tipoRetribuzione;
        this.stipendioMensile = stipendioMensile;
        this.compensoPerLezione = compensoPerLezione;
        this.numeroLezioniCompletate = numeroLezioniCompletate;
    }

    public String getTipoRetribuzione() {
        return tipoRetribuzione;
    }

    public double getStipendioMensile() {
        return stipendioMensile;
    }

    public double getCompensoPerLezione() {
        return compensoPerLezione;
    }

    public int getNumeroLezioniCompletate() {
        return numeroLezioniCompletate;
    }
}