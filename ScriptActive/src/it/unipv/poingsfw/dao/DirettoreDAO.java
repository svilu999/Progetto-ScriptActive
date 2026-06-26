package it.unipv.poingsfw.dao;

/**
 * DAO dedicato al recupero dei dati persistenti relativi al Direttore.
 *
 * L'interfaccia espone solo operazioni di lettura necessarie ai Service.
 */
public interface DirettoreDAO {

    /**
     * Recupera dal database un identificativo valido di Direttore.
     *
     * @return identificativo del Direttore, oppure null se non esiste
     */
    Integer trovaIdDirettoreDisponibile();
}