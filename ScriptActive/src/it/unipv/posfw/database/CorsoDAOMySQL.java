package it.unipv.posfw.database;

import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.domain.StatoCorso;
import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CorsoDAOMySQL implements CorsoDAO {

    @Override
    public void insert(Corso c) {
        /*
         * Metodo usato sia per creare/salvare corsi sia per rendere persistente
         * lo swap.
         *
         * Schema comune:
         * Corso(
         *   ID_Corso,
         *   Nome,
         *   DataOra,
         *   CapienzaMassima,
         *   PostiDisponibili,
         *   Stato,
         *   ID_Sede,
         *   ID_Trainer,
         *   ID_Direttore
         * )
         */
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {

            Integer idCorso = estraiIdNumerico(c.getIdCorso());

            if (idCorso != null && esisteCorso(conn, idCorso)) {
                aggiornaCorso(conn, c, idCorso);
            } else {
                inserisciCorso(conn, c, idCorso);
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il salvataggio del corso su MySQL.", e);
        }
    }

    @Override
    public void delete(String idCorso) {
        /*
         * Cancellazione logica.
         * Nel DB comune il corso annullato ha Stato = 'Annullato'.
         */
        String sql = """
            UPDATE Corso
            SET Stato = 'Annullato'
            WHERE ID_Corso = ?
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, Integer.parseInt(idCorso));
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'annullamento logico del corso su MySQL.", e);
        }
    }

    @Override
    public Corso findById(String idCorso) {
        String sql = queryBase() + " WHERE c.ID_Corso = ?";

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, Integer.parseInt(idCorso));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creaCorsoDaResultSet(rs);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca del corso su MySQL.", e);
        }
    }

    @Override
    public List<Corso> findAll() {
        String sql = queryBase() + " ORDER BY c.DataOra ASC";
        return eseguiLista(sql);
    }

    @Override
    public List<Corso> getPalinsesto() {
        String sql = queryBase()
                + " WHERE c.Stato IN ('Pianificato', 'InCorso')"
                + " AND c.DataOra >= NOW()"
                + " ORDER BY c.DataOra ASC";

        return eseguiLista(sql);
    }

    @Override
    public void updatePostiDisponibili(Corso corso) {
        /*
         * Metodo richiesto da UC3.
         * Aggiorna solo i posti disponibili del corso.
         */
        String sql = """
            UPDATE Corso
            SET PostiDisponibili = ?
            WHERE ID_Corso = ?
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, corso.getPostiDisponibili());
            stmt.setInt(2, Integer.parseInt(corso.getIdCorso()));

            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'aggiornamento dei posti disponibili del corso su MySQL.", e);
        }
    }

    private boolean esisteCorso(Connection conn, int idCorso) throws Exception {
        String sql = "SELECT COUNT(*) AS totale FROM Corso WHERE ID_Corso = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCorso);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt("totale") > 0;
            }
        }
    }

    private void aggiornaCorso(Connection conn, Corso c, int idCorso) throws Exception {
        /*
         * Aggiornamento usato anche dallo swap.
         * Quando cambia il PT assegnato, aggiorniamo ID_Trainer.
         * Il corso resta nel palinsesto e non viene cancellato.
         */
        String sql = """
            UPDATE Corso
            SET Nome = ?,
                DataOra = ?,
                CapienzaMassima = ?,
                PostiDisponibili = ?,
                Stato = ?,
                ID_Trainer = ?
            WHERE ID_Corso = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getNome());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(c.getDataOra()));
            stmt.setInt(3, c.getCapienzaMassima());
            stmt.setInt(4, c.getPostiDisponibili());
            stmt.setString(5, statoJavaToDb(c.getStato()));
            stmt.setInt(6, idTrainerObbligatorio(c));
            stmt.setInt(7, idCorso);

            stmt.executeUpdate();
        }
    }

    private void inserisciCorso(Connection conn, Corso c, Integer idCorso) throws Exception {
        /*
         * Inserimento compatibile con lo schema comune.
         * ID_Sede, ID_Trainer e ID_Direttore sono obbligatori.
         *
         * Se non esistono Sede/Direttore, vengono creati record tecnici
         * minimi per evitare errori di foreign key durante i test.
         */
        String sql = """
            INSERT INTO Corso (
                ID_Corso,
                Nome,
                DataOra,
                CapienzaMassima,
                PostiDisponibili,
                Stato,
                ID_Sede,
                ID_Trainer,
                ID_Direttore
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        int idSede = recuperaOCreaSedePredefinita(conn);
        int idDirettore = recuperaOCreaDirettorePredefinito(conn);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (idCorso == null) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, idCorso);
            }

            stmt.setString(2, c.getNome());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(c.getDataOra()));
            stmt.setInt(4, c.getCapienzaMassima());
            stmt.setInt(5, c.getPostiDisponibili());
            stmt.setString(6, statoJavaToDb(c.getStato()));
            stmt.setInt(7, idSede);
            stmt.setInt(8, idTrainerObbligatorio(c));
            stmt.setInt(9, idDirettore);

            stmt.executeUpdate();
        }
    }

    private List<Corso> eseguiLista(String sql) {
        List<Corso> listaCorsi = new ArrayList<>();

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                listaCorsi.add(creaCorsoDaResultSet(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la lettura dei corsi da MySQL.", e);
        }

        return listaCorsi;
    }

    private String queryBase() {
        return """
            SELECT
                c.ID_Corso,
                c.Nome AS NomeCorso,
                c.Stato AS StatoCorso,
                c.DataOra,
                c.PostiDisponibili,
                c.CapienzaMassima,

                pt.ID_Trainer,
                pt.Specializzazione,
                pt.TipoContratto,
                pt.StatoContratto,
                pt.Attivo AS TrainerAttivo,
                pt.TipoRetribuzione,
                pt.StipendioMensile,
                pt.CompensoPerLezione,

                u.Nome AS NomeTrainer,
                u.Cognome AS CognomeTrainer,
                u.Email AS EmailTrainer

            FROM Corso c
            LEFT JOIN PersonalTrainer pt
                ON c.ID_Trainer = pt.ID_Trainer
            LEFT JOIN Utente u
                ON pt.ID_Trainer = u.ID_Utente
        """;
    }

    private Corso creaCorsoDaResultSet(ResultSet rs) throws Exception {
        PersonalTrainer trainer = null;

        int idTrainer = rs.getInt("ID_Trainer");

        if (!rs.wasNull()) {
            trainer = new PersonalTrainer(
                    rs.getString("NomeTrainer"),
                    rs.getString("CognomeTrainer"),
                    rs.getString("EmailTrainer"),
                    String.valueOf(idTrainer)
            );

            trainer.setSpecializzazione(rs.getString("Specializzazione"));
            trainer.setStatoContratto(rs.getString("StatoContratto"));
            trainer.setAttivo(rs.getBoolean("TrainerAttivo"));
        }

        Corso corso = new Corso(
                String.valueOf(rs.getInt("ID_Corso")),
                rs.getString("NomeCorso"),
                rs.getTimestamp("DataOra").toLocalDateTime(),
                rs.getInt("CapienzaMassima"),
                trainer
        );

        corso.setPostiDisponibili(rs.getInt("PostiDisponibili"));
        corso.setStato(statoDbToJava(rs.getString("StatoCorso")));

        return corso;
    }

    private StatoCorso statoDbToJava(String statoDb) {
        if (statoDb == null) {
            return StatoCorso.ATTIVO;
        }

        return switch (statoDb) {
            case "Annullato" -> StatoCorso.CANCELLATO;
            case "Completato" -> StatoCorso.COMPLETO;
            case "Pianificato", "InCorso" -> StatoCorso.ATTIVO;
            default -> StatoCorso.ATTIVO;
        };
    }

    private String statoJavaToDb(StatoCorso statoJava) {
        if (statoJava == null) {
            return "Pianificato";
        }

        return switch (statoJava) {
            case CANCELLATO -> "Annullato";
            case COMPLETO -> "Completato";
            case ATTIVO -> "Pianificato";
        };
    }

    private int idTrainerObbligatorio(Corso c) {
        if (c.getTrainerAssegnato() == null || c.getTrainerAssegnato().getIdTrainer() == null) {
            throw new IllegalArgumentException("Il corso deve avere un Personal Trainer assegnato.");
        }

        Integer idTrainer = estraiIdNumerico(c.getTrainerAssegnato().getIdTrainer());

        if (idTrainer == null) {
            throw new IllegalArgumentException(
                    "ID Trainer non valido: " + c.getTrainerAssegnato().getIdTrainer()
            );
        }

        return idTrainer;
    }

    private Integer estraiIdNumerico(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        String soloNumeri = id.replaceAll("[^0-9]", "");

        if (soloNumeri.isBlank()) {
            return null;
        }

        return Integer.parseInt(soloNumeri);
    }

    private int recuperaOCreaSedePredefinita(Connection conn) throws Exception {
        String select = "SELECT ID_Sede FROM Sede ORDER BY ID_Sede LIMIT 1";

        try (
            PreparedStatement stmt = conn.prepareStatement(select);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("ID_Sede");
            }
        }

        String insert = "INSERT INTO Sede (NomeSede) VALUES ('Sede Principale')";

        try (PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new RuntimeException("Impossibile creare una Sede predefinita.");
    }

    private int recuperaOCreaDirettorePredefinito(Connection conn) throws Exception {
        String select = "SELECT ID_Direttore FROM Direttore ORDER BY ID_Direttore LIMIT 1";

        try (
            PreparedStatement stmt = conn.prepareStatement(select);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("ID_Direttore");
            }
        }

        String insertUtente = """
            INSERT INTO Utente (
                CodiceFiscale,
                Nome,
                Cognome,
                Email,
                PasswordHash,
                Ruolo,
                Stato
            )
            VALUES (
                'DIR0000000000001',
                'Direttore',
                'Sistema',
                'direttore.sistema@scriptactive.local',
                '1234',
                'Direttore',
                'Attivo'
            )
        """;

        int idDirettore;

        try (PreparedStatement stmt = conn.prepareStatement(insertUtente, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException("Impossibile creare l'utente Direttore predefinito.");
                }

                idDirettore = rs.getInt(1);
            }
        }

        String insertDirettore = """
            INSERT INTO Direttore (
                ID_Direttore,
                CodiceAutorizzazione
            )
            VALUES (?, 'DIR-SISTEMA')
            ON DUPLICATE KEY UPDATE
                CodiceAutorizzazione = VALUES(CodiceAutorizzazione)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(insertDirettore)) {
            stmt.setInt(1, idDirettore);
            stmt.executeUpdate();
        }

        return idDirettore;
    }
}