package it.unipv.posfw.controller;

import java.time.LocalDateTime;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.TipoAbbonamento;

public class TestPrenotazione {

    public static void main(String[] args) {
        
        // Passiamo i numeri "3" e "1" come stringhe per aggirare il problema tra Java e DB
        String idClienteVero = "3"; // L'ID di Giulia Cliente su MySQL
        String idCorsoVero = "1";   // L'ID del Corso Yoga su MySQL
        
        System.out.println("--- INIZIO TEST PRENOTAZIONE ---");
        
        // Creiamo gli oggetti fittizi con gli ID corretti (gli altri campi non importano per il DAO)
        Cliente clienteTest = new Cliente("Giulia", "Cliente", "cli@test.com", idClienteVero, TipoAbbonamento.BASE);
        Corso corsoTest = new Corso(idCorsoVero, "Corso Yoga", LocalDateTime.now(), 20, null);

        GestorePrenotazioni gestore = new GestorePrenotazioni();

        try {
            System.out.println("Provo a prenotare il cliente ID=" + idClienteVero + " al corso ID=" + idCorsoVero + "...");
            
            gestore.prenotaCorso(clienteTest, corsoTest);
            
            System.out.println("\n✅ TEST COMPLETATO SENZA ECCEZIONI IN JAVA!");
            System.out.println("👉 Vai su MySQL Workbench e lancia:");
            System.out.println("SELECT * FROM Prenotazione; (Dovrebbe esserci una riga)");
            System.out.println("SELECT PostiDisponibili FROM Corso WHERE ID_Corso = 1; (Dovrebbe essere 19)");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERRORE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}