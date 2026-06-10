package it.unipv.posfw.database;

public interface UtenteDAO {
    // Elenco delle operazioni disponibili (CRUD)
    void registraCliente(String cf, String nome, String cognome, String email, String passwordHash);
    
    // In futuro qui aggiungerai altri metodi, ad esempio:
    // Utente login(String email, String password);
    // void eliminaUtente(int idUtente);
}