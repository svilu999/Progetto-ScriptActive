package it.unipv.poingsfw.database;

import it.unipv.poingsfw.dao.DirettoreDAO;
import it.unipv.poingsfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Implementazione MySQL del DAO dedicato al Direttore.
 *
 * La classe contiene solo accesso al database e query SQL.
 */
public class DirettoreDAOMySQL implements DirettoreDAO {

    /**
     * Recupera un identificativo valido di Direttore dalla tabella Direttore.
     *
     * @return identificativo del Direttore, oppure null se non esiste
     */
    @Override
    public Integer trovaIdDirettoreDisponibile() {
        String sql = """
            SELECT ID_Direttore
            FROM Direttore
            ORDER BY ID_Direttore
            LIMIT 1
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("ID_Direttore");
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il recupero del Direttore da MySQL.", e);
        }
    }
}