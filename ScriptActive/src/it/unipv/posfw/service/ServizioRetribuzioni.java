package it.unipv.posfw.service;

/**
 * Interfaccia per il calcolo delle retribuzioni del personale.
 *
 * Serve a tenere separata la logica di calcolo degli stipendi
 * dal controller GestorePersonale.
 *
 * In questo modo GestorePersonale non contiene query SQL.
 */
public interface ServizioRetribuzioni {

    /**
     * Calcola il totale mensile delle retribuzioni dei Personal Trainer attivi.
     *
     * Regole:
     * - FISSA_MENSILE: usa lo stipendio_mensile del contratto;
     * - A_LEZIONE: conta i corsi COMPLETO del mese corrente
     *   e moltiplica per compenso_per_lezione.
     *
     * @return totale retribuzioni mensili dei PT attivi
     */
    double calcolaTotaleRetribuzioniMensili();
}
