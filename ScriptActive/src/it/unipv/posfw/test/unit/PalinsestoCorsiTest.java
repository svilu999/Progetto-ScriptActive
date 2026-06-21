package it.unipv.posfw.test.unit;

import java.util.List;
import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.database.CorsoDAOMySQL;
import it.unipv.posfw.domain.Corso;

/**
 * Componente dedicato al Testing di Integrazione per la verifica della persistenza del palinsesto.
 * 
 * Secondo il modello a V del processo software, questa classe funge da driver per il test 
 * di integrazione tra il Modello di Dominio e il Data Access Layer. L'obiettivo primario è 
 * accertare che il mapping indiretto operato dal Data Access Object (DAO) traduca fedelmente 
 * le relazioni relazionali del DBMS in oggetti Java Bean (POJO), garantendo la coerenza dei 
 * dati prima del dispiegamento dello strato di presentazione (GUI).
 * 
 * In termini architetturali, l'uso di questa classe permette di isolare e risolvere 
 * problematiche legate alla Java Database Connectivity (JDBC) e alla configurazione 
 * dello schema, rispettando il principio di separazione delle responsabilità.
 *
 * @author Lorenzo
 * @version 1.2
 * @see CorsoDAO
 * @see Corso
 */
public class PalinsestoCorsiTest {

    /**
     * Entry-point principale per l'esecuzione dello Smoke Test di integrazione.
     * 
     * Il metodo avvia il normale flusso di esecuzione del programma per verificare 
     * l'interoperabilità con il supporto fisico di memorizzazione. Implementa una 
     * logica di coordinamento che istanzia una Pure Fabrication (CorsoDAOMySQL) 
     * e ne invoca i metodi di recupero dati, validando lo stato degli oggetti 
     * di dominio risultanti.
     * 
     * @param args Argomenti passati da linea di comando (non processati in questa fase di test).
     */
    public static void main(String[] args) {
        // Segnalazione avvio delle attività di Verifica e Convalida (Verification and Validation)
        System.out.println("[INTEGRATION TEST] Avvio fase di collaudo: Data Access Layer");
        System.out.println("-------------------------------------------------------");

        try {
            /* 
             * Istanziazione del DAO specifico per MySQL. Si programma verso l'interfaccia 
             * CorsoDAO per favorire il Low Coupling e la manutenibilità.
             */
            CorsoDAO corsoDAO = new CorsoDAOMySQL();

            // Invocazione dell'operazione di sistema per il recupero del palinsesto filtrato
            List<Corso> corsiRecuperati = corsoDAO.getPalinsesto();

            // Analisi della collezione risultante per accertare la presenza di record consistenti
            if (corsiRecuperati.isEmpty()) {
                System.out.println("[WARN] Stato DB: La query non ha prodotto risultati. Verificare i filtri sullo 'Stato'.");
            } else {
                System.out.println(String.format("[STATUS] Recuperate %d istanze di dominio.", corsiRecuperati.size()));
                System.out.println("\n--- VALIDAZIONE MAPPING INDIRETTO ---");

                // Iterazione per verificare la corretta ricostruzione degli oggetti di dominio
                for (Corso c : corsiRecuperati) {
                    /* 
                     * Stampa formattata volta a verificare l'integrità delle associazioni tra oggetti
                     * (es. l'oggetto Corso deve possedere un riferimento valido a PersonalTrainer).
                     */
                    System.out.println(String.format("Dominio -> Corso: %-20s | Orario: %-16s | Trainer: %s %s",
                        c.getNome(),
                        c.getDataOra().toString().replace("T", " "),
                        c.getTrainerAssegnato().getNome(),
                        c.getTrainerAssegnato().getCognome()
                    ));
                }
            }

            System.out.println("-------------------------------------------------------");
            System.out.println("[SUCCESS] Collaudo terminato: Comunicazione DB-Dominio verificata.");

        } catch (Exception e) {
            /* 
             * Gestione delle eccezioni controllate (Checked Exceptions). In caso di errore SQL 
             * o di rete, il sistema traccia lo stack delle chiamate per facilitare il debugging.
             */
            System.err.println("[FAILED] Errore critico rilevato durante l'esecuzione del Test:");
            e.printStackTrace();
        }
    }
}