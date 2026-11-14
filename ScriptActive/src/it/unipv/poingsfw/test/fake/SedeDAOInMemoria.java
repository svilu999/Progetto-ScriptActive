package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.dao.SedeDAO;
import it.unipv.poingsfw.domain.Sede;

/**
 * Implementazione in memoria di {@link SedeDAO} per i test di unità.
 */
public class SedeDAOInMemoria implements SedeDAO {

    private final List<Sede> sedi = new ArrayList<>();

    @Override
    public void aggiungiSede(String nomeSede) {
        sedi.add(new Sede(sedi.size() + 1, nomeSede));
    }

    @Override
    public List<Sede> getTutteLeSedi() {
        return new ArrayList<>(sedi);
    }
}
