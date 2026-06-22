package it.unipv.poingsfw.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entità di dominio {@code SessioneAllenamento} che assume il ruolo architetturale di <b>Aggregate Root</b>.
 * <p>
 * Incapsula i dati temporali e identificativi di una seduta di allenamento, gestendo attivamente 
 * il ciclo di vita della collezione dei singoli esercizi (le entità deboli/DTO {@link DatiFormPojo}).
 * </p>
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Rappresenta il concetto centrale dello <b>Use Case UC4: Registrazione e Monitoraggio Prestazioni</b>. 
 * Costituisce l'aggregato popolato dalla Vista, validato dal Controller e reso persistente 
 * dal DAO per materializzare la postcondizione dello storico attività.
 * </p>
 *
 * @author Vilucchi
 * @version 1.1
 */
public class SessioneAllenamento {
    
    private int idSessione; 
    private Date data;
    private String idCliente;
    private List<DatiFormPojo> esercizi;

    /**
     * Costruttore dell'entità.
     * <p>
     * Inizializza l'aggregato in uno stato <i>transient</i> (non ancora persistito). 
     * Alloca dinamicamente la memoria per la collezione interna per prevenire {@code NullPointerException}.
     * </p>
     *
     * @param data      La data di esecuzione della sessione.
     * @param idCliente Il riferimento (Foreign Key logica) all'attore proprietario della sessione.
     */
    public SessioneAllenamento(Date data, String idCliente) {
        /* Stato transitorio: -1 indica che l'identità (Primary Key) non è ancora stata assegnata dal DBMS */
        this.idSessione = -1;
        this.data = data;
        this.idCliente = idCliente;
        this.esercizi = new ArrayList<>();
    }

    /**
     * Imposta la chiave surrogata dell'entità.
     * <p>
     * Metodo invocato dal livello di persistenza (DAO) a seguito dell'inserimento nel database 
     * per allineare l'oggetto in memoria con l'ID auto-generato dal sistema relazionale.
     * </p>
     *
     * @param idSessione L'identificativo numerico univoco.
     */
    public void setIdSessione(int idSessione) {
        this.idSessione = idSessione;
    }

    /**
     * Recupera l'identificativo univoco della sessione.
     *
     * @return L'ID della sessione, oppure -1 se l'entità è in stato <i>transient</i>.
     */
    public int getIdSessione() {
        return idSessione;
    }

    /**
     * Aggiunge un nuovo esercizio alla sessione corrente.
     * <p>
     * Rafforza il pattern <b>Aggregate Root</b>: i componenti interni (esercizi) vengono 
     * manipolati esclusivamente attraverso l'interfaccia pubblica della radice dell'aggregato.
     * </p>
     *
     * @param esercizio L'oggetto contenente i parametri quantitativi (carichi, ripetizioni) da accodare.
     */
    public void aggiungiEsercizio(DatiFormPojo esercizio) {
        this.esercizi.add(esercizio);
    }

    /**
     * Recupera la coordinata temporale della sessione.
     *
     * @return La data di svolgimento dell'allenamento.
     */
    public Date getData() { 
        return data; 
    }

    /**
     * Recupera l'identificativo del cliente associato alla sessione.
     *
     * @return La stringa identificativa del cliente.
     */
    public String getIdCliente() { 
        return idCliente; 
    }

    /**
     * Recupera la collezione strutturata degli esercizi registrati.
     *
     * @return La lista degli oggetti {@link DatiFormPojo} facenti parte dell'aggregato.
     */
    public List<DatiFormPojo> getEsercizi() { 
        return esercizi; 
    }
}