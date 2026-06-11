package it.unipv.posfw.dao;

public interface PrenotazioneDAO {
    
    // Controlla se un cliente è già iscritto a un determinato corso
    boolean esistePrenotazione(String idCliente, String idCorso);
    
    // Inserisce una nuova prenotazione nel DB
    boolean inserisciPrenotazione(String idCliente, String idCorso);
    
    // Cancella una prenotazione dal DB
    boolean eliminaPrenotazione(String idCliente, String idCorso);
}