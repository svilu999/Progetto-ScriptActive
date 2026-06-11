package it.unipv.posfw.dao;

import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.strategy.RetribuzioneFissa;
import it.unipv.posfw.strategy.RetribuzioneProvvigione;
import it.unipv.posfw.strategy.StrategiaRetribuzione;
import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonalTrainerDAOImplMySQL implements PersonalTrainerDAO {

    @Override
    public void salva(PersonalTrainer pt) {
        /*
         * DAO aggiornato per lo schema comune:
         *
         * Utente:
         * - ID_Utente
         * - CodiceFiscale
         * - Nome
         * - Cognome
         * - Email
         * - PasswordHash
         * - Ruolo
         * - Stato
         *
         * PersonalTrainer:
         * - ID_Trainer
         * - Specializzazione
         * - TipoContratto
         * - StatoContratto
         * - Attivo
         * - TipoRetribuzione
         * - StipendioMensile
         * - CompensoPerLezione
         * - ID_Direttore
         *
         * Non viene più usata la tabella contratto_personale.
         */

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            int idUtente = salvaOAggiornaUtente(conn, pt);
            int idDirettore = recuperaOCreaDirettorePredefinito(conn);

            DatiRetribuzione datiRetribuzione = calcolaDatiRetribuzione(pt);

            String sqlPT = """
                INSERT INTO PersonalTrainer (
                    ID_Trainer,
                    Specializzazione,
                    TipoContratto,
                    StatoContratto,
                    Attivo,
                    TipoRetribuzione,
                    StipendioMensile,
                    CompensoPerLezione,
                    ID_Direttore
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    Specializzazione = VALUES(Specializzazione),
                    TipoContratto = VALUES(TipoContratto),
                    StatoContratto = VALUES(StatoContratto),
                    Attivo = VALUES(Attivo),
                    TipoRetribuzione = VALUES(TipoRetribuzione),
                    StipendioMensile = VALUES(StipendioMensile),
                    CompensoPerLezione = VALUES(CompensoPerLezione),
                    ID_Direttore = VALUES(ID_Direttore)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlPT)) {
                stmt.setInt(1, idUtente);
                stmt.setString(2, pt.getSpecializzazione());
                stmt.setString(3, datiRetribuzione.tipoContratto);
                stmt.setString(4, normalizzaStatoContratto(pt.getStatoContratto()));
                stmt.setBoolean(5, pt.isAttivo());
                stmt.setString(6, datiRetribuzione.tipoRetribuzione);
                stmt.setDouble(7, datiRetribuzione.stipendioMensile);

                if (datiRetribuzione.compensoPerLezione == null) {
                    stmt.setNull(8, java.sql.Types.DECIMAL);
                } else {
                    stmt.setDouble(8, datiRetribuzione.compensoPerLezione);
                }

                stmt.setInt(9, idDirettore);

                stmt.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il salvataggio del Personal Trainer su MySQL.", e);
        }
    }

    @Override
    public PersonalTrainer trovaPerId(String idPT) {
        Integer idTrainer = estraiIdNumerico(idPT);

        if (idTrainer == null) {
            return null;
        }

        String sql = """
            SELECT
                pt.ID_Trainer,
                u.Nome,
                u.Cognome,
                u.Email,
                pt.Specializzazione,
                pt.TipoContratto,
                pt.StatoContratto,
                pt.Attivo,
                pt.TipoRetribuzione,
                pt.StipendioMensile,
                pt.CompensoPerLezione
            FROM PersonalTrainer pt
            JOIN Utente u
                ON pt.ID_Trainer = u.ID_Utente
            WHERE pt.ID_Trainer = ?
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, idTrainer);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creaPersonalTrainerDaResultSet(rs);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca del Personal Trainer su MySQL.", e);
        }
    }

    @Override
    public void aggiorna(PersonalTrainer pt) {
        Integer idTrainer = estraiIdNumerico(pt.getIdTrainer());

        if (idTrainer == null) {
            throw new IllegalArgumentException("ID Personal Trainer non valido: " + pt.getIdTrainer());
        }

        DatiRetribuzione datiRetribuzione = calcolaDatiRetribuzione(pt);

        String sqlPT = """
            UPDATE PersonalTrainer
            SET Specializzazione = ?,
                TipoContratto = ?,
                StatoContratto = ?,
                Attivo = ?,
                TipoRetribuzione = ?,
                StipendioMensile = ?,
                CompensoPerLezione = ?
            WHERE ID_Trainer = ?
        """;

        String sqlUtente = """
            UPDATE Utente
            SET Nome = ?,
                Cognome = ?,
                Email = ?,
                Stato = ?
            WHERE ID_Utente = ?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlPT)) {
                stmt.setString(1, pt.getSpecializzazione());
                stmt.setString(2, datiRetribuzione.tipoContratto);
                stmt.setString(3, normalizzaStatoContratto(pt.getStatoContratto()));
                stmt.setBoolean(4, pt.isAttivo());
                stmt.setString(5, datiRetribuzione.tipoRetribuzione);
                stmt.setDouble(6, datiRetribuzione.stipendioMensile);

                if (datiRetribuzione.compensoPerLezione == null) {
                    stmt.setNull(7, java.sql.Types.DECIMAL);
                } else {
                    stmt.setDouble(7, datiRetribuzione.compensoPerLezione);
                }

                stmt.setInt(8, idTrainer);

                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlUtente)) {
                stmt.setString(1, pt.getNome());
                stmt.setString(2, pt.getCognome());
                stmt.setString(3, pt.getEmail());
                stmt.setString(4, pt.isAttivo() ? "Attivo" : "Inattivo");
                stmt.setInt(5, idTrainer);

                stmt.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'aggiornamento del Personal Trainer su MySQL.", e);
        }
    }

    @Override
    public void elimina(String idPT) {
        /*
         * Soft delete coerente con UC5:
         * il PT non viene eliminato fisicamente dal database.
         * Viene marcato come LICENZIATO e non attivo.
         */
        Integer idTrainer = estraiIdNumerico(idPT);

        if (idTrainer == null) {
            throw new IllegalArgumentException("ID Personal Trainer non valido: " + idPT);
        }

        String sqlPT = """
            UPDATE PersonalTrainer
            SET StatoContratto = 'LICENZIATO',
                Attivo = FALSE
            WHERE ID_Trainer = ?
        """;

        String sqlUtente = """
            UPDATE Utente
            SET Stato = 'Inattivo'
            WHERE ID_Utente = ?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlPT)) {
                stmt.setInt(1, idTrainer);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlUtente)) {
                stmt.setInt(1, idTrainer);
                stmt.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la disattivazione del Personal Trainer su MySQL.", e);
        }
    }

    @Override
    public List<PersonalTrainer> trovaTutti() {
        List<PersonalTrainer> lista = new ArrayList<>();

        String sql = """
            SELECT
                pt.ID_Trainer,
                u.Nome,
                u.Cognome,
                u.Email,
                pt.Specializzazione,
                pt.TipoContratto,
                pt.StatoContratto,
                pt.Attivo,
                pt.TipoRetribuzione,
                pt.StipendioMensile,
                pt.CompensoPerLezione
            FROM PersonalTrainer pt
            JOIN Utente u
                ON pt.ID_Trainer = u.ID_Utente
            ORDER BY pt.ID_Trainer
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(creaPersonalTrainerDaResultSet(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la lettura dei Personal Trainer da MySQL.", e);
        }

        return lista;
    }

    private int salvaOAggiornaUtente(Connection conn, PersonalTrainer pt) throws Exception {
        /*
         * Siccome nello schema comune ID_Trainer coincide con ID_Utente,
         * prima salviamo l'utente e poi usiamo lo stesso ID per PersonalTrainer.
         */

        Integer idEsistenteDaEmail = trovaIdUtenteDaEmail(conn, pt.getEmail());

        if (idEsistenteDaEmail != null) {
            aggiornaUtente(conn, idEsistenteDaEmail, pt);
            return idEsistenteDaEmail;
        }

        String sqlUtente = """
            INSERT INTO Utente (
                CodiceFiscale,
                Nome,
                Cognome,
                Email,
                PasswordHash,
                Ruolo,
                Stato
            )
            VALUES (?, ?, ?, ?, ?, 'PersonalTrainer', ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sqlUtente, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, generaCodiceFiscaleTecnico(pt));
            stmt.setString(2, pt.getNome());
            stmt.setString(3, pt.getCognome());
            stmt.setString(4, pt.getEmail());
            stmt.setString(5, "1234");
            stmt.setString(6, pt.isAttivo() ? "Attivo" : "Inattivo");

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException("Impossibile recuperare ID_Utente dopo inserimento.");
                }

                return rs.getInt(1);
            }
        }
    }

    private void aggiornaUtente(Connection conn, int idUtente, PersonalTrainer pt) throws Exception {
        String sql = """
            UPDATE Utente
            SET Nome = ?,
                Cognome = ?,
                Email = ?,
                Ruolo = 'PersonalTrainer',
                Stato = ?
            WHERE ID_Utente = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pt.getNome());
            stmt.setString(2, pt.getCognome());
            stmt.setString(3, pt.getEmail());
            stmt.setString(4, pt.isAttivo() ? "Attivo" : "Inattivo");
            stmt.setInt(5, idUtente);

            stmt.executeUpdate();
        }
    }

    private Integer trovaIdUtenteDaEmail(Connection conn, String email) throws Exception {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT ID_Utente FROM Utente WHERE Email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_Utente");
                }
            }
        }

        return null;
    }

    private int recuperaOCreaDirettorePredefinito(Connection conn) throws Exception {
        /*
         * Serve perché nello schema comune PersonalTrainer richiede ID_Direttore.
         * Se esiste già un direttore, usa quello.
         * Se non esiste, crea un direttore tecnico di sistema.
         */
        String selectDirettore = """
            SELECT ID_Direttore
            FROM Direttore
            ORDER BY ID_Direttore
            LIMIT 1
        """;

        try (
            PreparedStatement stmt = conn.prepareStatement(selectDirettore);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("ID_Direttore");
            }
        }

        Integer idUtenteDirettore = trovaIdUtenteDaEmail(conn, "direttore.sistema@scriptactive.local");

        if (idUtenteDirettore == null) {
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

            try (PreparedStatement stmt = conn.prepareStatement(insertUtente, Statement.RETURN_GENERATED_KEYS)) {
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Impossibile creare l'utente direttore predefinito.");
                    }

                    idUtenteDirettore = rs.getInt(1);
                }
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
            stmt.setInt(1, idUtenteDirettore);
            stmt.executeUpdate();
        }

        return idUtenteDirettore;
    }

    private PersonalTrainer creaPersonalTrainerDaResultSet(ResultSet rs) throws Exception {
        String tipoRetribuzione = rs.getString("TipoRetribuzione");

        StrategiaRetribuzione strategia;

        if ("A_LEZIONE".equalsIgnoreCase(tipoRetribuzione)) {
            double compenso = rs.getDouble("CompensoPerLezione");
            strategia = new RetribuzioneProvvigione(compenso);
        } else {
            double stipendio = rs.getDouble("StipendioMensile");
            strategia = new RetribuzioneFissa(stipendio);
        }

        PersonalTrainer pt = new PersonalTrainer(
                rs.getString("Nome"),
                rs.getString("Cognome"),
                rs.getString("Email"),
                String.valueOf(rs.getInt("ID_Trainer")),
                rs.getString("Specializzazione"),
                strategia
        );

        pt.setStatoContratto(rs.getString("StatoContratto"));
        pt.setAttivo(rs.getBoolean("Attivo"));

        return pt;
    }

    private DatiRetribuzione calcolaDatiRetribuzione(PersonalTrainer pt) {
        double stipendioMensile = 0.00;
        Double compensoPerLezione = null;
        String tipoRetribuzione = "FISSA_MENSILE";
        String tipoContratto = "Fisso";

        if (pt.getStrategia() != null) {
            String tipo = pt.getStrategia().getTipoContratto();

            if ("Fisso".equalsIgnoreCase(tipo)) {
                tipoRetribuzione = "FISSA_MENSILE";
                tipoContratto = "Fisso";
                stipendioMensile = pt.getStrategia().calcolaStipendio(0);
                compensoPerLezione = null;
            } else {
                tipoRetribuzione = "A_LEZIONE";
                tipoContratto = "Provvigione";
                stipendioMensile = 0.00;
                compensoPerLezione = pt.getStrategia().calcolaStipendio(1);
            }
        }

        return new DatiRetribuzione(
                tipoContratto,
                tipoRetribuzione,
                stipendioMensile,
                compensoPerLezione
        );
    }

    private String normalizzaStatoContratto(String statoContratto) {
        if (statoContratto == null || statoContratto.isBlank()) {
            return "ATTIVO";
        }

        return statoContratto;
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

    private String generaCodiceFiscaleTecnico(PersonalTrainer pt) {
        /*
         * Lo schema richiede CodiceFiscale NOT NULL e UNIQUE.
         * La view UC5 non gestisce il codice fiscale, quindi generiamo
         * un codice tecnico stabile partendo dall'email.
         */
        String base = pt.getEmail();

        if (base == null || base.isBlank()) {
            base = pt.getNome() + "." + pt.getCognome() + "." + System.nanoTime();
        }

        long hash = Math.abs((long) base.hashCode());
        String numeri = String.format("%014d", hash % 100000000000000L);

        return "PT" + numeri;
    }

    private static class DatiRetribuzione {
        private final String tipoContratto;
        private final String tipoRetribuzione;
        private final double stipendioMensile;
        private final Double compensoPerLezione;

        private DatiRetribuzione(
                String tipoContratto,
                String tipoRetribuzione,
                double stipendioMensile,
                Double compensoPerLezione
        ) {
            this.tipoContratto = tipoContratto;
            this.tipoRetribuzione = tipoRetribuzione;
            this.stipendioMensile = stipendioMensile;
            this.compensoPerLezione = compensoPerLezione;
        }
    }
}