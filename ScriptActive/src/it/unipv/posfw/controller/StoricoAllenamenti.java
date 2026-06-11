package it.unipv.posfw.controller;

import java.util.List;

public class StoricoAllenamenti {
    private StoricoAllenamentiForm view;
    private SessioneDAO dao;

    public StoricoAllenamentiController(StoricoAllenamentiForm view, SessioneDAO dao) {
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

    // NUOVO METODO: Salva un'intera sessione con N esercizi
    public void salvaSessioneCompleta(java.util.Date data, List<DatiForm> esercizi, Cliente cliente) {
        // 1. Crea la sessione padre
        SessioneAllenamento nuovaSessione = new SessioneAllenamento(data, cliente.getIdCliente());
        
        // 2. Aggiunge tutti gli esercizi figli
        for (DatiForm esercizio : esercizi) {
            nuovaSessione.aggiungiEsercizio(esercizio);
        }

        // 3. Salva la sessione completa nel DB
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