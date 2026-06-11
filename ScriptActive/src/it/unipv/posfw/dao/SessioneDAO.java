package it.unipv.posfw.dao;

import java.util.List;

import provaview.SessioneAllenamento;

public interface SessioneDAO {
    boolean salvaSessione(SessioneAllenamento sessione);
    List<SessioneAllenamento> getStorico(String idCliente);
    boolean eliminaSessioneSpecifica(SessioneAllenamento sessione);
}