package it.unipv.posfw.dao;

import java.util.List; // IMPORTANTE
import it.unipv.posfw.domain.Corso; // IMPORTANTE

public interface PrenotazioneDAO {
    boolean esistePrenotazione(String idCliente, String idCorso);
    boolean inserisciPrenotazione(String idCliente, String idCorso);
    boolean eliminaPrenotazione(String idCliente, String idCorso);
    
    // NUOVI METODI PER BLOCCO 2
    boolean inserisciPrenotazione(String idCliente, String idCorso, String stato); // Sovraccarico del metodo
    String getPrimoInListaAttesa(String idCorso);
    boolean aggiornaStatoPrenotazione(String idCliente, String idCorso, String nuovoStato);
    
    // ========================================================
    // NUOVO METODO AGGIUNTO PER LA DASHBOARD CLIENTE
    // ========================================================
    List<Corso> getCorsiPerCliente(String idCliente);
}