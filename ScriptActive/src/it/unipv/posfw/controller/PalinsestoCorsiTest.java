package it.unipv.posfw.controller;

import java.util.List;
import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.dao.CorsoDAOMySQL;
import it.unipv.posfw.domain.Corso;

public class PalinsestoCorsiTest {
    
    public static void main(String[] args) {
        System.out.println("[TEST INTEGRATION] Avvio verifica persistenza Palinsesto");
        System.out.println("-------------------------------------------------------");
        
        try {
            // Inizializzazione del DAO reale agganciato a MySQL Workbench
            CorsoDAO corsoDAO = new CorsoDAOMySQL();
            
            System.out.println("[DB STATUS] Interrogazione tabella 'Corso' in corso...");
            List<Corso> corsiRecuperati = corsoDAO.getPalinsesto();
            
            System.out.println("[DB STATUS] Record estratti con successo: " + corsiRecuperati.size());
            System.out.println("\n--- RISULTATI DELLA QUERY REALE ---");
            
            if (corsiRecuperati.isEmpty()) {
                System.out.println("[WARN] Il palinsesto è vuoto. Verifica di avere corsi con Stato = 'Pianificato' nel DB.");
            } else {
                for (Corso c : corsiRecuperati) {
                    System.out.println(String.format("Corso: %-20s | Data: %-16s | Trainer: %s", 
                        c.getNome(), 
                        c.getDataOra().toString().replace("T", " "), 
                        c.getTrainerAssegnato().getNome() + " " + c.getTrainerAssegnato().getCognome()
                    ));
                }
            }
            
            System.out.println("-------------------------------------------------------");
            System.out.println("[TEST SUCCESS] Flusso dati da MySQL al Dominio verificato.");
            
        } catch (Exception e) {
            System.err.println("[TEST FAILED] Rilevata eccezione critica durante la lettura:");
            e.printStackTrace();
        }
    }
}