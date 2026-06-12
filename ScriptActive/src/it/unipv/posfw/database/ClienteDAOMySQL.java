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
import it.unipv.posfw.domain.LivelloAbbonamento; // IMPORTATO

public class ClienteDAOMySQL implements ClienteDAO {

    @Override
    public Cliente getClienteByCF(String codiceFiscale) {
        // Cerchiamo il codice fiscale nella tabella padre "Utente"
        String query = "SELECT * FROM Utente WHERE CodiceFiscale = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, codiceFiscale);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Se lo trova, ritorna un Cliente con i dati base. Questo farà scattare l'Eccezione nel Controller!
                    return new Cliente(
                        rs.getString("Nome"), 
                        rs.getString("Cognome"), 
                        rs.getString("Email"), 
                        rs.getString("CodiceFiscale"), 
                        null, // La sede la lasciamo a null in questo controllo
                        TipoAbbonamento.BASE 
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore in getClienteByCF: " + e.getMessage());
        }
        return null; // Ritorna null se il CF non esiste nel database (il cliente è nuovo)
    }

    @Override
    public boolean inserisciCliente(Cliente c) {
        Connection conn = null;
        try {
            // 0. Apriamo la connessione e DISATTIVIAMO l'autocommit per la Transazione
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); 

            // ==========================================================
            // STEP 1: Inserimento nella tabella padre "Utente"
            // ==========================================================
            String insertUtente = "INSERT INTO Utente (CodiceFiscale, Nome, Cognome, Email, PasswordHash, Ruolo, Stato) VALUES (?, ?, ?, ?, ?, 'Cliente', 'Attivo')";
            
            // Statement.RETURN_GENERATED_KEYS ci serve per farci restituire da MySQL l'ID_Utente appena creato
            PreparedStatement psUtente = conn.prepareStatement(insertUtente, Statement.RETURN_GENERATED_KEYS);
            psUtente.setString(1, c.getCodiceFiscale());
            psUtente.setString(2, c.getNome());
            psUtente.setString(3, c.getCognome());
            psUtente.setString(4, c.getEmail());
            psUtente.setString(5, c.getPassword()); // Assicurati di passare la password corretta
            
            psUtente.executeUpdate();
            
            // Recuperiamo l'ID_Utente autogenerato (AUTO_INCREMENT)
            ResultSet rsKeys = psUtente.getGeneratedKeys();
            int idGenerato = -1;
            if (rsKeys.next()) {
                idGenerato = rsKeys.getInt(1);
            }
            psUtente.close();

            // ==========================================================
            // STEP 2: Inserimento nella tabella figlia "Cliente"
            // ==========================================================
            String insertCliente = "INSERT INTO Cliente (ID_Cliente) VALUES (?)";
            PreparedStatement psCliente = conn.prepareStatement(insertCliente);
            psCliente.setInt(1, idGenerato); // Usiamo lo stesso ID della tabella Utente!
            psCliente.executeUpdate();
            psCliente.close();

            // ==========================================================
            // STEP 3: Inserimento nella tabella "Abbonamento"
            // ==========================================================
            Abbonamento abb = c.getAbbonamentoAttivo();
            int idAbbonamentoGenerato = -1;
            
            if (abb != null) {
                String insertAbbonamento = "INSERT INTO Abbonamento (Tipo, Livello, DataScadenza, RinnovoAutomatico, IBAN, ID_Cliente) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement psAbb = conn.prepareStatement(insertAbbonamento, Statement.RETURN_GENERATED_KEYS);
                
                // .name() o .toString() salvano la stringa dell'enum (es. "BASE", "PREMIUM", "MENSILE"...) nel DB
                psAbb.setString(1, abb.getTipo().name());
                psAbb.setString(2, abb.getLivello().name());
                psAbb.setDate(3, new java.sql.Date(abb.getDataScadenza().getTime()));
                psAbb.setBoolean(4, false); // Rinnovo disattivato di default per ora
                psAbb.setString(5, abb.getIban());
                psAbb.setInt(6, idGenerato); // Lo colleghiamo all'ID del cliente appena creato
                
                psAbb.executeUpdate();
                
                // Recuperiamo l'ID_Abbonamento appena creato per collegarci il pagamento
                ResultSet rsAbbKeys = psAbb.getGeneratedKeys();
                if (rsAbbKeys.next()) {
                    idAbbonamentoGenerato = rsAbbKeys.getInt(1);
                }
                psAbb.close();

                // ==========================================================
                // STEP 4: Inserimento della Ricevuta in "Pagamento"
                // ==========================================================
                String insertPagamento = "INSERT INTO Pagamento (Importo, DataTransazione, Esito, ID_Abbonamento) VALUES (?, NOW(), 'Successo', ?)";
                PreparedStatement psPag = conn.prepareStatement(insertPagamento);
                
                // CORRETTO: Adesso il controllo usa il tipo corretto di Enum (TipoAbbonamento) tramite confronto diretto tipo Java
                double importo = (abb.getTipo() == TipoAbbonamento.PREMIUM) ? 65.0 : 50.0;
                
                psPag.setDouble(1, importo);
                psPag.setInt(2, idAbbonamentoGenerato);
                psPag.executeUpdate();
                psPag.close();
            }

            // ==========================================================
            // STEP 5: COMMIT (Se tutto è andato liscio, saldiamo i dati)
            // ==========================================================
            conn.commit();
            System.out.println("[DB REAL] Inserimento gerarchico completato con successo su MySQL!");
            return true;

        } catch (SQLException e) {
            // SE QUALCOSA VA STORTO (es. email duplicata), IL ROLLBACK ANNULLA TUTTO!
            System.err.println("Errore SQL, eseguo ROLLBACK. Dettagli: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); 
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // Liberiamo le risorse
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Questi due puoi lasciarli "Mock" per ora se il Caso d'Uso 1 (Registrazione) non li richiede
    @Override
    public void updateCliente(Cliente c) {
        System.out.println("[DB] Funzione Update non ancora implementata.");
    }

    @Override
    public void deleteCliente(String codiceFiscale) {
        System.out.println("[DB] Funzione Delete non ancora implementata.");
    }
}