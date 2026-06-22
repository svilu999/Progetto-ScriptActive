package it.unipv.posfw.test.unit;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;

import it.unipv.posfw.controller.GestorePrenotazioni;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.exceptions.CorsoAlCompletoException;
import it.unipv.posfw.exceptions.PrenotazioneGiaEffettuataException;
import it.unipv.posfw.util.DatabaseManager;

class GestorePrenotazioniTest {

    private GestorePrenotazioni gestore;

    // Eseguito PRIMA di ogni test: prepara il DB e il Controller
    @BeforeEach
    void setUp() {
        resetDatabase();
        gestore = new GestorePrenotazioni();
    }

    // Eseguito DOPO ogni test: fa piazza pulita
    @AfterEach
    void tearDown() {
        resetDatabase();
    }

    @Test
    void testPrenotazioneSuccesso() throws Exception {
        Cliente c = new Cliente("Mario", "Rossi", "mario.rossi@email.it", "RSSMRA80A01H501Z");
        c.setId(3); 
        Corso corso = new Corso("1", "Corso Funzionale", LocalDateTime.now(), 15, null);
        
        assertDoesNotThrow(() -> gestore.prenotaCorso(c, corso));
    }

    @Test
    void testEccezioneDoppiaPrenotazione() throws Exception {
        Cliente c = new Cliente("Mario", "Rossi", "mario.rossi@email.it", "RSSMRA80A01H501Z");
        c.setId(3); 
        Corso corso = new Corso("1", "Corso Funzionale", LocalDateTime.now(), 15, null);

        // FIX: Facciamo prima una prenotazione valida (che va a buon fine)
        gestore.prenotaCorso(c, corso);

        // ORA proviamo a rifarla! Questa volta DEVE lanciare l'eccezione
        assertThrows(PrenotazioneGiaEffettuataException.class, () -> {
            gestore.prenotaCorso(c, corso);
        });
    }

    @Test
    void testListaAttesaESwap() throws Exception {
        Corso corsoZumba = new Corso("5", "Zumba", LocalDateTime.now(), 18, null);
        // FIX: Il costruttore lo imposta a 18, ma a noi serve che ce ne sia solo 1!
        corsoZumba.setPostiDisponibili(1); 
        
        Cliente cMario = new Cliente("Mario", "Rossi", "mario.rossi@email.it", "RSSMRA80A01H501Z");
        cMario.setId(3); 
        
        Cliente cLorenzo = new Cliente("Lorenzo", "Varano", "lorenzo@studenti.unipv.it", "VRNLRN99M21F205W");
        cLorenzo.setId(4); 

        // ATTO 1: Mario prende l'unico posto disponibile (i posti in memoria passano a 0)
        assertDoesNotThrow(() -> gestore.prenotaCorso(cMario, corsoZumba));

        // ATTO 2: Lorenzo prova a prenotare, vede 0 posti e lancia l'eccezione (Lista d'attesa)
        assertThrows(CorsoAlCompletoException.class, () -> {
            gestore.prenotaCorso(cLorenzo, corsoZumba);
        });

        // ATTO 3: Mario si disiscrive, scatta lo swap!
        assertDoesNotThrow(() -> gestore.annullaPrenotazione(cMario, corsoZumba));
    }

    // =======================================================
    // METODO HELPER PER L'IDEMPOTENZA DEL DATABASE
    // =======================================================
 // =======================================================
    // METODO HELPER PER L'IDEMPOTENZA DEL DATABASE
    // =======================================================
    private void resetDatabase() {
        try {
            // Sfruttiamo il Singleton del progetto per ottenere la connessione sicura
            // senza esporre le credenziali nel codice sorgente!
            Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();

            // Disabilita i controlli temporaneamente
            stmt.execute("SET SQL_SAFE_UPDATES = 0");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Pulisce le prenotazioni e resetta gli ID
            stmt.execute("DELETE FROM Prenotazione");
            stmt.execute("ALTER TABLE Prenotazione AUTO_INCREMENT = 1");

            // Imposta i posti esatti per far funzionare i test (1 posto a Zumba, 15 a Funzionale)
            stmt.execute("UPDATE Corso SET PostiDisponibili = 1 WHERE ID_Corso = 5");
            stmt.execute("UPDATE Corso SET PostiDisponibili = 15 WHERE ID_Corso = 1");

            // Riattiva le sicurezze
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            stmt.execute("SET SQL_SAFE_UPDATES = 1");

            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Errore durante il reset del Database per il test: " + e.getMessage());
        }
    
    }
}