package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unipv.poingsfw.dao.CorsoDAO;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.domain.StatoCorso;

/**
 * Implementazione in memoria di {@link CorsoDAO} per i test di unità.
 * <p>
 * Riproduce il contratto dell'implementazione MySQL su cui i controller fanno
 * affidamento: {@code insert} crea oppure aggiorna il corso con lo stesso ID,
 * assegna un ID progressivo ai corsi che non ne hanno ancora uno e,
 * se è stato indicato l'elenco dei trainer esistenti, rifiuta i corsi assegnati a
 * un trainer sconosciuto con lo stesso tipo di errore prodotto dal vincolo di
 * chiave esterna del database.
 * </p>
 */
public class CorsoDAOInMemoria implements CorsoDAO {

    private final Map<String, Corso> corsi = new LinkedHashMap<>();
    private final Set<String> idTrainerEsistenti;
    private int aggiornamentiPosti;
    private int prossimoId = 1;

    /** Nessun controllo sull'esistenza del trainer. */
    public CorsoDAOInMemoria() {
        this.idTrainerEsistenti = null;
    }

    /**
     * @param idTrainerEsistenti ID numerici dei trainer presenti (es. "10"); i corsi
     *                           assegnati ad altri trainer vengono rifiutati
     */
    public CorsoDAOInMemoria(Set<String> idTrainerEsistenti) {
        this.idTrainerEsistenti = idTrainerEsistenti;
    }

    /** @return quante volte è stato chiesto l'aggiornamento dei posti disponibili */
    public int getAggiornamentiPosti() {
        return aggiornamentiPosti;
    }

    @Override
    public void insert(Corso c) {
        if (idTrainerEsistenti != null && c.getTrainerAssegnato() != null) {
            String idNumerico = c.getTrainerAssegnato().getIdTrainer().replaceAll("[^0-9]", "");
            if (!idTrainerEsistenti.contains(idNumerico)) {
                throw new RuntimeException("Errore durante il salvataggio del corso.",
                        new IllegalStateException("Cannot add or update a child row: a foreign key constraint fails"));
            }
        }
        if (c.getIdCorso() == null) {
            // Come AUTO_INCREMENT: un corso nuovo riceve il primo ID libero
            while (corsi.containsKey(String.valueOf(prossimoId))) {
                prossimoId++;
            }
            c.setIdCorso(String.valueOf(prossimoId));
        }
        corsi.put(c.getIdCorso(), c);
    }

    @Override
    public void delete(String idCorso) {
        corsi.remove(idCorso);
    }

    @Override
    public Corso findById(String idCorso) {
        return corsi.get(idCorso);
    }

    @Override
    public List<Corso> findAll() {
        return new ArrayList<>(corsi.values());
    }

    @Override
    public List<Corso> getPalinsesto() {
        List<Corso> attivi = new ArrayList<>();
        for (Corso corso : corsi.values()) {
            if (corso.getStato() == StatoCorso.ATTIVO) {
                attivi.add(corso);
            }
        }
        return attivi;
    }

    @Override
    public void updatePostiDisponibili(Corso c) {
        aggiornamentiPosti++;
        corsi.put(c.getIdCorso(), c);
    }
}
