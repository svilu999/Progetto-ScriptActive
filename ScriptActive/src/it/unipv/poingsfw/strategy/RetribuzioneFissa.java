package it.unipv.poingsfw.strategy;

/**
 * Strategy per il calcolo della retribuzione fissa mensile.
 *
 * La retribuzione non dipende dal numero di lezioni completate.
 */
public class RetribuzioneFissa implements StrategiaRetribuzione {

    private static final String TIPO_RETRIBUZIONE = "FISSA_MENSILE";

    private final double stipendioBase;

    /**
     * Crea una Strategy di retribuzione fissa.
     *
     * @param stipendioBase importo mensile fisso
     * @throws IllegalArgumentException se l'importo è negativo
     */
    public RetribuzioneFissa(double stipendioBase) {
        if (stipendioBase < 0) {
            throw new IllegalArgumentException(
                    "Lo stipendio base non può essere negativo."
            );
        }

        this.stipendioBase = stipendioBase;
    }

    /**
     * Restituisce la retribuzione fissa mensile.
     *
     * Il numero di lezioni non modifica il risultato.
     *
     * @param numeroLezioni numero di lezioni, non utilizzato
     * @return stipendio fisso mensile
     */
    @Override
    public double calcolaStipendio(int numeroLezioni) {
        return stipendioBase;
    }

    /**
     * Restituisce il tipo di retribuzione rappresentato dalla Strategy.
     *
     * @return tipo di retribuzione fissa mensile
     */
    @Override
    public String getTipoRetribuzione() {
        return TIPO_RETRIBUZIONE;
    }
}