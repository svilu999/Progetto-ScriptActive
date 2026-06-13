package it.unipv.posfw.domain;

public class Sede {
    
    private int idSede; // Cambiato da String a int
    private String nomeSede;

    // Costruttore vuoto
    public Sede() {
    }

    // Costruttore con parametri
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

    @Override
    public String toString() {
        return this.nomeSede; 
    }  
}
