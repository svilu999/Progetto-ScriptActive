package it.unipv.posfw.domain;

import javax.swing.SwingUtilities;

import it.unipv.posfw.controller.LoginController;
import it.unipv.posfw.view.LoginView;
import it.unipv.posfw.view.RegistrazioneView; // IMPORTIAMO LA TUA VIEW

// IMPORTIAMO IL DAO CORRETTO
import it.unipv.posfw.database.UtenteDAO;
import it.unipv.posfw.database.UtenteDAOMySQL;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            UtenteDAO loginDAO = new UtenteDAOMySQL();
            
            LoginView loginView = new LoginView();
            LoginController loginController = new LoginController(loginView, loginDAO);
            loginView.setController(loginController);
            loginView.setVisible(true);
            
            // Creiamo un'istanza della tua View per la registrazione
            RegistrazioneView registrazioneView = new RegistrazioneView();
            
            // Facciamo apparire il tuo modulo a step sullo schermo
           // registrazioneView.mostraModuloRegistrazione();

        });
    }
}