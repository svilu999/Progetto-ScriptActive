package it.unipv.posfw.dao;

public interface PrenotazioneDAO {
    boolean esistePrenotazione(String idCliente, String idCorso);
    boolean inserisciPrenotazione(String idCliente, String idCorso);
    boolean eliminaPrenotazione(String idCliente, String idCorso);
}