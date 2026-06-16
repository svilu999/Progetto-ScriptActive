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

import it.unipv.posfw.domain.SessioneAllenamento;
import it.unipv.posfw.domain.DatiFormPojo;

public class SessioneDAOSQL implements SessioneDAO {

    // DATI DI CONNESSIONE
    private static final String URL = "jdbc:mysql://localhost:3306/scriptactive_db";
    private static final String USER = "root";
    private static final String PASS = "Enomis23*";

    public SessioneDAOSQL() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trovato! Assicurati di aver aggiunto il JAR.");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    @Override
    public boolean salvaSessione(SessioneAllenamento sessione) {
        // SOLUZIONE: Uso una subquery per inserire l'ID_Utente numerico partendo dal CodiceFiscale (Stringa)
        String sqlSessione = "INSERT INTO SessioneAllenamento (Data, ID_Cliente) VALUES (?, (SELECT ID_Utente FROM Utente WHERE CodiceFiscale = ?))";
        String sqlEsercizio = "INSERT INTO EsercizioRegistrato (GruppoMuscolare, Macchinario, Serie, Ripetizioni, Carico, ID_Sessione) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtSess = conn.prepareStatement(sqlSessione, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmtSess.setDate(1, new java.sql.Date(sessione.getData().getTime()));
                // Ora possiamo passare direttamente la stringa senza che vada in crash
                pstmtSess.setString(2, sessione.getIdCliente()); 
                pstmtSess.executeUpdate();

                int idSessioneGenerato = -1;
                try (ResultSet generatedKeys = pstmtSess.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idSessioneGenerato = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione sessione fallita, nessun ID ottenuto.");
                    }
                }

                try (PreparedStatement pstmtEsercizio = conn.prepareStatement(sqlEsercizio)) {
                    for (DatiFormPojo esercizio : sessione.getEsercizi()) {
                        pstmtEsercizio.setString(1, "Generico"); 
                        pstmtEsercizio.setString(2, esercizio.getNomeEsercizio()); 
                        pstmtEsercizio.setInt(3, 1); 
                        pstmtEsercizio.setInt(4, esercizio.getRipetizioni());
                        pstmtEsercizio.setDouble(5, esercizio.getCarichi());
                        pstmtEsercizio.setInt(6, idSessioneGenerato); 
                        pstmtEsercizio.addBatch(); 
                    }
                    pstmtEsercizio.executeBatch(); 
                }

                conn.commit(); 
                return true;

            } catch (SQLException ex) {
                conn.rollback(); 
                System.err.println("Errore durante il salvataggio della sessione: " + ex.getMessage());
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

        // SOLUZIONE: Join con Utente per poter filtrare direttamente per CodiceFiscale
        String sql = "SELECT s.ID_Sessione, s.Data, u.CodiceFiscale, e.Macchinario, e.Carico, e.Ripetizioni " +
                     "FROM SessioneAllenamento s " +
                     "JOIN Utente u ON s.ID_Cliente = u.ID_Utente " +
                     "LEFT JOIN EsercizioRegistrato e ON s.ID_Sessione = e.ID_Sessione " +
                     "WHERE u.CodiceFiscale = ? ORDER BY s.Data DESC, s.ID_Sessione DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idCliente); // Passiamo il Codice Fiscale in formato stringa

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int idSess = rs.getInt("ID_Sessione");

                    if (!mappaSessioni.containsKey(idSess)) {
                        SessioneAllenamento sess = new SessioneAllenamento(rs.getDate("Data"), rs.getString("CodiceFiscale"));
                        sess.setIdSessione(idSess); 
                        mappaSessioni.put(idSess, sess);
                    }

                    String macchinario = rs.getString("Macchinario");
                    if (macchinario != null) { 
                        DatiFormPojo esercizio = new DatiFormPojo(macchinario, rs.getDouble("Carico"), rs.getInt("Ripetizioni"));
                        mappaSessioni.get(idSess).aggiungiEsercizio(esercizio);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(mappaSessioni.values());
    }

    @Override
    public boolean eliminaSessioneSpecifica(SessioneAllenamento sessione) {
        String sql = "DELETE FROM SessioneAllenamento WHERE ID_Sessione = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessione.getIdSessione());
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}