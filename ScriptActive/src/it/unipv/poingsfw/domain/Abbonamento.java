package it.unipv.poingsfw.domain;

import java.util.Date;

/**
 * La classe {@code Abbonamento} rappresenta un'entità del dominio applicativo.
 * <p>
 * Implementa il pattern <b>POJO (Plain Old Java Object) / Java Bean</b>, fungendo da 
 * contenitore per i dati relativi alla sottoscrizione di un utente. Mantiene lo stato 
 * dell'abbonamento (livello, tipo, scadenze, dati di pagamento) e incapsula le logiche 
 * di business strettamente legate al proprio stato interno.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class Abbonamento {

    private int idAbbonamento;
    private String codiceFiscale;
    private LivelloAbbonamento livello;    // Mese / Semestre / Anno
    private TipoAbbonamento tipo;          // Base / Premium 
    private Date dataScadenza;             
    private boolean rinnovoAutomatico;     
    private String iban;

    /**
     * Costruttore principale per la creazione di un nuovo abbonamento.
     * <p>
     * L'ID e la data di scadenza non sono presenti nel costruttore in quanto 
     * l'ID viene delegato all'autoincremento del database, mentre la data di scadenza 
     * viene calcolata dalla logica di business solo dopo aver verificato il pagamento.
     * </p>
     * * @param codiceFiscale     Il Codice Fiscale dell'utente intestatario.
     * @param livello           La durata della sottoscrizione (es. Mensile, Annuale).
     * @param tipo              Il tier dell'abbonamento (es. Base, Premium).
     * @param rinnovoAutomatico Flag che indica se l'utente ha autorizzato l'addebito ricorrente.
     * @param iban              Le coordinate bancarie associate per i pagamenti.
     */
    
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


    /**
     * Valida lo stato della sottoscrizione confrontando la data di scadenza 
     * con il timestamp attuale del sistema.
     * * @return {@code true} se l'abbonamento ha una data di scadenza valorizzata e successiva ad oggi; 
     * {@code false} se è scaduto o se la data non è impostata.
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