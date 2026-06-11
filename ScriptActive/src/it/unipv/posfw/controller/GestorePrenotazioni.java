package it.unipv.posfw.controller;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.exceptions.CorsoAlCompletoException;
import it.unipv.posfw.exceptions.PrenotazioneGiaEffettuataException;
import it.unipv.posfw.dao.PrenotazioneDAO;
import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.database.PrenotazioneDAOMySQL;
import it.unipv.posfw.database.CorsoDAOMySQL;

public class GestorePrenotazioni {

    private PrenotazioneDAO prenotazioneDAO;
    private CorsoDAO corsoDAO;

    public GestorePrenotazioni() {
        // Inizializziamo le classi concrete che parlano con MySQL
        this.prenotazioneDAO = new PrenotazioneDAOMySQL();
        this.corsoDAO = new CorsoDAOMySQL();
    }

    /**
     * Tenta di prenotare un corso per un cliente.
     */
    public void prenotaCorso(Cliente cliente, Corso corso) throws CorsoAlCompletoException, PrenotazioneGiaEffettuataException {
        
        // 1. Controllo di Dominio: Il corso è già pieno?
        if (corso.alCompleto()) {
            throw new CorsoAlCompletoException("Spiacenti, il corso " + corso.getNome() + " ha raggiunto la capienza massima.");
        }

        // 2. Controllo DAO: Il cliente è già iscritto a questo specifico corso?
        if (prenotazioneDAO.esistePrenotazione(cliente.getIdCliente(), corso.getIdCorso())) {
            throw new PrenotazioneGiaEffettuataException("Sei già prenotato a questo corso!");
        }

        // 3. Modifica nel Dominio e Pattern Observer
        corso.decrementaPosti();
        corso.attach(cliente); 

        // 4. Salvataggio su Database (Persistenza)
        prenotazioneDAO.inserisciPrenotazione(cliente.getIdCliente(), corso.getIdCorso());
        corsoDAO.updatePostiDisponibili(corso); // Aggiorna i posti su MySQL
        
        System.out.println("Prenotazione effettuata con successo! Posti rimanenti: " + corso.getPostiDisponibili()); 
    }

    /**
     * Annulla una prenotazione esistente.
     */
    public void annullaPrenotazione(Cliente cliente, Corso corso) {
        
        // 1. Modifica nel Dominio e Pattern Observer
        corso.incrementaPosti();
        corso.detach(cliente);

        // 2. Aggiornamento su Database (Persistenza)
        prenotazioneDAO.eliminaPrenotazione(cliente.getIdCliente(), corso.getIdCorso());
        corsoDAO.updatePostiDisponibili(corso); // Aggiorna i posti su MySQL

        System.out.println("Prenotazione annullata con successo. Posti rimanenti: " + corso.getPostiDisponibili());
    }
}