package it.unipv.poingsfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.dao.SedeDAO;
import it.unipv.poingsfw.domain.Sede;
import it.unipv.poingsfw.util.DatabaseManager;

/**
 * Implementazione concreta per l'accesso ai dati (Data Access Object) dell'entità {@link Sede}.
 * <p>
 * <b>Contesto Architetturale:</b><br>
 * Risiede nel livello di Persistenza e gestisce le operazioni 
 * relative alle sedi geografiche del sistema. Nasconde la logica di accesso al DBMS MySQL 
 * restituendo al livello superiore (Controller/View) collezioni di oggetti Java.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class SedeDAOMySQL implements SedeDAO {

	/**
     * Inserisce un nuovo record fisico all'interno della tabella Sede (Operazione CREATE).
     * <p>
     * L'inserimento utilizza un {@code PreparedStatement} parametrico per blindare 
     * il sistema contro i tentativi di SQL Injection sul campo testuale del nome.
     * </p>
     * * @param nomeSede La stringa descrittiva (es. indirizzo o città) della nuova filiale.
     */
	
	public void aggiungiSede(String nomeSede) {
        String query = "INSERT INTO Sede (NomeSede) VALUES (?)";

        try {
            // Prendiamo la connessione FUORI dal try-with-resources per NON farla chiudere!
            Connection conn = DatabaseManager.getInstance().getConnection();
            
            // Mettiamo nel try-with-resources solo il PreparedStatement
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, nomeSede);
                pstmt.executeUpdate();
                
                System.out.println("Sede '" + nomeSede + "' salvata con successo in MySQL!");
            }
            
        } catch (SQLException e) {
            System.out.println("Errore durante l'inserimento della sede: " + e.getMessage());
        }
    }

	/**
     * Metodo che consente allo sviluppatore di verificare il popolamento del database senza 
     * transitare per le interfacce grafiche.
     */
	

    public void stampaTutteLeSedi() {
        String query = "SELECT ID_Sede, NomeSede FROM Sede";

        try {
        	Connection conn = DatabaseManager.getInstance().getConnection();
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                System.out.println("--- ELENCO SEDI DAL DATABASE ---");
                while (rs.next()) {
                    int id = rs.getInt("ID_Sede");
                    String nome = rs.getString("NomeSede");
                    System.out.println("ID: " + id + " | Nome: " + nome);
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Errore in stampaTutteLeSedi: " + e.getMessage());
        }
    }
    
    /**
     * Estrae tutti i record presenti nella tabella 'Sede' e applica il pattern 
     * Object-Relational Mapping (ORM) manuale.
     * <p>
     * Questo metodo è vitale per la logica di Presentazione (la View), in quanto 
     * fornisce la lista tipizzata necessaria a popolare componenti UI come i Menu a tendina 
     * (es. JComboBox) in fase di registrazione (Use Case UC1).
     * </p>
     * * @return Una collezione (List) di oggetti {@link Sede}. Se la tabella è vuota, restituisce una lista vuota.
     */
    
    public List<Sede> getTutteLeSedi() {
        List<Sede> listaSedi = new ArrayList<>();
        String query = "SELECT ID_Sede, NomeSede FROM Sede";

        try {
        	Connection conn = DatabaseManager.getInstance().getConnection();
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    Sede s = new Sede(rs.getInt("ID_Sede"), rs.getString("NomeSede"));
                    listaSedi.add(s);
                }
            }
        } catch (SQLException e) {
            System.out.println("Errore in getTutteLeSedi: " + e.getMessage());
        }
        return listaSedi;
    }

    // ==========================================
    // TEST AL VOLO
    // ==========================================
    public static void main(String[] args) {
        SedeDAOMySQL dao = new SedeDAOMySQL();
        
        dao.aggiungiSede("Palestra Milano Nord");
        
        dao.stampaTutteLeSedi();
    }
}