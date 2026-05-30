package it.unipv.posfw.dao;

import it.unipv.posfw.domain.Corso;
import java.util.List;

public interface CorsoDAO {
    void insert(Corso c);
    void delete(String idCorso);
    Corso findById(String idCorso);
    List<Corso> findAll();
}