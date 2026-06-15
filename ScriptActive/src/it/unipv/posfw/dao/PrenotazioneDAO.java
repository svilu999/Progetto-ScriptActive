package it.unipv.posfw.dao;

public interface PrenotazioneDAO {
    boolean esistePrenotazione(String idCliente, String idCorso);
    boolean inserisciPrenotazione(String idCliente, String idCorso);
    boolean eliminaPrenotazione(String idCliente, String idCorso);
    
    // NUOVI METODI PER BLOCCO 2
    boolean inserisciPrenotazione(String idCliente, String idCorso, String stato); // Sovraccarico del metodo
    String getPrimoInListaAttesa(String idCorso);
    boolean aggiornaStatoPrenotazione(String idCliente, String idCorso, String nuovoStato);
}