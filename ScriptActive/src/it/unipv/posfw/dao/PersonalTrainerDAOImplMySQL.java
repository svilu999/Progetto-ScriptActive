package it.unipv.posfw.dao;

import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.strategy.RetribuzioneFissa;
import it.unipv.posfw.strategy.RetribuzioneProvvigione;
import it.unipv.posfw.strategy.StrategiaRetribuzione;
import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonalTrainerDAOImplMySQL implements PersonalTrainerDAO {

    @Override
    public void salva(PersonalTrainer pt) {
        String sqlUtente = """
            INSERT INTO utente (nome, cognome, email, password, ruolo, attivo)
            VALUES (?, ?, ?, ?, 'PERSONAL_TRAINER', ?)
            ON DUPLICATE KEY UPDATE
                id_utente = LAST_INSERT_ID(id_utente),
                nome = VALUES(nome),
                cognome = VALUES(cognome),
                ruolo = VALUES(ruolo),
                attivo = VALUES(attivo)
        """;

        String sqlPT = """
            INSERT INTO personal_trainer (
                id_trainer,
                id_utente,
                specializzazione,
                stato_contratto,
                stipendio_mensile,
                attivo
            )
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                specializzazione = VALUES(specializzazione),
                stato_contratto = VALUES(stato_contratto),
                stipendio_mensile = VALUES(stipendio_mensile),
                attivo = VALUES(attivo)
        """;

        String sqlContratto = """
            INSERT INTO contratto_personale (
                id_trainer,
                data_inizio,
                data_fine,
                stipendio_mensile,
                stato,
                tipo_retribuzione,
                compenso_per_lezione
            )
            VALUES (?, CURRENT_DATE, NULL, ?, 'ATTIVO', ?, ?)
            ON DUPLICATE KEY UPDATE
                stipendio_mensile = VALUES(stipendio_mensile),
                stato = VALUES(stato),
                tipo_retribuzione = VALUES(tipo_retribuzione),
                compenso_per_lezione = VALUES(compenso_per_lezione)
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            int idUtente;

            try (PreparedStatement stmtUtente = conn.prepareStatement(sqlUtente, Statement.RETURN_GENERATED_KEYS)) {
                stmtUtente.setString(1, pt.getNome());
                stmtUtente.setString(2, pt.getCognome());
                stmtUtente.setString(3, pt.getEmail());
                stmtUtente.setString(4, "1234");
                stmtUtente.setBoolean(5, pt.isAttivo());

                stmtUtente.executeUpdate();

                try (ResultSet rs = stmtUtente.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Impossibile recuperare id_utente.");
                    }
                    idUtente = rs.getInt(1);
                }
            }

            double stipendioMensile = 0.00;
            Double compensoPerLezione = null;
            String tipoRetribuzione = "FISSA_MENSILE";

            if (pt.getStrategia() != null) {
                String tipo = pt.getStrategia().getTipoContratto();

                if ("Fisso".equalsIgnoreCase(tipo)) {
                    tipoRetribuzione = "FISSA_MENSILE";
                    stipendioMensile = pt.getStrategia().calcolaStipendio(0);
                    compensoPerLezione = null;
                } else {
                    tipoRetribuzione = "A_LEZIONE";
                    stipendioMensile = 0.00;
                    compensoPerLezione = pt.getStrategia().calcolaStipendio(1);
                }
            }

            try (PreparedStatement stmtPT = conn.prepareStatement(sqlPT)) {
                stmtPT.setString(1, pt.getIdTrainer());
                stmtPT.setInt(2, idUtente);
                stmtPT.setString(3, pt.getSpecializzazione());
                stmtPT.setString(4, pt.getStatoContratto());
                stmtPT.setDouble(5, stipendioMensile);
                stmtPT.setBoolean(6, pt.isAttivo());

                stmtPT.executeUpdate();
            }

            try (PreparedStatement stmtContratto = conn.prepareStatement(sqlContratto)) {
                stmtContratto.setString(1, pt.getIdTrainer());
                stmtContratto.setDouble(2, stipendioMensile);
                stmtContratto.setString(3, tipoRetribuzione);

                if (compensoPerLezione == null) {
                    stmtContratto.setNull(4, java.sql.Types.DECIMAL);
                } else {
                    stmtContratto.setDouble(4, compensoPerLezione);
                }

                stmtContratto.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il salvataggio del Personal Trainer su MySQL.", e);
        }
    }

    @Override
    public PersonalTrainer trovaPerId(String idPT) {
        String sql = """
            SELECT
                pt.id_trainer,
                u.nome,
                u.cognome,
                u.email,
                pt.specializzazione,
                pt.stato_contratto,
                pt.attivo,
                cp.tipo_retribuzione,
                cp.stipendio_mensile,
                cp.compenso_per_lezione
            FROM personal_trainer pt
            JOIN utente u ON pt.id_utente = u.id_utente
            LEFT JOIN contratto_personale cp
                ON pt.id_trainer = cp.id_trainer
               AND cp.stato = 'ATTIVO'
            WHERE pt.id_trainer = ?
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, idPT);

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
        String sqlPT = """
            UPDATE personal_trainer
            SET specializzazione = ?,
                stato_contratto = ?,
                attivo = ?
            WHERE id_trainer = ?
        """;

        String sqlUtente = """
            UPDATE utente u
            JOIN personal_trainer pt ON u.id_utente = pt.id_utente
            SET u.attivo = ?
            WHERE pt.id_trainer = ?
        """;

        String sqlChiudiContratto = """
            UPDATE contratto_personale
            SET stato = 'TERMINATO',
                data_fine = CURRENT_DATE
            WHERE id_trainer = ?
              AND stato = 'ATTIVO'
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlPT)) {
                stmt.setString(1, pt.getSpecializzazione());
                stmt.setString(2, pt.getStatoContratto());
                stmt.setBoolean(3, pt.isAttivo());
                stmt.setString(4, pt.getIdTrainer());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlUtente)) {
                stmt.setBoolean(1, pt.isAttivo());
                stmt.setString(2, pt.getIdTrainer());
                stmt.executeUpdate();
            }

            if (!pt.isAttivo() || "LICENZIATO".equalsIgnoreCase(pt.getStatoContratto())) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlChiudiContratto)) {
                    stmt.setString(1, pt.getIdTrainer());
                    stmt.executeUpdate();
                }
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'aggiornamento del Personal Trainer su MySQL.", e);
        }
    }

    @Override
    public void elimina(String idPT) {
        PersonalTrainer pt = trovaPerId(idPT);

        if (pt != null) {
            pt.setStatoContratto("LICENZIATO");
            pt.setAttivo(false);
            aggiorna(pt);
        }
    }

    @Override
    public List<PersonalTrainer> trovaTutti() {
        List<PersonalTrainer> lista = new ArrayList<>();

        String sql = """
            SELECT
                pt.id_trainer,
                u.nome,
                u.cognome,
                u.email,
                pt.specializzazione,
                pt.stato_contratto,
                pt.attivo,
                cp.tipo_retribuzione,
                cp.stipendio_mensile,
                cp.compenso_per_lezione
            FROM personal_trainer pt
            JOIN utente u ON pt.id_utente = u.id_utente
            LEFT JOIN contratto_personale cp
                ON pt.id_trainer = cp.id_trainer
               AND cp.stato = 'ATTIVO'
            ORDER BY pt.id_trainer
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

    private PersonalTrainer creaPersonalTrainerDaResultSet(ResultSet rs) throws Exception {
        String tipoRetribuzione = rs.getString("tipo_retribuzione");

        StrategiaRetribuzione strategia;

        if ("A_LEZIONE".equalsIgnoreCase(tipoRetribuzione)) {
            double compenso = rs.getDouble("compenso_per_lezione");
            strategia = new RetribuzioneProvvigione(compenso);
        } else {
            double stipendio = rs.getDouble("stipendio_mensile");
            strategia = new RetribuzioneFissa(stipendio);
        }

        PersonalTrainer pt = new PersonalTrainer(
            rs.getString("nome"),
            rs.getString("cognome"),
            rs.getString("email"),
            rs.getString("id_trainer"),
            rs.getString("specializzazione"),
            strategia
        );

        pt.setStatoContratto(rs.getString("stato_contratto"));
        pt.setAttivo(rs.getBoolean("attivo"));

        return pt;
    }
}
