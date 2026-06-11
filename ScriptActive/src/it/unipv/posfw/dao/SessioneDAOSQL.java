package it.unipv.posfw.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import provaview.SessioneDAO;


public class SessioneDAOSQL implements SessioneDAO {

    // DATI DI CONNESSIONE AL TUO DATABASE MYSQL
    private static final String URL = "jdbc:mysql://localhost:3306/scriptactive_db";
    private static final String USER = "root";
    private static final String PASS = "Enomis23*"; // <-- Ricordati di inserire la tua vera password!

    public SessioneDAOSQL() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trovato! Assicurati di aver aggiunto il JAR al Build Path.");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    @Override
    public boolean salvaSessione(SessioneAllenamento sessione) {
        // Le query aggiornate con i veri nomi delle tue tabelle e colonne
        String sqlSessione = "INSERT INTO SessioneAllenamento (Data, ID_Cliente) VALUES (?, ?)";
        String sqlEsercizio = "INSERT INTO EsercizioRegistrato (GruppoMuscolare, Macchinario, Serie, Ripetizioni, Carico, ID_Sessione) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Inizio transazione

            try (PreparedStatement pstmtSess = conn.prepareStatement(sqlSessione, Statement.RETURN_GENERATED_KEYS)) {
                
                // 1. Salviamo la Sessione Padre
                pstmtSess.setDate(1, new java.sql.Date(sessione.getData().getTime()));
                // Converte l'ID da String ("001") a Integer per rispettare il tuo DB
                pstmtSess.setInt(2, Integer.parseInt(sessione.getIdCliente())); 
                pstmtSess.executeUpdate();

                // Recuperiamo l'ID generato dal database (AUTO_INCREMENT)
                int idSessioneGenerato = -1;
                try (ResultSet generatedKeys = pstmtSess.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idSessioneGenerato = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione sessione fallita, nessun ID ottenuto.");
                    }
                }

                // 2. Salviamo gli Esercizi Figli
                try (PreparedStatement pstmtEsercizio = conn.prepareStatement(sqlEsercizio)) {
                    for (DatiForm esercizio : sessione.getEsercizi()) {
                        pstmtEsercizio.setString(1, "Generico"); // Default: aggiungerlo alla UI in futuro
                        pstmtEsercizio.setString(2, esercizio.getNomeEsercizio()); // Salvato in 'Macchinario'
                        pstmtEsercizio.setInt(3, 1); // Default Serie a 1
                        pstmtEsercizio.setInt(4, esercizio.getRipetizioni());
                        pstmtEsercizio.setDouble(5, esercizio.getCarichi());
                        pstmtEsercizio.setInt(6, idSessioneGenerato); // Collegamento Foreign Key
                        pstmtEsercizio.addBatch(); 
                    }
                    pstmtEsercizio.executeBatch(); 
                }

                conn.commit(); 
                return true;

            } catch (SQLException | NumberFormatException ex) {
                conn.rollback(); 
                System.err.println("Errore durante il salvataggio o conversione ID Cliente: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SessioneAllenamento> getStorico(String idCliente) {
        Map<Integer, SessioneAllenamento> mappaSessioni = new LinkedHashMap<>();

        // JOIN aggiornata sul tuo nuovo schema
        String sql = "SELECT s.ID_Sessione, s.Data, s.ID_Cliente, e.Macchinario, e.Carico, e.Ripetizioni " +
                     "FROM SessioneAllenamento s " +
                     "LEFT JOIN EsercizioRegistrato e ON s.ID_Sessione = e.ID_Sessione " +
                     "WHERE s.ID_Cliente = ? ORDER BY s.Data DESC, s.ID_Sessione DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Converte in numero l'ID che arriva dalla View
            pstmt.setInt(1, Integer.parseInt(idCliente));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int idSess = rs.getInt("ID_Sessione");

                    if (!mappaSessioni.containsKey(idSess)) {
                        SessioneAllenamento sess = new SessioneAllenamento(rs.getDate("Data"), String.valueOf(rs.getInt("ID_Cliente")));
                        sess.setIdSessione(idSess); 
                        mappaSessioni.put(idSess, sess);
                    }

                    // Attenzione: ora il nome esercizio lo leggiamo dal campo 'Macchinario'
                    String macchinario = rs.getString("Macchinario");
                    if (macchinario != null) { 
                        DatiForm esercizio = new DatiForm(macchinario, rs.getDouble("Carico"), rs.getInt("Ripetizioni"));
                        mappaSessioni.get(idSess).aggiungiEsercizio(esercizio);
                    }
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(mappaSessioni.values());
    }

    @Override
    public boolean eliminaSessioneSpecifica(SessioneAllenamento sessione) {
        // Grazie al ON DELETE CASCADE della tua Foreign Key, cancellando la Sessione spariscono anche gli esercizi!
        String sql = "DELETE FROM SessioneAllenamento WHERE ID_Sessione = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessione.getIdSessione());
            int righeCancellate = pstmt.executeUpdate();
            
            return righeCancellate > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}