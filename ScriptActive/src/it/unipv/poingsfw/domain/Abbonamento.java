package it.unipv.poingsfw.domain;

import java.util.Date;

public class Abbonamento {

    private int idAbbonamento;
    private String codiceFiscale;
    private LivelloAbbonamento livello;    // Mese / Semestre / Anno
    private TipoAbbonamento tipo;          // Base / Premium 
    private Date dataScadenza;             
    private boolean rinnovoAutomatico;     
    private String iban;

    // Costruttore
    public Abbonamento(String codiceFiscale, LivelloAbbonamento livello, TipoAbbonamento tipo, 
                       boolean rinnovoAutomatico, String iban) {
        this.codiceFiscale = codiceFiscale;
        this.livello = livello;
        this.tipo = tipo;
        this.rinnovoAutomatico = rinnovoAutomatico;
        this.iban = iban;
    }

    // --- GETTER E SETTER ---

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

    // --- METODI DI LOGICA DI BUSINESS ---

    /**
     * Controlla se l'abbonamento è ancora in corso di validità rispetto alla data odierna.
     * @return true se valido, false se scaduto.
     */
    public boolean isValidoOggi() {
        // Controllo di sicurezza: se la data manca nel database, l'abbonamento non è valido
        if (this.dataScadenza == null) {
            return false; 
        }
        
        // Prendiamo la data e l'ora esatta di questo momento
        Date oggi = new Date();
        
        // Il metodo .after() restituisce true solo se la scadenza viene DOPO oggi
        return this.dataScadenza.after(oggi);
    }
}