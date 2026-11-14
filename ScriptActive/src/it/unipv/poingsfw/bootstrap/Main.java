package it.unipv.poingsfw.bootstrap;

import javax.swing.SwingUtilities;

import it.unipv.poingsfw.controller.GestoreCorsi;
import it.unipv.poingsfw.controller.GestorePrenotazioni;
import it.unipv.poingsfw.controller.GestoreRegistrazione;
import it.unipv.poingsfw.dao.ClienteDAO;
import it.unipv.poingsfw.dao.CorsoDAO;
import it.unipv.poingsfw.dao.PrenotazioneDAO;
import it.unipv.poingsfw.dao.SedeDAO;
import it.unipv.poingsfw.dao.SessioneDAO;
import it.unipv.poingsfw.dao.UtenteDAO;
import it.unipv.poingsfw.database.ClienteDAOMySQL;
import it.unipv.poingsfw.database.CorsoDAOMySQL;
import it.unipv.poingsfw.database.PrenotazioneDAOMySQL;
import it.unipv.poingsfw.database.SedeDAOMySQL;
import it.unipv.poingsfw.database.SessioneDAOMySQL;
import it.unipv.poingsfw.database.UtenteDAOMySQL;
import it.unipv.poingsfw.service.GestorePagamenti;
import it.unipv.poingsfw.service.ServizioPagamenti;

/**
 * Classe di Bootstrap (<i>Entry Point</i>) dell'applicazione.
 * <p>
 * Agisce come <b>Composition Root</b> del sistema: è il modulo architetturale centralizzato in cui 
 * le interfacce e le implementazioni concrete vengono istanziate e collegate (<i>Dependency Injection</i>).
 * È l'unico punto, insieme al {@link NavigatoreSwing}, in cui compaiono le classi concrete della
 * persistenza MySQL: tutto il resto dell'applicazione dipende solo da interfacce.
 * </p>
 * <p>
 * Ogni DAO e ogni controller condiviso viene creato qui una sola volta: l'unicità delle istanze
 * è garantita dalla composizione e non dal pattern Singleton.
 * </p>
 * * @author Vilucchi
 * @version 2.0
 */
public class Main {
    
    /**
     * Metodo esecutivo principale per l'avvio del software.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            /* 1. Livello di persistenza: le uniche implementazioni concrete dei DAO */
            UtenteDAO utenteDAO = new UtenteDAOMySQL();
            ClienteDAO clienteDAO = new ClienteDAOMySQL();
            SedeDAO sedeDAO = new SedeDAOMySQL();
            CorsoDAO corsoDAO = new CorsoDAOMySQL();
            PrenotazioneDAO prenotazioneDAO = new PrenotazioneDAOMySQL();
            SessioneDAO sessioneDAO = new SessioneDAOMySQL();

            /* 2. Servizi esterni */
            ServizioPagamenti servizioPagamenti = new GestorePagamenti();

            /* 3. Controller condivisi tra più schermate (Constructor Injection) */
            GestoreCorsi gestoreCorsi = new GestoreCorsi(corsoDAO);
            GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni(prenotazioneDAO, corsoDAO);
            GestoreRegistrazione gestoreRegistrazione =
                    new GestoreRegistrazione(clienteDAO, sedeDAO, servizioPagamenti);

            /*
             * Nota: il motore dei rinnovi automatici in background
             * (it.unipv.poingsfw.service.GestoreRinnovi) non viene avviato, come nella versione
             * precedente dell'applicazione. Per attivarlo:
             *   new GestoreRinnovi(clienteDAO, servizioPagamenti).avviaMotoreRinnovi();
             */

            /* 4. Navigazione: costruisce le schermate e collega View e Controller */
            NavigatoreSwing navigatore = new NavigatoreSwing(
                    utenteDAO, sessioneDAO, gestoreCorsi, gestorePrenotazioni, gestoreRegistrazione);

            /* 5. Cessione del controllo del flusso applicativo alla schermata di Login */
            navigatore.apriLogin();
        });
    }
}
