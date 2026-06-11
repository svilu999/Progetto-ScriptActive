package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.unipv.posfw.dao.CorsoDAO;
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
        
        // 1. SELECT CORRETTA: Ho aggiunto c.ID_Trainer subito dopo c.ID_Corso!
        String query = "SELECT c.ID_Corso, c.ID_Trainer, c.Nome AS NomeCorso, c.DataOra, c.CapienzaMassima, c.PostiDisponibili, " +
                "s.NomeSede, u.Nome AS NomeTrainer, u.Cognome AS CognomeTrainer, u.Email AS EmailTrainer " + 
                "FROM Corso c " +
                "JOIN Sede s ON c.ID_Sede = s.ID_Sede " +
                "JOIN PersonalTrainer pt ON c.ID_Trainer = pt.ID_Trainer " +
                "JOIN Utente u ON pt.ID_Trainer = u.ID_Utente " +
                "WHERE c.Stato = 'Pianificato' " +
                "ORDER BY c.DataOra ASC";

        Connection conn = it.unipv.posfw.util.DatabaseManager.getInstance().getConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // 1. Estraiamo i dati del Corso
                String idCorso = String.valueOf(rs.getInt("ID_Corso"));
                String nomeCorso = rs.getString("NomeCorso");
                java.time.LocalDateTime dataOra = rs.getTimestamp("DataOra").toLocalDateTime();
                int capienza = rs.getInt("CapienzaMassima");
                
                // Mettiamo da parte i posti veri estratti dal DB
                int postiVeri = rs.getInt("PostiDisponibili");
                
                // 2. Estraiamo i dati del Trainer (ORA FUNZIONA!)
                String idTrainer = String.valueOf(rs.getInt("ID_Trainer")); 
                String nomeTrainer = rs.getString("NomeTrainer");
                String cognomeTrainer = rs.getString("CognomeTrainer");
                String emailTrainer = rs.getString("EmailTrainer");

                // 3. Istanziamo il PersonalTrainer
                PersonalTrainer trainer = new PersonalTrainer(nomeTrainer, cognomeTrainer, emailTrainer, idTrainer);
                
                // 4. Creiamo il Corso
                Corso corso = new Corso(idCorso, nomeCorso, dataOra, capienza, trainer);
                
                // 5. Inseriamo i posti disponibili reali prima di mandarlo all'interfaccia
                corso.setPostiDisponibili(postiVeri); 
                
                listaCorsi.add(corso);
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("Errore durante getPalinsesto: " + e.getMessage());
            e.printStackTrace();
        }

        return listaCorsi;
    }
    
    @Override
    public void updatePostiDisponibili(Corso c) {
        String query = "UPDATE Corso SET postiDisponibili = ? WHERE ID_Corso = ?";
        Connection conn = it.unipv.posfw.util.DatabaseManager.getInstance().getConnection();
        
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, c.getPostiDisponibili());
            pstmt.setString(2, c.getIdCorso());
            
            int righeModificate = pstmt.executeUpdate();
            if (righeModificate == 0) {
                System.err.println("Attenzione: Nessun corso aggiornato nel DB. L'ID esiste?");
            }
            
        } catch (java.sql.SQLException e) {
            System.err.println("Errore DAO (updatePostiDisponibili): " + e.getMessage());
        }
    }
}