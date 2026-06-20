package it.unipv.posfw.test.unit;
/**
 * Classe dedicata al Testing di Unità per il componente {@link GestoreCorsi}.
 * 
 * In conformità con le linee guida del corso sulla Verifica e Convalida, questa classe
 * implementa test funzionali automatizzati volti ad accertare la correttezza del 
 * comportamento del sistema rispetto ai requisiti di UC3. Ogni metodo segue rigorosamente
 * la triade strutturale Setup-Action-Assertion, isolando le operazioni per verificare 
 * la logica di coordinamento del controller.
 * 
 * In termini architetturali, il test valida l'interazione tra lo strato di controllo
 * (Singleton) e il Modello di Dominio, assicurando che le precondizioni e le 
 * eccezioni di business siano gestite correttamente.
 *
 * @author Lorenzo
 * @version 2.1
 * @see GestoreCorsi
 */
// Nuovi import ufficiali di JUnit 5 (Jupiter)
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.posfw.controller.GestoreCorsi;
import it.unipv.posfw.exceptions.SovrapposizioneOrarioException;
import java.time.LocalDateTime;

public class GestoreCorsiTest {

    private GestoreCorsi gestore;

    // In JUnit 5 @Before si chiama @BeforeEach
    @BeforeEach
    public void setUp() {
        gestore = GestoreCorsi.getInstance();
    }

    // 1. TEST DI SUCCESSO
    @Test
    public void testOrganizzaNuovoCorso_Successo() {
        try {
            LocalDateTime orario = LocalDateTime.of(2026, 9, 10, 10, 0);
            gestore.organizzaNuovoCorso("Yoga Mattutino", orario, 15, "PT-123");
            
            // In JUnit 5 si preferisce verificare la non-nullità o usare assertDoesNotThrow
            assertNotNull(gestore); 
            
        } catch (Exception e) {
            fail("Non doveva lanciare un'eccezione per un inserimento valido!");
        }
    }

    // 2. TEST DI FALLIMENTO
    // In JUnit 5 non si usa più (expected = ...), ma il metodo più elegante assertThrows
    @Test
    public void testSovrapposizioneOrario_LanciaEccezione() {
        LocalDateTime orario = LocalDateTime.of(2026, 10, 15, 18, 0);
        
        try {
            // Inseriamo il primo corso valido
            gestore.organizzaNuovoCorso("Crossfit", orario, 20, "PT-999");
        } catch (Exception e) {
            fail("Il primo inserimento doveva riuscire");
        }
        
        // Verifichiamo che il secondo inserimento identico lanci l'eccezione corretta
        assertThrows(SovrapposizioneOrarioException.class, () -> {
            gestore.organizzaNuovoCorso("Pilates", orario, 20, "PT-999");
        });
    }
}