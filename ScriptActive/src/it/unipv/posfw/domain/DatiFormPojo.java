package it.unipv.posfw.domain;

/**
 * La classe {@code DatiFormPojo} modella un'entità debole (o <i>Value Object</i>) che incapsula i parametri 
 * quantitativi di una singola attività fisica.
 * <p>
 * <b>Nota Architetturale (Code Smell: Nomi Inconsistenti):</b><br>
 * Hai intuito bene: il suffisso "Pojo" (Plain Old Java Object) è un dettaglio implementativo e vìola le regole 
 * di Ubiquitous Language del dominio, costituendo un <i>Code Smell</i> per Nomenclatura Inconsistente/Ambigua. 
 * Un nome corretto sarebbe stato {@code DettaglioEsercizio} o {@code EsercizioDTO}. Tuttavia, in stretta aderenza 
 * al vincolo di <b>Invarianza della Struttura</b> (per non rompere l'integrazione con il codice dei colleghi), 
 * il nome della classe <u>non è stato modificato</u> in questa sede.
 * </p>
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Supporta lo <b>Use Case UC4: Registrazione e Monitoraggio Prestazioni</b> agendo come <i>Data Transfer Object</i>. 
 * Raccoglie i dati temporanei dal form della Vista (carichi, ripetizioni) per veicolarli in sicurezza al Controller.
 * </p>
 * * @author Vilucchi
 * @version 1.1
 */
public class DatiFormPojo {
    
    private String nomeEsercizio;
    private double carichi;
    private int ripetizioni;

    /**
     * Costruttore completo per l'inizializzazione dello stato interno.
     * <p>
     * Rispetta il principio di <b>Incapsulamento</b>: lo stato viene popolato unicamente al momento 
     * dell'istanziazione, evitando l'esposizione di metodi mutator (setter) superflui e garantendo 
     * l'immutabilità dei dati raccolti dalla View.
     * </p>
     * * @param nomeEsercizio La denominazione della macchina o dell'attività muscolare.
     * @param carichi Il peso (in kg) sollevato durante la serie.
     * @param ripetizioni Il numero di esecuzioni per singola serie.
     */
    public DatiFormPojo(String nomeEsercizio, double carichi, int ripetizioni) {
        this.nomeEsercizio = nomeEsercizio;
        this.carichi = carichi;
        this.ripetizioni = ripetizioni;
    }

    /**
     * Recupera la denominazione dell'esercizio fisico.
     * * @return La stringa identificativa dell'esercizio.
     */
    public String getNomeEsercizio() { 
        return nomeEsercizio; 
    }

    /**
     * Recupera il parametro quantitativo del carico.
     * * @return Il valore del carico in chilogrammi.
     */
    public double getCarichi() { 
        return carichi; 
    }

    /**
     * Recupera il parametro quantitativo delle ripetizioni.
     * * @return Il conteggio delle ripetizioni effettuate.
     */
    public int getRipetizioni() { 
        return ripetizioni; 
    }
}