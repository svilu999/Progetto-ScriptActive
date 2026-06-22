package it.unipv.posfw.domain;

import javax.swing.SwingUtilities;

import it.unipv.posfw.controller.LoginController;
import it.unipv.posfw.dao.UtenteDAO;
import it.unipv.posfw.database.UtenteDAOMySQL;
import it.unipv.posfw.view.LoginView;

/**
 * Classe di Bootstrap (<i>Entry Point</i>) dell'applicazione.
 * <p>
 * Agisce come <b>Composition Root</b> del sistema: è il modulo architetturale centralizzato in cui 
 * le interfacce e le implementazioni concrete vengono istanziate e collegate (<i>Dependency Injection</i>).
 * Il suo compito esclusivo è assemblare la triade iniziale del pattern <b>MVC (Model-View-Controller)</b> 
 * dedicata al processo di Autenticazione (Login) e cedere il controllo del flusso applicativo.
 * </p>
 * * @author Vilucchi
 * @version 1.1
 */
public class Main {
    
    /**
     * Metodo esecutivo principale per l'avvio del software.
     * <p>
     * 
     */
    public static void main(String[] args) {
    	//GestoreRinnovi.getIstanza().avviaMotoreRinnovi();
    	
        SwingUtilities.invokeLater(() -> {
            
            /* 1. Inizializzazione del Data Access Object (Persistenza) */
            UtenteDAO loginDAO = new UtenteDAOMySQL();
            
            /* 2. Inizializzazione del Top-Level Container (View) */
            LoginView loginView = new LoginView();
                                   
              /* 3. Inizializzazione del Controller tramite Dependency Injection (Constructor Injection) */
            LoginController loginController = new LoginController(loginView, loginDAO);
            
            /* 4. Completamento del binding bidirezionale MVC (Event Delegation) */
            loginView.setController(loginController);
            
            /* 5. Richiesta al sistema operativo di renderizzare a schermo l'interfaccia */
            loginView.setVisible(true); 
            
        });
    }
}