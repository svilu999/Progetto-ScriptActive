package it.unipv.posfw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.unipv.posfw.database.DatabaseConnection;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.PersonalTrainer;

public class CorsoDAOMySQL implements CorsoDAO {

    @Override
    public void insert(Corso c) {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(String idCorso) {
        // TODO Auto-generated method stub
    }

    @Override
    public Corso findById(String idCorso) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Corso> findAll() {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    @Override
    public List<Corso> getPalinsesto() {
        List<Corso> listaCorsi = new ArrayList<>();
        
        // 1. SELECT allargata per prendere anche ID e Capienza
        String query = "SELECT c.ID_Corso, c.Nome AS NomeCorso, c.DataOra, c.CapienzaMassima, c.PostiDisponibili, " +
                "s.NomeSede, u.Nome AS NomeTrainer, u.Cognome AS CognomeTrainer, u.Email AS EmailTrainer " + // <-- ECCOLA QUI
                "FROM Corso c " +
                "JOIN Sede s ON c.ID_Sede = s.ID_Sede " +
                "JOIN PersonalTrainer pt ON c.ID_Trainer = pt.ID_Trainer " +
                "JOIN Utente u ON pt.ID_Trainer = u.ID_Utente " +
                "WHERE c.Stato = 'Pianificato' " +
                "ORDER BY c.DataOra ASC";

        Connection conn = DatabaseConnection.getConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // 1. Estraiamo i dati del Corso
                String idCorso = String.valueOf(rs.getInt("ID_Corso"));
                String nomeCorso = rs.getString("NomeCorso");
                java.time.LocalDateTime dataOra = rs.getTimestamp("DataOra").toLocalDateTime();
                int capienza = rs.getInt("CapienzaMassima");
                
                // 2. Estraiamo i dati del Trainer necessari per il suo costruttore
                String idTrainer = String.valueOf(rs.getInt("ID_Trainer")); // O la colonna corretta dell'ID del trainer
                String nomeTrainer = rs.getString("NomeTrainer");
                String cognomeTrainer = rs.getString("CognomeTrainer");
                String emailTrainer = rs.getString("EmailTrainer");

                // 3. Istanziamo il PersonalTrainer sfruttando il suo VERO costruttore
                PersonalTrainer trainer = new PersonalTrainer(nomeTrainer, cognomeTrainer, emailTrainer, idTrainer);
                // 4. Creiamo il Corso passandogli il trainer appena configurato
                Corso corso = new Corso(idCorso, nomeCorso, dataOra, capienza, trainer);
                
                // Aggiungiamo l'oggetto alla lista per la tua GUI
                listaCorsi.add(corso);
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listaCorsi;
    }
}