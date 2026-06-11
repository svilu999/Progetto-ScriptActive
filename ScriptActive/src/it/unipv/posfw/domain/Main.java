package it.unipv.posfw.domain;

import javax.swing.SwingUtilities;

import it.unipv.posfw.dao.SessioneDAO;
import it.unipv.posfw.dao.SessioneDAOSQL;
import provaview.Cliente;
import provaview.StoricoAllenamentiForm;
import provaview.TipoAbbonamento;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            // MODIFICA QUI: L'ID ora è "1" e non più "001" 
            // In questo modo, quando il DAO usa Integer.parseInt("1"), otterrà il numero 1.
            Cliente clientePremium = new Cliente("1", TipoAbbonamento.PREMIUM);
            
            SessioneDAO dao = new SessioneDAOSQL();
            StoricoAllenamentiForm view = new StoricoAllenamentiForm();
            StoricoAllenamentiController controller = new StoricoAllenamentiController(view, dao);
            
            view.setController(controller);
            view.setUtenteCorrente(clientePremium);

            view.setLocationRelativeTo(null); 
            view.setVisible(true);
        });
    }
}