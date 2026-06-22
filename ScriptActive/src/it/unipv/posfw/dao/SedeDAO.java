package it.unipv.posfw.dao;

import java.util.List;
import it.unipv.posfw.domain.Sede;

/**
 * Interfaccia DAO (Data Access Object) per l'entità Sede.
 * Separa la logica di business dall'implementazione fisica del database.
 */
public interface SedeDAO {
    
    void aggiungiSede(String nomeSede);
    
    void stampaTutteLeSedi();
    
    List<Sede> getTutteLeSedi();

}
