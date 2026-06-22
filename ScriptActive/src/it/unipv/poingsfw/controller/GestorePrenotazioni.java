package it.unipv.poingsfw.controller;

import it.unipv.poingsfw.dao.CorsoDAO;
import it.unipv.poingsfw.dao.PrenotazioneDAO;
import it.unipv.poingsfw.database.CorsoDAOMySQL;
import it.unipv.poingsfw.database.PrenotazioneDAOMySQL;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.exceptions.CorsoAlCompletoException;
import it.unipv.poingsfw.exceptions.PrenotazioneGiaEffettuataException;

import java.util.List;

/**
 * Controller dedicato alla gestione delle prenotazioni dei corsi.
 * Implementa la logica per l'iscrizione, la gestione della capienza 
 * massima (Lista d'Attesa) e lo swap automatico in caso di annullamento.
 * Utilizza il pattern DAO per comunicare con il database.
 * * @author Matteo
 * @version 1.0
 */

public class GestorePrenotazioni {

    private PrenotazioneDAO prenotazioneDAO;
    private CorsoDAO corsoDAO;

    public GestorePrenotazioni() {
 
        this.prenotazioneDAO = new PrenotazioneDAOMySQL();
        this.corsoDAO = new CorsoDAOMySQL();
    }

    
    /**
     * Registra un cliente a un corso specifico.
     * Se il corso ha posti disponibili, la prenotazione viene confermata scalandone la disponibilità.
     * Se il corso è al completo, il cliente viene inserito automaticamente in Lista d'Attesa.
     * * @param cliente Il cliente loggato che richiede la prenotazione.
     * @param corso Il corso a cui il cliente desidera iscriversi.
     * @throws PrenotazioneGiaEffettuataException Se il cliente risulta già iscritto o già in lista d'attesa per questo corso.
     * @throws CorsoAlCompletoException Se i posti sono esauriti (Notifica l'avvenuto inserimento in coda).
     * @throws Exception In caso di errori generici di comunicazione con il database.
     */
    
    public void prenotaCorso(Cliente cliente, Corso corso) throws Exception { 
        
        it.unipv.poingsfw.dao.PrenotazioneDAO prenotazioneDB = new it.unipv.poingsfw.database.PrenotazioneDAOMySQL();
        it.unipv.poingsfw.dao.CorsoDAO corsoDB = new it.unipv.poingsfw.database.CorsoDAOMySQL();

        String idCliente = String.valueOf(cliente.getId());
        String idCorso = corso.getIdCorso();

        if (prenotazioneDB.esistePrenotazione(idCliente, idCorso)) {
            throw new it.unipv.poingsfw.exceptions.PrenotazioneGiaEffettuataException("Sei già registrato a questo corso (o sei già in lista d'attesa)!");
        }

        if (corso.getPostiDisponibili() > 0) {
            boolean successo = prenotazioneDB.inserisciPrenotazione(idCliente, idCorso, "Confermata");
            if (successo) {
                corso.setPostiDisponibili(corso.getPostiDisponibili() - 1);
                corsoDB.updatePostiDisponibili(corso);
            } else {

                throw new Exception("Errore interno: impossibile salvare la prenotazione nel Database!");
            }
        } else {
            boolean successo = prenotazioneDB.inserisciPrenotazione(idCliente, idCorso, "InListaAttesa");
            if (successo) {
                throw new it.unipv.poingsfw.exceptions.CorsoAlCompletoException("Il corso è pieno. Sei stato inserito in LISTA D'ATTESA!");
            } else {
                throw new Exception("Errore interno: impossibile inserirti in lista d'attesa!");
            }
        }
    }
    
    
    /**
     * Annulla una prenotazione esistente e gestisce l'avanzamento automatico della coda (Swap).
     * Dopo l'eliminazione della prenotazione del cliente, il sistema verifica la presenza 
     * di utenti in Lista d'Attesa. Se presenti, il primo utente in coda viene promosso 
     * allo stato "Confermata"; altrimenti, viene liberato un posto nel corso.
     * * @param cliente Il cliente che richiede l'annullamento.
     * @param corso Il corso dal quale il cliente vuole disiscriversi.
     * @throws PrenotazioneInesistenteException Se non viene trovata alcuna prenotazione attiva per la coppia Cliente-Corso.
     * @throws Exception In caso di errori generici nel processo di cancellazione o di swap.
     */
    
    public void annullaPrenotazione(Cliente cliente, Corso corso) throws it.unipv.poingsfw.exceptions.PrenotazioneInesistenteException {
        

        it.unipv.poingsfw.dao.PrenotazioneDAO prenotazioneDB = new it.unipv.poingsfw.database.PrenotazioneDAOMySQL();
        it.unipv.poingsfw.dao.CorsoDAO corsoDB = new it.unipv.poingsfw.database.CorsoDAOMySQL();
        

        String idCliente = String.valueOf(cliente.getId());
        String idCorso = corso.getIdCorso();

 
        if (!prenotazioneDB.esistePrenotazione(idCliente, idCorso)) {
            throw new it.unipv.poingsfw.exceptions.PrenotazioneInesistenteException("Impossibile disiscriversi: non sei prenotato a questo corso!");
        }


        boolean eliminato = prenotazioneDB.eliminaPrenotazione(idCliente, idCorso);
        
        if (eliminato) {

            String idPrimoInAttesa = prenotazioneDB.getPrimoInListaAttesa(idCorso);
            
            if (idPrimoInAttesa != null) {
                prenotazioneDB.aggiornaStatoPrenotazione(idPrimoInAttesa, idCorso, "Confermata");
                System.out.println("Il posto liberato è stato assegnato automaticamente all'utente ID: " + idPrimoInAttesa);
            } else {
                corso.setPostiDisponibili(corso.getPostiDisponibili() + 1);
                corsoDB.updatePostiDisponibili(corso); 
            }
        } else {
            System.err.println("Errore durante l'eliminazione della prenotazione nel database.");
        }
    }

    // ========================================================
    // NUOVO METODO: RECUPERA I CORSI PRENOTATI DAL CLIENTE
    // ========================================================
    public List<Corso> getCorsiPrenotatiDalCliente(Cliente cliente) {
        String idCliente = String.valueOf(cliente.getId());
        
        // Chiediamo al DAO delle prenotazioni (istanziato nel costruttore) di pescare i corsi
        return prenotazioneDAO.getCorsiPerCliente(idCliente);
    }
}