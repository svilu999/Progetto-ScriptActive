package it.unipv.posfw.domain;

public class Sede {
    
    private String idSede;
    private String nomeSede;

    public Sede() {
    }

   
    public Sede(String idSede, String nomeSede) {
        this.idSede = idSede;       
        this.nomeSede = nomeSede;   
    }


    
    public String getIdSede() {
        return idSede;
    }

    public void setIdSede(String idSede) {
        this.idSede = idSede;
    }

    public String getNomeSede() {
        return nomeSede;
    }

    public void setNomeSede(String nomeSede) {
        this.nomeSede = nomeSede;
    }
}
