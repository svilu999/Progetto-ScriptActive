package it.unipv.posfw.test;

/**
 * Classe dedicata al Testing di Integrazione per il componente {@link GestoreCorsi}.
 * * In conformità con le linee guida del corso sulla Verifica e Convalida, questa classe
 * implementa test funzionali automatizzati volti ad accertare la correttezza del 
 * comportamento del sistema rispetto ai requisiti di UC3. 
 * * Per garantire il Principio di Isolamento e Ripetibilità dei test su un database 
 * relazionale persistente, la suite utilizza il pattern Teardown (@AfterEach) per 
 * effettuare il rollback fisico delle scritture al termine di ogni scenario.
 *
 * @author Lorenzo
 * @version 3.0
 * @see GestoreCorsi
 */

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.posfw.controller.GestoreCorsi;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.StatoCorso;
import it.unipv.posfw.exceptions.SovrapposizioneOrarioException;
import it.unipv.posfw.exceptions.TrainerNonValidoException;
import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

public class GestoreCorsiTest {

    private GestoreCorsi gestore;

    // --- SETUP: Viene eseguito PRIMA di ogni test ---
    @BeforeEach
    public void setUp() {
        gestore = GestoreCorsi.getInstance();
    }

    // --- TEARDOWN: Viene eseguito DOPO ogni test ---
    @AfterEach
    public void tearDown() {
        /*
         * Pattern Teardown: Pulizia fisica del database.
         * Evita il problema dello "Stato Persistente" assicurando che ogni test
         * trovi un ambiente pulito, permettendo l'esecuzione ripetibile all'infinito.
         */
        System.out.println("[TEARDOWN] Ripulitura ambiente di test in corso...");
        String sql = "DELETE FROM Corso WHERE Nome IN ('Yoga Mattutino', 'Crossfit', 'Pilates', 'Zumba', 'Functional Training')";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int righeCancellate = stmt.executeUpdate();
            System.out.println("[TEARDOWN] Rimossi " + righeCancellate + " record di test dal database.\n");
            
        } catch (Exception e) {
            System.err.println("[TEARDOWN] Errore durante la pulizia: " + e.getMessage());
        }
    }

 // ==========================================
    // 1. TEST DI SUCCESSO (Flusso Principale)
    // ==========================================
    @Test
    public void testOrganizzaNuovoCorso_Successo() {
        try {
            // Spostiamo la data al 2030 per bypassare i "fantasmi" delle esecuzioni precedenti
            LocalDateTime orario = LocalDateTime.of(2030, 11, 20, 10, 0);
            gestore.organizzaNuovoCorso("Yoga Mattutino", orario, 15, "PT-10");
            
            assertNotNull(gestore); 
            
        } catch (Exception e) {
            e.printStackTrace(); 
            fail("Non doveva lanciare un'eccezione per un inserimento valido!");
        }
    }

    // ==========================================
    // 2. TEST DI FALLIMENTO (Prevenzione Sovrapposizioni)
    // ==========================================
    @Test
    public void testSovrapposizioneOrario_LanciaEccezione() {
        LocalDateTime orario = LocalDateTime.of(2026, 12, 10, 18, 0);
        
        try {
            // Primo inserimento (deve riuscire)
            gestore.organizzaNuovoCorso("Crossfit", orario, 20, "PT-10");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Il primo inserimento doveva riuscire senza errori.");
        }
        
        // Secondo inserimento identico (deve essere bloccato)
        assertThrows(SovrapposizioneOrarioException.class, () -> {
            gestore.organizzaNuovoCorso("Pilates", orario, 20, "PT-10");
        });
    }

    // ==========================================
    // 3. TEST FLUSSO ALTERNATIVO (Trainer Non Valido)
    // ==========================================
    @Test
    public void testOrganizzaNuovoCorso_TrainerInesistente_LanciaEccezione() {
        LocalDateTime orario = LocalDateTime.of(2027, 1, 15, 10, 0);
        
        // Verifica che l'Exception Translation intercetti la Foreign Key fallita
        assertThrows(TrainerNonValidoException.class, () -> {
            gestore.organizzaNuovoCorso("Zumba", orario, 20, "PT-999999");
        });
    }

    // ==========================================
    // 4. TEST CICLO DI VITA (Annullamento Corso)
    // ==========================================
    @Test
    public void testAnnullaCorso_Successo() {
        LocalDateTime orario = LocalDateTime.of(2027, 2, 10, 11, 0);
        
        try {
            // Setup del test
            gestore.organizzaNuovoCorso("Functional Training", orario, 10, "PT-10");
            
            // Recupero dell'ID generato
            List<Corso> palinsesto = gestore.getElencoCorsi();
            String idCorso = null;
            for (Corso c : palinsesto) {
                if (c.getNome().equals("Functional Training")) {
                    idCorso = c.getIdCorso();
                    break;
                }
            }
            
            assertNotNull(idCorso, "Il corso non è stato trovato nel database.");

            // Esecuzione dell'azione da testare
            gestore.annullaCorso(idCorso);
            
            // Verifica (Assertion) del cambiamento di stato
            Corso corsoAnnullato = null;
            for (Corso c : gestore.getElencoCorsi()) {
                if (c.getIdCorso().equals(idCorso)) {
                    corsoAnnullato = c;
                    break;
                }
            }
            
            assertNotNull(corsoAnnullato);
            assertEquals(StatoCorso.CANCELLATO, corsoAnnullato.getStato(), 
                    "Lo stato del corso deve essere aggiornato a CANCELLATO.");
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("Il flusso di annullamento ha lanciato un'eccezione imprevista.");
        }
    }
}