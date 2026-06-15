package it.unipv.posfw.controller;

import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.dao.PrenotazioneDAO;
import it.unipv.posfw.database.CorsoDAOMySQL;
import it.unipv.posfw.database.PrenotazioneDAOMySQL;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.exceptions.CorsoAlCompletoException;
import it.unipv.posfw.exceptions.PrenotazioneGiaEffettuataException;

public class GestorePrenotazioni {

    private PrenotazioneDAO prenotazioneDAO;
    private CorsoDAO corsoDAO;

    public GestorePrenotazioni() {
        /*
         * PrenotazioneDAOMySQL resta nel package database,
         * perché per ora stiamo correggendo solo il DAO dei corsi.
         *
         * CorsoDAOMySQL invece ora viene importato dal package dao:
         * it.unipv.posfw.dao.CorsoDAOMySQL
         */
        this.prenotazioneDAO = new PrenotazioneDAOMySQL();
        this.corsoDAO = new CorsoDAOMySQL();
    }

    /**
     * Tenta di prenotare un corso per un cliente.
     */
    public void prenotaCorso(Cliente cliente, Corso corso) throws Exception { // <-- Metti Exception generica o le tue
        
        it.unipv.posfw.dao.PrenotazioneDAO prenotazioneDB = new it.unipv.posfw.database.PrenotazioneDAOMySQL();
        it.unipv.posfw.dao.CorsoDAO corsoDB = new it.unipv.posfw.database.CorsoDAOMySQL();

        // ECCO LA MODIFICA: Prendiamo l'ID vero e lo trasformiamo in Stringa!
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
                // IL BLOCCO ANTIFREGATURA
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

 
//ANNULLAMENTO CON LISTA D'ATTESA ---
    
    	public void annullaPrenotazione(Cliente cliente, Corso corso) throws it.unipv.posfw.exceptions.PrenotazioneInesistenteException {
        
        // 1. ISTANZIAMO I DAO (Presentiamo le variabili a Java!)
        it.unipv.posfw.dao.PrenotazioneDAO prenotazioneDB = new it.unipv.posfw.database.PrenotazioneDAOMySQL();
        it.unipv.posfw.dao.CorsoDAO corsoDB = new it.unipv.posfw.database.CorsoDAOMySQL();
        
        // 2. Recuperiamo gli ID
        String idCliente = String.valueOf(cliente.getId());
        String idCorso = corso.getIdCorso();

        // 3. Controllo: L'utente è davvero iscritto?
        if (!prenotazioneDB.esistePrenotazione(idCliente, idCorso)) {
            throw new it.unipv.posfw.exceptions.PrenotazioneInesistenteException("Impossibile disiscriversi: non sei prenotato a questo corso!");
        }

        // 4. Eliminiamo la prenotazione dal Database
        boolean eliminato = prenotazioneDB.eliminaPrenotazione(idCliente, idCorso);
        
        if (eliminato) {
            // 5. Gestione della Lista d'Attesa
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
}