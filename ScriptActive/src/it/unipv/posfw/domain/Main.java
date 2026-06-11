package it.unipv.posfw.domain;

import javax.swing.SwingUtilities;

import it.unipv.posfw.controller.StoricoAllenamenti;
import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.dao.SessioneDAOSQL;
import it.unipv.posfw.view.StoricoAllenamentiView;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            // MODIFICA QUI: Creiamo il cliente usando il nuovo costruttore unificato.
            // ATTENZIONE: Affinché il database salvi correttamente la sessione, 
            // il Codice Fiscale ("RSSMRA80A01H501Z" in questo esempio) DEVE essere 
            // già presente nella tua tabella 'Utente' di MySQL!
            Cliente clientePremium = new Cliente(
                "Mario",                // Nome
                "Rossi",                // Cognome
                "mario.rossi@email.it", // Email
                "ABCDEF12G34H567I",     // Codice Fiscale (sostituiscilo con uno vero del tuo DB)
                TipoAbbonamento.PREMIUM // Abbonamento
            );
            
            SessioneDAO dao = new SessioneDAOSQL();
            StoricoAllenamentiView view = new StoricoAllenamentiView();
            StoricoAllenamenti controller = new StoricoAllenamenti(view, dao);
            
            view.setController(controller);
            
            // Ora passiamo l'oggetto corretto senza che il codice vada in errore
            view.setUtenteCorrente(clientePremium);

            view.setLocationRelativeTo(null); 
            view.setVisible(true);
        });
    }
}