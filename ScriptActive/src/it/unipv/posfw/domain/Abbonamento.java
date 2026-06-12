package it.unipv.posfw.domain;

import java.util.Date;

public class Abbonamento {

    private int idAbbonamento;
    private String codiceFiscale;
    private LivelloAbbonamento livello;    // SOSTITUITO: Mese / Semestre / Anno
    private TipoAbbonamento tipo;          // Base / Premium 
    private Date dataScadenza;             
    private boolean rinnovoAutomatico;     
    private String iban;

    // Costruttore aggiornato
    public Abbonamento(String codiceFiscale, LivelloAbbonamento livello, TipoAbbonamento tipo, 
                       boolean rinnovoAutomatico, String iban) {
        this.codiceFiscale = codiceFiscale;
        this.livello = livello;
        this.tipo = tipo;
        this.rinnovoAutomatico = rinnovoAutomatico;
        this.iban = iban;
    }

    public int getIdAbbonamento() { 
        return idAbbonamento; 
    }
    public void setIdAbbonamento(int idAbbonamento) { 
        this.idAbbonamento = idAbbonamento; 
    }

    public String getCodiceFiscale() { 
        return codiceFiscale; 
    }
    public void setCodiceFiscale(String codiceFiscale) { 
        this.codiceFiscale = codiceFiscale; 
    }

    // Nuovi Getter e Setter per il Livello
    public LivelloAbbonamento getLivello() {
        return livello; 
    }
    public void setLivello(LivelloAbbonamento livello) { 
        this.livello = livello; 
    }

    public TipoAbbonamento getTipo() {
        return tipo; 
    }
    public void setTipo(TipoAbbonamento tipo) { 
        this.tipo = tipo; 
    }

    public Date getDataScadenza() {
        return dataScadenza; 
    }
    public void setDataScadenza(Date dataScadenza) {
        this.dataScadenza = dataScadenza; 
    }

    public boolean isRinnovoAutomatico() {
        return rinnovoAutomatico; 
    }
    public void setRinnovoAutomatico(boolean rinnovoAutomatico) {
        this.rinnovoAutomatico = rinnovoAutomatico; 
    }

    public String getIban() { 
        return iban; 
    }
    public void setIban(String iban) { 
        this.iban = iban; 
    }
}