package it.unipv.posfw.controller;

import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.dao.PrenotazioneDAO;
import it.unipv.posfw.database.CorsoDAOMySQL;
import it.unipv.posfw.database.PrenotazioneDAOMySQL;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.exceptions.CorsoAlCompletoException;
import it.unipv.posfw.exceptions.PrenotazioneGiaEffettuataException;

import java.util.List; // IMPORTANTE: Aggiunto per poter usare le liste!

public class GestorePrenotazioni {

    private PrenotazioneDAO prenotazioneDAO;
    private CorsoDAO corsoDAO;

    public GestorePrenotazioni() {
 
        this.prenotazioneDAO = new PrenotazioneDAOMySQL();
        this.corsoDAO = new CorsoDAOMySQL();
    }

    public void prenotaCorso(Cliente cliente, Corso corso) throws Exception { 
        
        it.unipv.posfw.dao.PrenotazioneDAO prenotazioneDB = new it.unipv.posfw.database.PrenotazioneDAOMySQL();
        it.unipv.posfw.dao.CorsoDAO corsoDB = new it.unipv.posfw.database.CorsoDAOMySQL();

        String idCliente = String.valueOf(cliente.getId());
        String idCorso = corso.getIdCorso();

        if (prenotazioneDB.esistePrenotazione(idCliente, idCorso)) {
            throw new it.unipv.posfw.exceptions.PrenotazioneGiaEffettuataException("Sei già registrato a questo corso (o sei già in lista d'attesa)!");
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
                throw new it.unipv.posfw.exceptions.CorsoAlCompletoException("Il corso è pieno. Sei stato inserito in LISTA D'ATTESA!");
            } else {
                throw new Exception("Errore interno: impossibile inserirti in lista d'attesa!");
            }
        }
    }
    
    public void annullaPrenotazione(Cliente cliente, Corso corso) throws it.unipv.posfw.exceptions.PrenotazioneInesistenteException {
        

        it.unipv.posfw.dao.PrenotazioneDAO prenotazioneDB = new it.unipv.posfw.database.PrenotazioneDAOMySQL();
        it.unipv.posfw.dao.CorsoDAO corsoDB = new it.unipv.posfw.database.CorsoDAOMySQL();
        

        String idCliente = String.valueOf(cliente.getId());
        String idCorso = corso.getIdCorso();

 
        if (!prenotazioneDB.esistePrenotazione(idCliente, idCorso)) {
            throw new it.unipv.posfw.exceptions.PrenotazioneInesistenteException("Impossibile disiscriversi: non sei prenotato a questo corso!");
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