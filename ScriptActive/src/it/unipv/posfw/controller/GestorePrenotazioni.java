package it.unipv.posfw.controller;

import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.dao.CorsoDAOMySQL;
import it.unipv.posfw.dao.PrenotazioneDAO;
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
    public void prenotaCorso(Cliente cliente, Corso corso)
            throws CorsoAlCompletoException, PrenotazioneGiaEffettuataException {

        /*
         * Controllo di dominio:
         * verifica se il corso ha ancora posti disponibili.
         */
        if (corso.alCompleto()) {
            throw new CorsoAlCompletoException(
                    "Spiacenti, il corso " + corso.getNome() + " ha raggiunto la capienza massima."
            );
        }

        /*
         * Controllo DAO:
         * verifica se il cliente ha già prenotato questo corso.
         */
        if (prenotazioneDAO.esistePrenotazione(cliente.getIdCliente(), corso.getIdCorso())) {
            throw new PrenotazioneGiaEffettuataException("Sei già prenotato a questo corso!");
        }

        /*
         * Modifica nel dominio.
         * decrementaPosti aggiorna l'oggetto Corso in memoria.
         */
        corso.decrementaPosti();

        /*
         * Pattern Observer:
         * il cliente viene collegato al corso per ricevere eventuali notifiche.
         */
        corso.attach(cliente);

        /*
         * Persistenza:
         * salva la prenotazione e aggiorna i posti disponibili nel database.
         */
        prenotazioneDAO.inserisciPrenotazione(cliente.getIdCliente(), corso.getIdCorso());
        corsoDAO.updatePostiDisponibili(corso);

        System.out.println(
                "Prenotazione effettuata con successo! Posti rimanenti: " + corso.getPostiDisponibili()
        );
    }

    /**
     * Annulla una prenotazione esistente.
     */
    public void annullaPrenotazione(Cliente cliente, Corso corso) {

        /*
         * Modifica nel dominio:
         * libera un posto nel corso.
         */
        corso.incrementaPosti();

        /*
         * Pattern Observer:
         * il cliente viene scollegato dal corso.
         */
        corso.detach(cliente);

        /*
         * Persistenza:
         * elimina la prenotazione e aggiorna i posti disponibili nel database.
         */
        prenotazioneDAO.eliminaPrenotazione(cliente.getIdCliente(), corso.getIdCorso());
        corsoDAO.updatePostiDisponibili(corso);

        System.out.println(
                "Prenotazione annullata con successo. Posti rimanenti: " + corso.getPostiDisponibili()
        );
    }
}