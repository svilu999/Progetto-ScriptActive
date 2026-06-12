package it.unipv.posfw.dao;

import it.unipv.posfw.domain.Utente;

public interface LoginDAO {
    // Restituisce l'Utente se le credenziali sono corrette, altrimenti null
    Utente verificaCredenziali(String email, String password);
}