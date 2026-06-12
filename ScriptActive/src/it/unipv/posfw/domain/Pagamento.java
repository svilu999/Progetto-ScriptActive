package it.unipv.posfw.domain;

import java.util.Date;

public class Pagamento {

    private int idPagamento;
    private double importo;
    private Date dataTransazione;
    private String esito;
    private String iban;

    public Pagamento(double importo, String esito, String iban) {
        this.importo = importo;
        this.esito = esito;
        this.iban = iban;
        this.dataTransazione = new Date(); 
    }

    
    public int getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(int idPagamento) {
        this.idPagamento = idPagamento;
    }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public Date getDataTransazione() {
        return dataTransazione;
    }

    public void setDataTransazione(Date dataTransazione) {
        this.dataTransazione = dataTransazione;
    }

    public String getEsito() {
        return esito;
    }

    public void setEsito(String esito) {
        this.esito = esito;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }
}
