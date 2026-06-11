package it.unipv.posfw.dao;

import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.domain.StatoCorso;
import it.unipv.posfw.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CorsoDAOMySQL implements CorsoDAO {

    @Override
    public void insert(Corso c) {
        String sql = """
            INSERT INTO corso (
                id_corso,
                nome,
                stato,
                data_ora,
                posti_disponibili,
                capienza_massima,
                id_trainer_assegnato
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                nome = VALUES(nome),
                stato = VALUES(stato),
                data_ora = VALUES(data_ora),
                posti_disponibili = VALUES(posti_disponibili),
                capienza_massima = VALUES(capienza_massima),
                id_trainer_assegnato = VALUES(id_trainer_assegnato)
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, c.getIdCorso());
            stmt.setString(2, c.getNome());
            stmt.setString(3, c.getStato().name());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(c.getDataOra()));
            stmt.setInt(5, c.getPostiDisponibili());
            stmt.setInt(6, c.getCapienzaMassima());

            if (c.getTrainerAssegnato() == null) {
                stmt.setNull(7, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(7, c.getTrainerAssegnato().getIdTrainer());
            }

            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il salvataggio del corso su MySQL.", e);
        }
    }

    @Override
    public void delete(String idCorso) {
        /*
         * Cancellazione logica: coerente con UC3/UC5.
         * Il record resta nel database, ma non appare più come corso attivo.
         */
        String sql = """
            UPDATE corso
            SET stato = 'CANCELLATO'
            WHERE id_corso = ?
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, idCorso);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'annullamento logico del corso su MySQL.", e);
        }
    }

    @Override
    public Corso findById(String idCorso) {
        String sql = queryBase() + " WHERE c.id_corso = ?";

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, idCorso);

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
        String sql = queryBase() + " ORDER BY c.data_ora ASC";
        return eseguiLista(sql);
    }

    @Override
    public List<Corso> getPalinsesto() {
        String sql = queryBase()
                + " WHERE c.stato = 'ATTIVO' AND c.data_ora >= NOW()"
                + " ORDER BY c.data_ora ASC";
        return eseguiLista(sql);
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
            throw new RuntimeException("Errore durante la lettura del palinsesto corsi da MySQL.", e);
        }

        return listaCorsi;
    }

    private String queryBase() {
        return """
            SELECT
                c.id_corso,
                c.nome AS nome_corso,
                c.stato,
                c.data_ora,
                c.posti_disponibili,
                c.capienza_massima,
                pt.id_trainer,
                pt.specializzazione,
                pt.stato_contratto,
                pt.attivo AS trainer_attivo,
                u.nome AS nome_trainer,
                u.cognome AS cognome_trainer,
                u.email AS email_trainer
            FROM corso c
            LEFT JOIN personal_trainer pt
                ON c.id_trainer_assegnato = pt.id_trainer
            LEFT JOIN utente u
                ON pt.id_utente = u.id_utente
        """;
    }

    private Corso creaCorsoDaResultSet(ResultSet rs) throws Exception {
        PersonalTrainer trainer = null;

        String idTrainer = rs.getString("id_trainer");
        if (idTrainer != null) {
            trainer = new PersonalTrainer(
                    rs.getString("nome_trainer"),
                    rs.getString("cognome_trainer"),
                    rs.getString("email_trainer"),
                    idTrainer
            );
            trainer.setSpecializzazione(rs.getString("specializzazione"));
            trainer.setStatoContratto(rs.getString("stato_contratto"));
            trainer.setAttivo(rs.getBoolean("trainer_attivo"));
        }

        Corso corso = new Corso(
                rs.getString("id_corso"),
                rs.getString("nome_corso"),
                rs.getTimestamp("data_ora").toLocalDateTime(),
                rs.getInt("capienza_massima"),
                trainer
        );

        corso.setPostiDisponibili(rs.getInt("posti_disponibili"));
        corso.setStato(StatoCorso.valueOf(rs.getString("stato")));

        return corso;
    }
}
