package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Importiamo tutte le classi del dominio necessarie!
import it.unipv.posfw.domain.Utente;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.domain.PersonalTrainer; 
import it.unipv.posfw.domain.Direttore;

public class UtenteDAOMySQL implements UtenteDAO {

    // ==========================================
    // METODO PER IL LOGIN (AGGIORNATO CON TUTTI I RUOLI)
    // ==========================================
    @Override
    public Utente effettuaLogin(String email, String passwordInserita) {
        Utente utenteLoggato = null;
        String queryLogin = "SELECT * FROM Utente WHERE Email = ? AND PasswordHash = ?";
        
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(queryLogin)) {
             
            stmt.setString(1, email);
            stmt.setString(2, passwordInserita); 
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 1. Estraiamo i dati base comuni a tutti gli utenti
                    int idUtente = rs.getInt("ID_Utente");
                    String ruolo = rs.getString("Ruolo");
                    String nome = rs.getString("Nome");
                    String cognome = rs.getString("Cognome");
                    String emailDb = rs.getString("Email");
                    String cf = rs.getString("CodiceFiscale");
                    String stato = rs.getString("Stato");

                    // Controllo di sicurezza: se l'utente non è attivo, blocchiamo l'accesso
                    if (!"Attivo".equalsIgnoreCase(stato)) {
                        System.out.println("Utente trovato ma non attivo (" + stato + ")!");
                        return null; 
                    }

                    // 2. Creiamo l'oggetto specifico in base al ruolo
                    if (ruolo.equals("Cliente")) {
                        
                        // Recuperiamo l'abbonamento dal DB
                        TipoAbbonamento tipoAbb = recuperaAbbonamentoCliente(idUtente, conn);
                        
                        // Creiamo il cliente con il costruttore completo
                        Cliente cliente = new Cliente(nome, cognome, emailDb, cf, tipoAbb);
                        cliente.setId(idUtente); 
                        utenteLoggato = cliente;

                    } else if (ruolo.equals("PersonalTrainer")) {
                        // Creiamo il Trainer usando il costruttore sicuro (da 4 parametri)
                        PersonalTrainer trainer = new PersonalTrainer(nome, cognome, emailDb, cf);
                        trainer.setId(idUtente);
                        utenteLoggato = trainer;

                    } else if (ruolo.equals("Direttore")) {
                        // Recuperiamo il Codice Autorizzazione dalla tabella Direttore
                        String codiceAuth = recuperaAutorizzazioneDirettore(idUtente, conn);
                        Direttore direttore = new Direttore(nome, cognome, emailDb, codiceAuth);
                        direttore.setId(idUtente);
                        utenteLoggato = direttore;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il login: " + e.getMessage());
            e.printStackTrace();
        }

        return utenteLoggato;
    }

    // ==========================================
    // METODO DI SUPPORTO PER L'ABBONAMENTO
    // ==========================================
    private TipoAbbonamento recuperaAbbonamentoCliente(int idUtente, Connection conn) {
        String queryAbbonamento = "SELECT Tipo FROM Abbonamento WHERE ID_Cliente = ?";
        TipoAbbonamento tipoAssegnato = null; // Di default nullo
        
        try (PreparedStatement stmtAbb = conn.prepareStatement(queryAbbonamento)) {
            stmtAbb.setInt(1, idUtente);
            
            try (ResultSet rsAbb = stmtAbb.executeQuery()) {
                if (rsAbb.next()) {
                    String livelloDB = rsAbb.getString("Tipo"); 
                    if (livelloDB != null) {
                        // Converte la stringa del DB (es. "Premium") nell'Enum (PREMIUM)
                        tipoAssegnato = TipoAbbonamento.valueOf(livelloDB.toUpperCase());
                    }
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nel recupero dell'abbonamento per l'utente " + idUtente);
            e.printStackTrace();
        }
        
        return tipoAssegnato;
    }

    // ==========================================
    // METODO DI SUPPORTO PER IL DIRETTORE
    // ==========================================
    private String recuperaAutorizzazioneDirettore(int idUtente, Connection conn) {
        String queryAuth = "SELECT CodiceAutorizzazione FROM Direttore WHERE ID_Direttore = ?";
        String codiceTrovato = "Sconosciuto"; 
        
        try (PreparedStatement stmt = conn.prepareStatement(queryAuth)) {
            stmt.setInt(1, idUtente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    codiceTrovato = rs.getString("CodiceAutorizzazione");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero codice autorizzazione per il Direttore " + idUtente);
        }
        return codiceTrovato;
    }

    // ==========================================
    // METODO PER REGISTRARE UN NUOVO CLIENTE
    // ==========================================
    @Override
    public void registraCliente(String cf, String nome, String cognome, String email, String passwordHash) {
        
        String insertUtente = "INSERT INTO Utente (CodiceFiscale, Nome, Cognome, Email, PasswordHash, Ruolo) VALUES (?, ?, ?, ?, ?, 'Cliente')";
        String insertCliente = "INSERT INTO Cliente (ID_Cliente) VALUES (?)";
        
        Connection conn = DatabaseConnection.getConnection();

        try {
            conn.setAutoCommit(false); 

            PreparedStatement pstmtUtente = conn.prepareStatement(insertUtente, Statement.RETURN_GENERATED_KEYS);
            pstmtUtente.setString(1, cf);
            pstmtUtente.setString(2, nome);
            pstmtUtente.setString(3, cognome);
            pstmtUtente.setString(4, email);
            pstmtUtente.setString(5, passwordHash);
            
            pstmtUtente.executeUpdate();

            ResultSet rs = pstmtUtente.getGeneratedKeys();
            int idGenerato = -1;
            if (rs.next()) {
                idGenerato = rs.getInt(1); 
            }
            
            PreparedStatement pstmtCliente = conn.prepareStatement(insertCliente);
            pstmtCliente.setInt(1, idGenerato);
            pstmtCliente.executeUpdate();

            conn.commit();
            System.out.println("Perfetto! Cliente '" + nome + " " + cognome + "' registrato con ID: " + idGenerato);

            rs.close();
            pstmtUtente.close();
            pstmtCliente.close();

        } catch (SQLException e) {
            System.out.println("Errore durante la registrazione. Rollback in corso...");
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ==========================================
    // TEST AL VOLO
    // ==========================================
    public static void main(String[] args) {
        UtenteDAOMySQL dao = new UtenteDAOMySQL();
        
        // Simulo la registrazione di un cliente
        // dao.registraCliente("VRNLRN99M21F205W", "Lorenzo", "Varano", "lorenzo@studenti.unipv.it", "hash_finto_per_ora");
        
        // Test di Login (assicurati che questo utente esista nel tuo DB per vedere se funziona)
        System.out.println("Test di Login in corso...");
        Utente utente = dao.effettuaLogin("lorenzo@studenti.unipv.it", "hash_finto_per_ora");
        
        if (utente != null) {
            System.out.println("Login effettuato con successo! Benvenuto " + utente.getNome());
            if (utente instanceof Cliente) {
                Cliente c = (Cliente) utente;
                System.out.println("ID Cliente: " + c.getId()); // Questo dimostra che l'ID è stato pescato correttamente
                System.out.println("Abbonamento: " + c.getTipoAbbonamento());
            }
        } else {
            System.out.println("Login fallito. Credenziali errate.");
        }
    }
}