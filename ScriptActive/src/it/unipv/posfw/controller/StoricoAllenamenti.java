package it.unipv.posfw.controller;

import java.util.List;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.DatiForm;
import it.unipv.posfw.domain.SessioneAllenamento;
import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.view.StoricoAllenamentiView;

public class StoricoAllenamenti {
    private StoricoAllenamentiView view;
    private SessioneDAO dao;

    // Costruttore corretto (stesso nome della classe e riferimenti esatti alla View)
    public StoricoAllenamenti(StoricoAllenamentiView view, SessioneDAO dao) {
        this.view = view;
        this.dao = dao;
    }

    public void gestisciAccessoSezione(Cliente cliente) {
        if (!cliente.isPremium()) {
            view.mostraBloccoUpgradePremium();
        } else {
            view.mostraModuloInserimento();
            caricaStorico(cliente); 
        }
    }

    public void salvaSessioneCompleta(java.util.Date data, List<DatiForm> esercizi, Cliente cliente) {
        SessioneAllenamento nuovaSessione = new SessioneAllenamento(data, cliente.getIdCliente());
        
        for (DatiForm esercizio : esercizi) {
            nuovaSessione.aggiungiEsercizio(esercizio);
        }

        boolean isSalvato = dao.salvaSessione(nuovaSessione);

        if (isSalvato) {
            caricaStorico(cliente); 
        } else {
            view.mostraMessaggio("Errore durante il salvataggio della sessione.", "Errore Database", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminaSessioneSelezionata(SessioneAllenamento sessione, Cliente cliente) {
        boolean rimosso = dao.eliminaSessioneSpecifica(sessione);
        
        if (rimosso) {
            caricaStorico(cliente); 
        } else {
            view.mostraMessaggio("Errore durante l'eliminazione.", "Errore", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void caricaStorico(Cliente cliente) {
        List<SessioneAllenamento> storico = dao.getStorico(cliente.getIdCliente());
        view.mostraStorico(storico);
    }
}