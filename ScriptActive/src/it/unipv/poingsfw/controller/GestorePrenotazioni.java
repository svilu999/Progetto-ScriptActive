package it.unipv.poingsfw.controller;

import it.unipv.poingsfw.dao.CorsoDAO;
import it.unipv.poingsfw.dao.PrenotazioneDAO;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.exceptions.CorsoAlCompletoException;
import it.unipv.poingsfw.exceptions.PrenotazioneGiaEffettuataException;
import it.unipv.poingsfw.exceptions.PrenotazioneInesistenteException;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Controller dedicato alla gestione delle prenotazioni dei corsi.
 * Implementa la logica per l'iscrizione, la gestione della capienza 
 * massima (Lista d'Attesa) e lo swap automatico in caso di annullamento.
 * Utilizza il pattern DAO per comunicare con il database: riceve le interfacce
 * {@link PrenotazioneDAO} e {@link CorsoDAO} dal costruttore (Dependency Injection)
 * e non conosce l'implementazione MySQL.
 * * @author Matteo
 * @version 1.0
 */

public class GestorePrenotazioni {

    private static final Logger LOG = Logger.getLogger(GestorePrenotazioni.class.getName());

    private static final String STATO_CONFERMATA = "Confermata";
    private static final String STATO_LISTA_ATTESA = "InListaAttesa";

    private final PrenotazioneDAO prenotazioneDAO;
    private final CorsoDAO corsoDAO;

    public GestorePrenotazioni(PrenotazioneDAO prenotazioneDAO, CorsoDAO corsoDAO) {
        this.prenotazioneDAO = Objects.requireNonNull(prenotazioneDAO, "prenotazioneDAO non può essere null");
        this.corsoDAO = Objects.requireNonNull(corsoDAO, "corsoDAO non può essere null");
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
        

        String idCliente = String.valueOf(cliente.getId());
        String idCorso = corso.getIdCorso();

        if (prenotazioneDAO.esistePrenotazione(idCliente, idCorso)) {
            throw new PrenotazioneGiaEffettuataException("Sei già registrato a questo corso (o sei già in lista d'attesa)!");
        }

        if (corso.getPostiDisponibili() > 0) {
            boolean successo = prenotazioneDAO.inserisciPrenotazione(idCliente, idCorso, STATO_CONFERMATA);
            if (successo) {
                corso.setPostiDisponibili(corso.getPostiDisponibili() - 1);
                corsoDAO.updatePostiDisponibili(corso);
            } else {

                throw new Exception("Errore interno: impossibile salvare la prenotazione nel Database!");
            }
        } else {
            boolean successo = prenotazioneDAO.inserisciPrenotazione(idCliente, idCorso, STATO_LISTA_ATTESA);
            if (successo) {
                throw new CorsoAlCompletoException("Il corso è pieno. Sei stato inserito in LISTA D'ATTESA!");
            } else {
                throw new Exception("Errore interno: impossibile inserirti in lista d'attesa!");
            }
        }
    }
    
    
    /**
     * Annulla una prenotazione esistente e gestisce l'avanzamento automatico della coda (Swap).
     * Dopo l'eliminazione della prenotazione del cliente, il sistema verifica la presenza 
     * di utenti in Lista d'Attesa. Se presenti, il primo utente in coda viene promosso 
     * allo stato STATO_CONFERMATA; altrimenti, viene liberato un posto nel corso.
     * * @param cliente Il cliente che richiede l'annullamento.
     * @param corso Il corso dal quale il cliente vuole disiscriversi.
     * @throws PrenotazioneInesistenteException Se non viene trovata alcuna prenotazione attiva per la coppia Cliente-Corso.
     * @throws Exception In caso di errori generici nel processo di cancellazione o di swap.
     */
    
    public void annullaPrenotazione(Cliente cliente, Corso corso) throws PrenotazioneInesistenteException {
        

        

        String idCliente = String.valueOf(cliente.getId());
        String idCorso = corso.getIdCorso();

 
        if (!prenotazioneDAO.esistePrenotazione(idCliente, idCorso)) {
            throw new PrenotazioneInesistenteException("Impossibile disiscriversi: non sei prenotato a questo corso!");
        }


        boolean eliminato = prenotazioneDAO.eliminaPrenotazione(idCliente, idCorso);
        
        if (eliminato) {

            String idPrimoInAttesa = prenotazioneDAO.getPrimoInListaAttesa(idCorso);
            
            if (idPrimoInAttesa != null) {
                prenotazioneDAO.aggiornaStatoPrenotazione(idPrimoInAttesa, idCorso, STATO_CONFERMATA);
                LOG.info("Posto liberato assegnato automaticamente all'utente ID: " + idPrimoInAttesa);
            } else {
                corso.setPostiDisponibili(corso.getPostiDisponibili() + 1);
                corsoDAO.updatePostiDisponibili(corso); 
            }
        } else {
            LOG.warning("Eliminazione della prenotazione non riuscita nel livello di persistenza.");
        }
    }

    /**
     * Recupera i corsi prenotati dal cliente indicato.
     * * @param cliente Il cliente di cui si vogliono conoscere le prenotazioni.
     * @return La lista dei corsi prenotati (vuota se non ce ne sono).
     */
    public List<Corso> getCorsiPrenotatiDalCliente(Cliente cliente) {
        String idCliente = String.valueOf(cliente.getId());
        
        return prenotazioneDAO.getCorsiPerCliente(idCliente);
    }
}