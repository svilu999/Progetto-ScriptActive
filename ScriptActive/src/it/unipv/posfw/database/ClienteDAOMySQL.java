package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.unipv.posfw.dao.ClienteDAO;
import it.unipv.posfw.domain.Abbonamento;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.domain.LivelloAbbonamento; 

public class ClienteDAOMySQL implements ClienteDAO {

    @Override
    public Cliente getClienteByCF(String codiceFiscale) {
        String query = "SELECT * FROM Utente WHERE CodiceFiscale = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, codiceFiscale);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Cliente c = new Cliente(
                    rs.getString("Nome"), 
                    rs.getString("Cognome"), 
                    rs.getString("Email"), 
                    rs.getString("CodiceFiscale"), 
                    null, 
                    TipoAbbonamento.BASE 
                );
                rs.close();
                ps.close();
                return c;
            }
            rs.close();
            ps.close();
            
        } catch (SQLException e) {
            System.err.println("Errore in getClienteByCF: " + e.getMessage());
        }
        return null; 
    }

    @Override
    public boolean inserisciCliente(Cliente c) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); 

            String insertUtente = "INSERT INTO Utente (CodiceFiscale, Nome, Cognome, Email, PasswordHash, Ruolo, Stato) VALUES (?, ?, ?, ?, ?, 'Cliente', 'Attivo')";
            PreparedStatement psUtente = conn.prepareStatement(insertUtente, Statement.RETURN_GENERATED_KEYS);
            psUtente.setString(1, c.getCodiceFiscale());
            psUtente.setString(2, c.getNome());
            psUtente.setString(3, c.getCognome());
            psUtente.setString(4, c.getEmail());
            psUtente.setString(5, c.getPassword()); 
            
            psUtente.executeUpdate();
            
            ResultSet rsKeys = psUtente.getGeneratedKeys();
            int idGenerato = -1;
            if (rsKeys.next()) {
                idGenerato = rsKeys.getInt(1);
            }
            psUtente.close();

            String insertCliente = "INSERT INTO Cliente (ID_Cliente, ID_Sede) VALUES (?, ?)";
            PreparedStatement psCliente = conn.prepareStatement(insertCliente);
            psCliente.setInt(1, idGenerato); 
            psCliente.setInt(2, c.getSedePrincipale().getIdSede()); 
            psCliente.executeUpdate();
            psCliente.close();

            Abbonamento abb = c.getAbbonamentoAttivo();
            int idAbbonamentoGenerato = -1;
            
            if (abb != null) {
                String insertAbbonamento = "INSERT INTO Abbonamento (Tipo, Livello, DataScadenza, RinnovoAutomatico, IBAN, ID_Cliente) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement psAbb = conn.prepareStatement(insertAbbonamento, Statement.RETURN_GENERATED_KEYS);
                
                psAbb.setString(1, abb.getTipo().name());
                psAbb.setString(2, abb.getLivello().name());
                psAbb.setDate(3, new java.sql.Date(abb.getDataScadenza().getTime()));
                
                // ECCO LA STAMPA DI DEBUG INSERITA CORRETTAMENTE!
                System.out.println("[DEBUG DAO] Il valore dentro l'oggetto Abbonamento prima di salvare è: " + abb.isRinnovoAutomatico());
                
                psAbb.setBoolean(4, abb.isRinnovoAutomatico()); 
                psAbb.setString(5, abb.getIban());
                psAbb.setInt(6, idGenerato); 
                
                psAbb.executeUpdate();
                
                ResultSet rsAbbKeys = psAbb.getGeneratedKeys();
                if (rsAbbKeys.next()) {
                    idAbbonamentoGenerato = rsAbbKeys.getInt(1);
                }
                psAbb.close();

                String insertPagamento = "INSERT INTO Pagamento (Importo, DataTransazione, Esito, ID_Abbonamento) VALUES (?, NOW(), 'Successo', ?)";
                PreparedStatement psPag = conn.prepareStatement(insertPagamento);
                
                double importoFinale = 0.0;
                if (abb.getLivello() == LivelloAbbonamento.MENSILE) importoFinale = 50.0;
                else if (abb.getLivello() == LivelloAbbonamento.SEMESTRALE) importoFinale = 250.0;
                else if (abb.getLivello() == LivelloAbbonamento.ANNUALE) importoFinale = 450.0;
                
                if (abb.getTipo() == TipoAbbonamento.PREMIUM) {
                    importoFinale += 15.0; 
                }
                
                psPag.setDouble(1, importoFinale);
                psPag.setInt(2, idAbbonamentoGenerato);
                psPag.executeUpdate();
                psPag.close();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.err.println("Errore SQL, eseguo ROLLBACK. Dettagli: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); 
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } 
    }

    @Override
    public void updateCliente(Cliente c) {}

    @Override
    public void deleteCliente(String codiceFiscale) {}
}