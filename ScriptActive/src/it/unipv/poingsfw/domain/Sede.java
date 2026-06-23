package it.unipv.poingsfw.domain;

/**
 * La classe {@code Sede} rappresenta un'entità del dominio applicativo, modellando 
 * una sede fisica (es. filiale, centro sportivo) all'interno del sistema.
 * <p>
 * Implementata come <b>POJO (Plain Old Java Object)</b>, ha il solo compito di 
 * incapsulare e trasportare i dati identificativi della sede tra i vari livelli 
 * dell'applicazione (Database, Controller, Interfaccia Grafica).
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class Sede {
    
    private int idSede; // Cambiato da String a int
    private String nomeSede;

    // Costruttore vuoto
    public Sede() {
    }

    /**
     * Costruttore parametrico per l'inizializzazione completa dell'entità.
     * * @param idSede   L'identificativo numerico univoco della sede (Primary Key nel database).
     * @param nomeSede Il nome descrittivo della sede.
     */
    
    public Sede(int idSede, String nomeSede) { // Aggiornato anche qui
        this.idSede = idSede;       
        this.nomeSede = nomeSede;   
    }

    public int getIdSede() {
        return idSede;
    }

    public void setIdSede(int idSede) {
        this.idSede = idSede;
    }

    public String getNomeSede() {
        return nomeSede;
    }

    public void setNomeSede(String nomeSede) {
        this.nomeSede = nomeSede;
    }

    /**
     * Restituisce una rappresentazione testuale dell'oggetto.
     * <p>
     * </p>
     * * @return Il nome della sede come stringa.
     */
    @Override
    public String toString() {
        return this.nomeSede; 
    }  
}
