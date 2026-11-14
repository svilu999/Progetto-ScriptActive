package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.unipv.poingsfw.dao.ClienteDAO;
import it.unipv.poingsfw.domain.Abbonamento;
import it.unipv.poingsfw.domain.Cliente;

/**
 * Implementazione in memoria di {@link ClienteDAO} per i test di unità.
 */
public class ClienteDAOInMemoria implements ClienteDAO {

    private final Map<String, Cliente> clienti = new LinkedHashMap<>();
    private final List<Abbonamento> abbonamentiDaRinnovare = new ArrayList<>();
    private final Map<Integer, Date> scadenzeAggiornate = new HashMap<>();
    private final List<Integer> rinnoviDisattivati = new ArrayList<>();
    private boolean inserimentoFallisce;

    /** Se {@code true}, {@link #inserisciCliente(Cliente)} fallisce come se il database lo rifiutasse. */
    public void setInserimentoFallisce(boolean inserimentoFallisce) {
        this.inserimentoFallisce = inserimentoFallisce;
    }

    /** Aggiunge un abbonamento scaduto con rinnovo automatico attivo. */
    public void aggiungiAbbonamentoDaRinnovare(Abbonamento abbonamento) {
        abbonamentiDaRinnovare.add(abbonamento);
    }

    public int getNumeroClienti() {
        return clienti.size();
    }

    public Map<Integer, Date> getScadenzeAggiornate() {
        return scadenzeAggiornate;
    }

    public List<Integer> getRinnoviDisattivati() {
        return rinnoviDisattivati;
    }

    @Override
    public boolean inserisciCliente(Cliente c) {
        if (inserimentoFallisce) {
            return false;
        }
        clienti.put(c.getCodiceFiscale(), c);
        return true;
    }

    @Override
    public Cliente getClienteByCF(String codiceFiscale) {
        return clienti.get(codiceFiscale);
    }

    @Override
    public void updateCliente(Cliente c) {
        clienti.put(c.getCodiceFiscale(), c);
    }

    @Override
    public void deleteCliente(String codiceFiscale) {
        clienti.remove(codiceFiscale);
    }

    @Override
    public List<Abbonamento> getAbbonamentiInScadenzaConRinnovo() {
        return new ArrayList<>(abbonamentiDaRinnovare);
    }

    @Override
    public void aggiornaScadenzaAbbonamento(int idAbbonamento, Date nuovaData) {
        scadenzeAggiornate.put(idAbbonamento, nuovaData);
    }

    @Override
    public void disattivaRinnovoAutomatico(int idAbbonamento) {
        rinnoviDisattivati.add(idAbbonamento);
    }
}
