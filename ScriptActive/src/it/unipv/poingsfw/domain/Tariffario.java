package it.unipv.poingsfw.domain;

/**
 * Listino prezzi degli abbonamenti.
 * <p>
 * Regola di dominio (<i>Information Expert</i>): il prezzo dipende solo dal livello
 * (durata) e dal tipo (Base/Premium) dell'abbonamento, quindi viene calcolato qui e non
 * nei controller. Registrazione e rinnovo automatico usano lo stesso listino.
 * </p>
 */
public final class Tariffario {

    /** Supplemento applicato agli abbonamenti Premium, in Euro. */
    public static final double SUPPLEMENTO_PREMIUM = 15.0;

    private Tariffario() {
    }

    /**
     * Calcola l'importo da addebitare per un abbonamento.
     *
     * @param tipo    Base o Premium ({@code null} è trattato come Base)
     * @param livello durata dell'abbonamento
     * @return l'importo in Euro; 0 se il livello non è specificato
     */
    public static double calcolaImporto(TipoAbbonamento tipo, LivelloAbbonamento livello) {
        double importo = 0.0;
        if (livello == LivelloAbbonamento.MENSILE) {
            importo = 50.0;
        } else if (livello == LivelloAbbonamento.SEMESTRALE) {
            importo = 250.0;
        } else if (livello == LivelloAbbonamento.ANNUALE) {
            importo = 450.0;
        }

        if (tipo == TipoAbbonamento.PREMIUM) {
            importo += SUPPLEMENTO_PREMIUM;
        }
        return importo;
    }
}
