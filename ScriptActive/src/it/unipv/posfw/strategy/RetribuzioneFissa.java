package it.unipv.posfw.strategy;

/**
 * Strategia concreta per la retribuzione fissa mensile.
 *
 * La classe implementa StrategiaRetribuzione restituendo sempre lo stesso
 * importo mensile, indipendentemente dal numero di lezioni svolte.
 */
public class RetribuzioneFissa implements StrategiaRetribuzione {

    private double stipendioBase;

    /**
     * Costruisce una strategia di retribuzione fissa.
     *
     * @param stipendioBase importo mensile fisso da riconoscere al PersonalTrainer
     */
    public RetribuzioneFissa(double stipendioBase) {
        this.stipendioBase = stipendioBase;
    }

    /**
     * Restituisce lo stipendio fisso mensile.
     *
     * Il parametro numeroLezioni non modifica il risultato, perché questa
     * strategia non dipende dal numero di lezioni svolte.
     *
     * @param numeroLezioni numero di lezioni, non usato in questa strategia
     * @return stipendio fisso mensile
     */
    @Override
    public double calcolaStipendio(int numeroLezioni) {
        return stipendioBase;
    }

    /**
     * Restituisce il nome del tipo di contratto associato alla retribuzione fissa.
     *
     * @return tipo di contratto
     */
    @Override
    public String getTipoContratto() {
        return "Fisso";
    }
}
