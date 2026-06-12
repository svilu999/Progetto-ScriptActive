package it.unipv.posfw.database; // Assicurati che il package sia lo stesso di UtenteDAOMySQL!

import it.unipv.posfw.domain.Utente;

public interface UtenteDAO {
    
    // Metodo per il login
    Utente effettuaLogin(String email, String password);
    
    // Metodo per la registrazione
    void registraCliente(String cf, String nome, String cognome, String email, String passwordHash);

}