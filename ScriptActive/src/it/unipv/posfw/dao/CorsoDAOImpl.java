package it.unipv.posfw.dao;

import it.unipv.posfw.domain.Corso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CorsoDAOImpl implements CorsoDAO {

    // Database finto in RAM
    private Map<String, Corso> databaseMock = new HashMap<>();

    @Override
    public void insert(Corso c) {
        databaseMock.put(c.getIdCorso(), c);
        System.out.println("[DB-MOCK] Corso inserito/aggiornato: " + c.getIdCorso());
    }

    @Override
    public void delete(String idCorso) {
        databaseMock.remove(idCorso);
        System.out.println("[DB-MOCK] Corso eliminato: " + idCorso);
    }

    @Override
    public Corso findById(String idCorso) {
        return databaseMock.get(idCorso);
    }

    @Override
    public List<Corso> findAll() {
        return new ArrayList<>(databaseMock.values());
    }

    @Override
    public List<Corso> getPalinsesto() {
        // Essendo un mock finto, restituiamo semplicemente tutti i corsi che ha in pancia
        return findAll();
    }
    
    @Override
    public void updatePostiDisponibili(it.unipv.posfw.domain.Corso c) {
        // Metodo vuoto inserito per compatibilità con l'interfaccia. 
        // L'update reale viene fatto su DB da CorsoDAOMySQL.
    }
}