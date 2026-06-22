package it.unipv.poingsfw.dao;

import java.util.List;

import it.unipv.poingsfw.domain.Corso;

public interface PrenotazioneDAO {
    boolean esistePrenotazione(String idCliente, String idCorso);
    boolean inserisciPrenotazione(String idCliente, String idCorso);
    boolean eliminaPrenotazione(String idCliente, String idCorso);
    

    boolean inserisciPrenotazione(String idCliente, String idCorso, String stato);
    String getPrimoInListaAttesa(String idCorso);
    boolean aggiornaStatoPrenotazione(String idCliente, String idCorso, String nuovoStato);
    
    // ========================================================
    // NUOVO METODO AGGIUNTO PER LA DASHBOARD CLIENTE
    // ========================================================
    List<Corso> getCorsiPerCliente(String idCliente);
}