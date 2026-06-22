package it.unipv.posfw.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * La classe {@code DatabaseManager} implementa rigorosamente il design pattern creazionale Singleton 
 * 
 * Motivazione Architetturale: Nel contesto dello strato di accesso ai dati (Data Access Layer), 
 * è fondamentale gestire la persistenza in modo centralizzato. Il pattern Singleton risolve il problema 
 * di garantire che vi sia un'unica istanza della classe responsabile della lettura del file di configurazione 
 * e del caricamento del driver JDBC (Java Database Connectivity), fornendo a tutti i Data Access Object (DAO) 
 * un punto di accesso globale all'istanza. L'uso di un'istanza "normale" invece di soli metodi statici 
 * facilita l'evoluzione futura del sistema o il polimorfismo.
 * 
 * 
 * Le librerie di riferimento utilizzate appartengono a {@code java.sql}, implementando la connessione al DBMS 
 * tramite i Driver forniti dal produttore (specificamente un Type 4: All Java JDBC Driver per MySQL).
 * 
 * 
 * @author Simone e Lorenzo 
 * @version 1.2
 * @see java.sql.Connection
 * @see java.sql.DriverManager
 */
public class DatabaseManager {

    /**
     * Attributo statico che mantiene il riferimento all'unica istanza della classe.
     * È inizializzato a {@code null} finché il metodo statico {@link #getInstance()} non viene chiamato (Inizializzazione Lazy).
     */
    private static DatabaseManager instance;

    /**
     * Oggetto delegato al mantenimento delle proprietà di configurazione del database (URL, utente, password) 
     * disaccoppiate dal codice sorgente, lette da un file esterno.
     */
    private final Properties prop = new Properties();

    /**
     * Costruttore privato della classe {@code DatabaseManager}, fondamentale per imporre il vincolo del pattern Singleton 
     * impedendo l'istanziazione diretta dall'esterno.
     * 
     * Teoria delle Fonti: Durante la fase di inizializzazione, il costruttore si occupa di:
     * 
     *   Leggere il file strutturato {@code db.properties} contenente i parametri di connessione, applicando il costrutto {@code try-with-resources} per la gestione sicura dello stream di input.
     *   Abilitare il driver JDBC in modo che la Java Virtual Machine (JVM) lo carichi in memoria. Questo avviene tramite la Java Reflection mediante l'istruzione {@code Class.forName("com.mysql.cj.jdbc.Driver")}.
     * 
     * Le eccezioni intercettate vengono gestite e propagate sotto forma di eccezioni Unchecked ({@link RuntimeException}) 
     * in caso di anomalie bloccanti (es. file non trovato o driver assente).
     * 
     * 
     * @throws IllegalStateException se il file di configurazione non viene localizzato nel classpath.
     * @throws RuntimeException se si verifica un errore durante il caricamento delle proprietà o della classe del Driver.
     */
    private DatabaseManager() {
        try (InputStream input = getClass().getResourceAsStream("/db.properties")) {

            if (input == null) {
                // Partizione di non correttezza: file di configurazione assente.
                throw new IllegalStateException("File db.properties non trovato nella cartella src.");
            }

            // Caricamento dei parametri di connessione in memoria.
            prop.load(input);

            // Abilitazione del Driver JDBC specifico per MySQL tramite Java Reflection.
            Class.forName("com.mysql.cj.jdbc.Driver");

            System.out.println("Configurazione DB caricata correttamente.");

        } catch (Exception e) {
            // Propagazione dell'eccezione mascherandola come Unchecked Exception per interrompere il flusso
            // in caso di indisponibilità dei servizi tecnici fondamentali.
            throw new RuntimeException("Errore durante il caricamento della configurazione DB.", e);
        }
    }

    /**
     * Definisce il metodo statico (di classe) che fornisce il punto di accesso globale all'oggetto Singleton.
     * 
     * Motivazione Architetturale: Applica la logica dell'inizializzazione ritardata (Lazy Initialization). 
     * L'uso della parola chiave {@code synchronized} garantisce il mutuo scomparto: qualora molteplici thread 
     * tentassero simultaneamente di acquisire l'istanza per la prima volta, il blocco di codice verrà eseguito 
     * in modo sequenziale, prevenendo la creazione accidentale di istanze multiple che violerebbero il pattern.
     * 
     * 
     * @return L'unica istanza di {@link DatabaseManager} attiva nel sistema.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }

        return instance;
    }

    /**
     * Stabilisce ed eroga una connessione fisica verso il Database Management System (DBMS).
     * 
     * La connessione viene generata dal framework {@link java.sql.DriverManager} utilizzando 
     * la URL (connection string) precedentemente caricata, che astrae la locazione logica e di rete del database 
     * 
     * Questo metodo maschera l'eccezione Checked {@link java.sql.SQLException} convertendola in una 
     * {@link RuntimeException}. Questo evita di accoppiare eccessivamente la logica di business e le firme dei 
     * metodi delegati al livello superiore con le tipologie di eccezioni specifiche dei servizi tecnici di persistenza.
     * 
     * 
     * @return Un oggetto {@link java.sql.Connection} attivo e pronto per eseguire operazioni SQL.
     * @throws RuntimeException in caso di fallimento della negoziazione della connessione col DBMS.
     */
    public Connection getConnection() {
        try {
            // Apertura della connessione delegata al Driver Manager tramite Connection String.
            return DriverManager.getConnection(
                    prop.getProperty("db.url"),
                    prop.getProperty("db.user"),
                    prop.getProperty("db.password")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'apertura della connessione al DB.", e);
        }
    }
}