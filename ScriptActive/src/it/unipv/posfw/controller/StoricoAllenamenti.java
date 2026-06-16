package it.unipv.posfw.controller;

import java.util.List;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.DatiFormPojo;
import it.unipv.posfw.domain.SessioneAllenamento;
import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.view.StoricoAllenamentiView;

// IMPORTIAMO LE NOSTRE ECCEZIONI PERSONALIZZATE
import it.unipv.posfw.exceptions.SchedaVuotaException;
import it.unipv.posfw.exceptions.DatiAllenamentoNonValidiException;
import it.unipv.posfw.exceptions.SalvataggioFallitoException;
import it.unipv.posfw.exceptions.UtenteNonPremiumException;

public class StoricoAllenamenti {
    private StoricoAllenamentiView view;
    private SessioneDAO dao;

    public StoricoAllenamenti(StoricoAllenamentiView view, SessioneDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    // AGGIORNATO: Ora "lancia" (throws) un'eccezione se il cliente non è premium
    public void gestisciAccessoSezione(Cliente cliente) throws UtenteNonPremiumException {
        if (!cliente.isPremium()) {
            throw new UtenteNonPremiumException();
        } else {
            view.mostraModuloInserimento();
            caricaStorico(cliente); 
        }
    }

    // Gestisce 3 tipi di errori diversi (Scheda Vuota, Dati Invalidi, Database KO)
    public void salvaSessioneCompleta(java.util.Date data, List<DatiFormPojo> esercizi, Cliente cliente) 
           throws SchedaVuotaException, DatiAllenamentoNonValidiException, SalvataggioFallitoException {
        
        // 1. Controllo: La scheda è vuota?
        if (esercizi == null || esercizi.isEmpty()) {
            throw new SchedaVuotaException();
        }

        // 2. Controllo: I dati degli esercizi sono validi?
        for (DatiFormPojo esercizio : esercizi) {
            if (esercizio.getCarichi() < 0) {
                throw new DatiAllenamentoNonValidiException("Errore in '" + esercizio.getNomeEsercizio() + "': Il carico non può essere negativo.");
            }
            if (esercizio.getRipetizioni() <= 0) {
                throw new DatiAllenamentoNonValidiException("Errore in '" + esercizio.getNomeEsercizio() + "': Le ripetizioni devono essere maggiori di zero.");
            }
        }

        // Se i controlli passano, creo l'oggetto Sessione
        SessioneAllenamento nuovaSessione = new SessioneAllenamento(data, cliente.getIdCliente());
        for (DatiFormPojo esercizio : esercizi) {
            nuovaSessione.aggiungiEsercizio(esercizio);
        }

        // 3. Provo a salvare sul database
        boolean isSalvato = dao.salvaSessione(nuovaSessione);

        if (!isSalvato) {
            // Se il DAO restituisce false (fallimento SQL), lancio l'eccezione
            throw new SalvataggioFallitoException();
        }

        // Se arriviamo fin qui senza lanciare eccezioni, è andato tutto bene
        caricaStorico(cliente); 
    }

    // AGGIORNATO: Se l'eliminazione fallisce, lancio un'eccezione mascherando l'errore DB
    public void eliminaSessioneSelezionata(SessioneAllenamento sessione, Cliente cliente) throws SalvataggioFallitoException {
        boolean rimosso = dao.eliminaSessioneSpecifica(sessione);
        
        if (rimosso) {
            caricaStorico(cliente); 
        } else {
            throw new SalvataggioFallitoException("Impossibile eliminare l'allenamento. Riprova più tardi.");
        }
    }

    private void caricaStorico(Cliente cliente) {
        List<SessioneAllenamento> storico = dao.getStorico(cliente.getIdCliente());
        view.mostraStorico(storico);
    }
}