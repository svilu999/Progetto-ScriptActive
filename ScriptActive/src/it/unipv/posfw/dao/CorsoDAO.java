package it.unipv.posfw.dao;

import it.unipv.posfw.domain.Corso;
import java.util.List;

public interface CorsoDAO {
    void insert(Corso c);
    void delete(String idCorso);
    Corso findById(String idCorso);
    List<Corso> findAll();
    
    // Il metodo per il tuo UC3: non stampa, ma restituisce la lista
    List<Corso> getPalinsesto();
    
    // NUOVO METODO AGGIUNTO PER IL TUO UC2:
    void updatePostiDisponibili(Corso c);
}