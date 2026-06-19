package it.unipv.posfw.service;

/**
 * Interfaccia di servizio per il calcolo delle retribuzioni del personale.
 *
 * L'interfaccia separa GestorePersonale dalla logica concreta di calcolo
 * delle retribuzioni e dall'accesso ai dati necessari per eseguire tale calcolo.
 */
public interface ServizioRetribuzioni {

    /**
     * Calcola il totale mensile delle retribuzioni dei PersonalTrainer attivi.
     *
     * Le regole di calcolo dipendono dall'implementazione concreta. Nel sistema
     * sono previste retribuzioni fisse mensili e retribuzioni basate sul numero
     * di lezioni svolte.
     *
     * @return totale mensile delle retribuzioni dei PersonalTrainer attivi
     */
    double calcolaTotaleRetribuzioniMensili();
}
