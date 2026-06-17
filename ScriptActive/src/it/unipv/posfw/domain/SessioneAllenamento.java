package it.unipv.posfw.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class SessioneAllenamento {
    private int idSessione; 
    private Date data;
    private String idCliente;
    private List<DatiFormPojo> esercizi;

    public SessioneAllenamento(Date data, String idCliente) {
        this.idSessione = -1;
        this.data = data;
        this.idCliente = idCliente;
        this.esercizi = new ArrayList<>();
    }

    public void setIdSessione(int idSessione) {
        this.idSessione = idSessione;
    }

    public int getIdSessione() {
        return idSessione;
    }

    public void aggiungiEsercizio(DatiFormPojo esercizio) {
        this.esercizi.add(esercizio);
    }

    public Date getData() { return data; }
    public String getIdCliente() { return idCliente; }
    public List<DatiFormPojo> getEsercizi() { return esercizi; }
}