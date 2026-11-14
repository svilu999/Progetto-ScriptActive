package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.dao.PrenotazioneDAO;
import it.unipv.poingsfw.domain.Corso;

/**
 * Implementazione in memoria di {@link PrenotazioneDAO} per i test di unità.
 * Le prenotazioni sono conservate nell'ordine di inserimento, così la lista
 * d'attesa rispetta l'ordine di arrivo come nel database.
 */
public class PrenotazioneDAOInMemoria implements PrenotazioneDAO {

    private static final String STATO_CONFERMATA = "Confermata";
    private static final String STATO_LISTA_ATTESA = "InListaAttesa";

    private static final class Riga {
        private final String idCliente;
        private final String idCorso;
        private String stato;

        private Riga(String idCliente, String idCorso, String stato) {
            this.idCliente = idCliente;
            this.idCorso = idCorso;
            this.stato = stato;
        }
    }

    private final List<Riga> righe = new ArrayList<>();
    private boolean inserimentoFallisce;

    /** Se {@code true}, gli inserimenti falliscono come se il database li rifiutasse. */
    public void setInserimentoFallisce(boolean inserimentoFallisce) {
        this.inserimentoFallisce = inserimentoFallisce;
    }

    /** @return lo stato della prenotazione, oppure {@code null} se non esiste */
    public String getStato(String idCliente, String idCorso) {
        Riga riga = trova(idCliente, idCorso);
        return riga == null ? null : riga.stato;
    }

    private Riga trova(String idCliente, String idCorso) {
        for (Riga riga : righe) {
            if (riga.idCliente.equals(idCliente) && riga.idCorso.equals(idCorso)) {
                return riga;
            }
        }
        return null;
    }

    @Override
    public boolean esistePrenotazione(String idCliente, String idCorso) {
        return trova(idCliente, idCorso) != null;
    }

    @Override
    public boolean inserisciPrenotazione(String idCliente, String idCorso) {
        return inserisciPrenotazione(idCliente, idCorso, STATO_CONFERMATA);
    }

    @Override
    public boolean inserisciPrenotazione(String idCliente, String idCorso, String stato) {
        if (inserimentoFallisce) {
            return false;
        }
        righe.add(new Riga(idCliente, idCorso, stato));
        return true;
    }

    @Override
    public boolean eliminaPrenotazione(String idCliente, String idCorso) {
        Riga riga = trova(idCliente, idCorso);
        return riga != null && righe.remove(riga);
    }

    @Override
    public String getPrimoInListaAttesa(String idCorso) {
        for (Riga riga : righe) {
            if (riga.idCorso.equals(idCorso) && STATO_LISTA_ATTESA.equals(riga.stato)) {
                return riga.idCliente;
            }
        }
        return null;
    }

    @Override
    public boolean aggiornaStatoPrenotazione(String idCliente, String idCorso, String nuovoStato) {
        Riga riga = trova(idCliente, idCorso);
        if (riga == null) {
            return false;
        }
        riga.stato = nuovoStato;
        return true;
    }

    @Override
    public List<Corso> getCorsiPerCliente(String idCliente) {
        // I test che ne hanno bisogno forniscono un elenco tramite sottoclasse.
        return new ArrayList<>();
    }
}
