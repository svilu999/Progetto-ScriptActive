package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;

import it.unipv.poingsfw.controller.GestorePrenotazioni;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.exceptions.CorsoAlCompletoException;
import it.unipv.poingsfw.exceptions.PrenotazioneGiaEffettuataException;
import it.unipv.poingsfw.util.DatabaseManager;

class GestorePrenotazioniTest {

    private GestorePrenotazioni gestore;


    @BeforeEach
    void setUp() {
        resetDatabase();
        gestore = new GestorePrenotazioni();
    }


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


        gestore.prenotaCorso(c, corso);


        assertThrows(PrenotazioneGiaEffettuataException.class, () -> {
            gestore.prenotaCorso(c, corso);
        });
    }

    @Test
    void testListaAttesaESwap() throws Exception {
        Corso corsoZumba = new Corso("5", "Zumba", LocalDateTime.now(), 18, null);

        corsoZumba.setPostiDisponibili(1); 
        
        Cliente cMario = new Cliente("Mario", "Rossi", "mario.rossi@email.it", "RSSMRA80A01H501Z");
        cMario.setId(3); 
        
        Cliente cLorenzo = new Cliente("Lorenzo", "Varano", "lorenzo@studenti.unipv.it", "VRNLRN99M21F205W");
        cLorenzo.setId(4); 


        assertDoesNotThrow(() -> gestore.prenotaCorso(cMario, corsoZumba));


        assertThrows(CorsoAlCompletoException.class, () -> {
            gestore.prenotaCorso(cLorenzo, corsoZumba);
        });


        assertDoesNotThrow(() -> gestore.annullaPrenotazione(cMario, corsoZumba));
    }


    private void resetDatabase() {
        try {

            Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();


            stmt.execute("SET SQL_SAFE_UPDATES = 0");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");


            stmt.execute("DELETE FROM Prenotazione");
            stmt.execute("ALTER TABLE Prenotazione AUTO_INCREMENT = 1");


            stmt.execute("UPDATE Corso SET PostiDisponibili = 1 WHERE ID_Corso = 5");
            stmt.execute("UPDATE Corso SET PostiDisponibili = 15 WHERE ID_Corso = 1");


            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            stmt.execute("SET SQL_SAFE_UPDATES = 1");

            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Errore durante il reset del Database per il test: " + e.getMessage());
        }
    
    }
}