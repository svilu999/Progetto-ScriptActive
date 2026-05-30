package it.unipv.posfw.controller;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.dao.CorsoDAOImpl;
import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.exceptions.CorsoNonTrovatoException;
import java.time.LocalDateTime;

public class TestUC3 {
    public static void main(String[] args) {
        System.out.println("=== AVVIO SIMULAZIONE SCRIPTACTIVE UC3 ===");
        
        // 1. Prendiamo l'istanza del nostro controllore unico (Singleton)
        GestoreCorsi gestore = GestoreCorsi.getInstance();
        
        try {
            // 2. Il Direttore organizza un corso di Pilates
            LocalDateTime orario = LocalDateTime.of(2026, 6, 1, 18, 0);
            gestore.organizzaNuovoCorso("Pilates Enterprise", orario, 20, "PT-MARIO");
            
            // Per testare l'Observer, recuperiamo il corso appena fatto e iscriviamo dei clienti
            // (Nota: Usiamo un DAO temporaneo solo per simulare l'iscrizione in questo test)
            CorsoDAO tempDAO = new CorsoDAOImpl(); 
            // In un test reale recupereremmo l'istanza, ma ci basta per vedere l'Observer:         
           
            System.out.println("\n--- Simulazione Annullamento Corso con Notifiche ---");
            // Creiamo un corso direttamente per fare il trigger dell'Observer
            PersonalTrainer pt = new PersonalTrainer("Luigi", "Verdi", "luigi@email.it", "PT-LUIGI");
            Corso corsoTest = new Corso("CRS-123", "Spinning Extreme", orario, 25, pt);
            
            // Creiamo due clienti finti che si iscrivono alle notifiche del corso
            Cliente c1 = new Cliente("Annalisa", "Bianchi", "anna@email.it", "CF-ANN123");
            Cliente c2 = new Cliente("Federico", "Neri", "fede@email.it", "CF-FED456");
            
            corsoTest.attach(c1);
            corsoTest.attach(c2);
            
            // Il Direttore annulla il corso tramite il gestore...
            System.out.println("Il Direttore decide di cancellare il corso...");
            corsoTest.setStato(it.unipv.posfw.domain.StatoCorso.CANCELLATO);
            
        } catch (Exception e) {
            System.err.println("Rilevata anomalia: " + e.getMessage());
        }
    }
}