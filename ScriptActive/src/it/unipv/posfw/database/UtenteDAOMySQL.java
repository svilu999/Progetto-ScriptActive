package it.unipv.posfw.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.unipv.posfw.domain.Utente;
import it.unipv.posfw.util.DatabaseManager;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.domain.PersonalTrainer; 
import it.unipv.posfw.domain.Direttore;
import it.unipv.posfw.dao.UtenteDAO;
import it.unipv.posfw.domain.Abbonamento;
import it.unipv.posfw.domain.LivelloAbbonamento;

public class UtenteDAOMySQL implements UtenteDAO {

    // ==========================================
    // 1. METODO PER IL LOGIN (AGGIORNATO CON TUTTI I RUOLI)
    // ==========================================
    @Override
    public Utente effettuaLogin(String email, String passwordInserita) {
        Utente utenteLoggato = null;
        String queryLogin = "SELECT * FROM Utente WHERE Email = ? AND PasswordHash = ?";
        
        Connection conn = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(queryLogin)) {
             
            stmt.setString(1, email);
            stmt.setString(2, passwordInserita); 
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idUtente = rs.getInt("ID_Utente");
                    String ruolo = rs.getString("Ruolo");
                    String nome = rs.getString("Nome");
                    String cognome = rs.getString("Cognome");
                    String emailDb = rs.getString("Email");
                    String cf = rs.getString("CodiceFiscale");
                    String stato = rs.getString("Stato");

                    if (ruolo.equals("Cliente")) {
                        Abbonamento abbCompleto = recuperaAbbonamentoCompleto(idUtente, cf, conn);
                        TipoAbbonamento tipoAbb = (abbCompleto != null) ? abbCompleto.getTipo() : null;
                        
                        Cliente cliente = new Cliente(nome, cognome, emailDb, cf, tipoAbb);
                        cliente.setId(idUtente); 
                        cliente.setAbbonamentoAttivo(abbCompleto);
                        
                        utenteLoggato = cliente;

                    } else if (ruolo.equals("PersonalTrainer")) {
                        PersonalTrainer trainer = new PersonalTrainer(nome, cognome, emailDb, cf);
                        trainer.setId(idUtente);
                        utenteLoggato = trainer;

                    } else if (ruolo.equals("Direttore")) {
                        String codiceAuth = recuperaAutorizzazioneDirettore(idUtente, conn);
                        Direttore direttore = new Direttore(nome, cognome, emailDb, codiceAuth);
                        direttore.setId(idUtente);
                        utenteLoggato = direttore;
                    }

                    // Impostiamo lo stato nell'oggetto Java
                    if (utenteLoggato != null) {
                        utenteLoggato.setStato(stato);
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
    // 2. METODO DI SUPPORTO PER L'ABBONAMENTO
    // ==========================================
    private Abbonamento recuperaAbbonamentoCompleto(int idUtente, String cf, Connection conn) {
        // AGGIUNTO RinnovoAutomatico ALLA SELECT
        String queryAbbonamento = "SELECT Tipo, Livello, DataScadenza, RinnovoAutomatico FROM Abbonamento WHERE ID_Cliente = ?";
        Abbonamento abbonamentoTrovato = null;
        
        try (PreparedStatement stmtAbb = conn.prepareStatement(queryAbbonamento)) {
            stmtAbb.setInt(1, idUtente);
            
            try (ResultSet rsAbb = stmtAbb.executeQuery()) {
                if (rsAbb.next()) {
                    String tipoDB = rsAbb.getString("Tipo"); 
                    String livelloDB = rsAbb.getString("Livello"); 
                    java.sql.Date dataScadenzaDB = rsAbb.getDate("DataScadenza");
                    // LEGGIAMO IL VALORE REALE DAL DB (0 o 1 diventa false o true)
                    boolean rinnovoAutoDB = rsAbb.getBoolean("RinnovoAutomatico");
                    
                    TipoAbbonamento tipoEnum = null;
                    LivelloAbbonamento livelloEnum = null;
                    
                    if (tipoDB != null) {
                        tipoEnum = TipoAbbonamento.valueOf(tipoDB.toUpperCase());
                    }
                    if (livelloDB != null) {
                        livelloEnum = LivelloAbbonamento.valueOf(livelloDB.toUpperCase());
                    }
                    
                    // PASSIAMO IL VALORE REALE AL COSTRUTTORE
                    abbonamentoTrovato = new Abbonamento(cf, livelloEnum, tipoEnum, rinnovoAutoDB, "");
                    
                    if (dataScadenzaDB != null) {
                        abbonamentoTrovato.setDataScadenza(new java.util.Date(dataScadenzaDB.getTime()));
                    }
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nel recupero dell'abbonamento per l'utente " + idUtente);
            e.printStackTrace();
        }
        
        return abbonamentoTrovato;
    }
    // ==========================================
    // 3. METODO DI SUPPORTO PER IL DIRETTORE
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
    // 4. METODO DI REGISTRAZIONE
    // ==========================================
    @Override
    public void registraCliente(String cf, String nome, String cognome, String email, String passwordHash) {
        
        String insertUtente = "INSERT INTO Utente (CodiceFiscale, Nome, Cognome, Email, PasswordHash, Ruolo) VALUES (?, ?, ?, ?, ?, 'Cliente')";
        String insertCliente = "INSERT INTO Cliente (ID_Cliente) VALUES (?)";
        
        Connection conn = DatabaseManager.getInstance().getConnection();

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
    // METODO PER ESEGUIRE IL RINNOVO NEL DB
    // ==========================================
 // ==========================================
    // METODO PER ESEGUIRE IL RINNOVO NEL DB
    // ==========================================
 // ==========================================
    // METODO PER ESEGUIRE IL RINNOVO NEL DB
    // ==========================================
    public void eseguiRinnovo(String email, int mesiAggiuntivi, String nuovoPiano) {
        
        // 1. TRADUTTORE: Puliamo la stringa per il database!
        // Se riceve "Annuale - €360.00", estrapola solo "ANNUALE"
        String livelloDB = "MENSILE"; // default
        if (nuovoPiano != null) {
            String pianoUpper = nuovoPiano.toUpperCase();
            if (pianoUpper.contains("ANNUALE")) {
                livelloDB = "ANNUALE";
            } else if (pianoUpper.contains("SEMESTRALE")) {
                livelloDB = "SEMESTRALE";
            }
        }
        
        // 2. LA QUERY SQL
        String queryUpdate = "UPDATE Abbonamento SET DataScadenza = DATE_ADD(CURRENT_DATE, INTERVAL ? MONTH), Livello = ? " +
                             "WHERE ID_Cliente = (SELECT ID_Utente FROM Utente WHERE Email = ?)";
                             
        Connection conn = DatabaseManager.getInstance().getConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(queryUpdate)) {
            stmt.setInt(1, mesiAggiuntivi);
            stmt.setString(2, livelloDB); // <-- Usiamo la parola pulita e perfetta per MySQL!
            stmt.setString(3, email);
            
            int righeModificate = stmt.executeUpdate();
            if(righeModificate > 0) {
                System.out.println("✅ Rinnovo completato con successo per: " + email);
            } else {
                System.out.println("❌ Nessuna riga modificata. Sicura che l'email sia giusta?");
            }
        } catch (SQLException e) {
            System.err.println("Errore SQL durante l'aggiornamento dell'abbonamento.");
            e.printStackTrace();
        }
    }
 // ========================================================
    // METODO PER LO SBLOCCO SILENZIOSO (Scavalca i controlli)
    // ========================================================
 // ========================================================
    // METODO PER LO SBLOCCO SILENZIOSO (Versione con Debug)
    // ========================================================
 // ========================================================
    // METODO PER LO SBLOCCO SILENZIOSO (Lettura Numerica)
    // ========================================================
 // ========================================================
    // METODO PER LO SBLOCCO SILENZIOSO (Versione Definitiva)
    // ========================================================
    public boolean tentaRinnovoSilenzioso(String email) {
        String query = "SELECT a.RinnovoAutomatico, a.Livello FROM Abbonamento a JOIN Utente u ON a.ID_Cliente = u.ID_Utente WHERE u.Email = ?";
        
        try {
            java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            java.sql.ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int rinnovoAuto = rs.getInt("RinnovoAutomatico");
                String livello = rs.getString("Livello");
                
                if (rinnovoAuto == 1) { 
                    int mesi = 1;
                    if (livello != null && livello.toUpperCase().contains("SEMESTRALE")) mesi = 6;
                    if (livello != null && livello.toUpperCase().contains("ANNUALE")) mesi = 12;
                    
                    eseguiRinnovo(email, mesi, livello);
                    return true; 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; 
    }
}