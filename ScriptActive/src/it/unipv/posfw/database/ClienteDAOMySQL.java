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
import it.unipv.posfw.util.DatabaseManager;
import it.unipv.posfw.domain.LivelloAbbonamento; 

/**
 * Implementazione concreta dell'interfaccia {@link ClienteDAO} per il DBMS MySQL.
 * <p>
 * Applica il pattern <b>Data Access Object (DAO)</b> incapsulando la logica di accesso tramite le API JDBC.
 * Si occupa di tradurre il paradigma Object-Oriented (oggetti di dominio) nel paradigma Relazionale (tabelle DB),
 * nascondendo la complessità delle query SQL e la gestione delle transazioni al resto dell'applicazione.
 * Si appoggia al Singleton {@link DatabaseManager} per il reperimento delle risorse di rete (connessioni).
 * </p>
 * * @author Vilucchi
 * @version 1.1
 * @see it.unipv.posfw.dao.ClienteDAO
 */
public class ClienteDAOMySQL implements ClienteDAO {

    /**
     * Recupera l'entità di dominio {@link Cliente} filtrando per la chiave naturale (Codice Fiscale).
     * <p>
     * Esegue una query di proiezione ed esegue l'Object-Relational Mapping (ORM) manuale 
     * istanziando l'oggetto Cliente con i dati ricavati dal {@link ResultSet}.
     * </p>
     * * @param codiceFiscale La stringa che identifica univocamente l'utente.
     * @return L'oggetto {@link Cliente} popolato se trovato; {@code null} in caso di assenza o errore SQL.
     */
    @Override
    public Cliente getClienteByCF(String codiceFiscale) {
        String query = "SELECT * FROM Utente WHERE CodiceFiscale = ?";

        try {
            /* Acquisizione della connessione fornita dal gestore Singleton */
            Connection conn = DatabaseManager.getInstance().getConnection();
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

    /**
     * Esegue l'inserimento transazionale di un nuovo aggregate di dominio (Cliente e relativo Abbonamento).
     * <p>
     * Implementa la gestione manuale delle transazioni ({@code setAutoCommit(false)}) per garantire le 
     * <b>Proprietà ACID</b> (in particolare l'Atomicità). Poiché l'inserimento coinvolge tabelle 
     * relazionate (Utente, Cliente, Abbonamento, Pagamento), in caso di eccezione l'intera transazione 
     * subisce un {@code rollback()}, prevenendo la persistenza di stati parziali e inconsistenti.
     * </p>
     * * @param c L'entità {@link Cliente} da persistere.
     * @return {@code true} se la transazione completa il commit con successo; {@code false} in caso di rollback.
     */
    @Override
    public boolean inserisciCliente(Cliente c) {
        /* Dichiarazione esterna per garantire l'accessibilità nel blocco catch (Rollback) */
        Connection conn = null; 
        
        try {
            conn = DatabaseManager.getInstance().getConnection();
            
            /* Sospensione dell'autocommit per raggruppare le DML in un'unica Transazione Atomica */
            conn.setAutoCommit(false); 

            /* Fase 1: Inserimento nella super-tabella Utente con recupero della Primary Key generata */
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

            /* Fase 2: Inserimento nella sotto-tabella Cliente (Relazione 1:1) */
            String insertCliente = "INSERT INTO Cliente (ID_Cliente, ID_Sede) VALUES (?, ?)";
            PreparedStatement psCliente = conn.prepareStatement(insertCliente);
            psCliente.setInt(1, idGenerato); 
            psCliente.setInt(2, c.getSedePrincipale().getIdSede()); 
            psCliente.executeUpdate();
            psCliente.close();

            Abbonamento abb = c.getAbbonamentoAttivo();
            int idAbbonamentoGenerato = -1;
            
            /* Fase 3: Inserimento condizionale dell'Abbonamento associato */
            if (abb != null) {
                String insertAbbonamento = "INSERT INTO Abbonamento (Tipo, Livello, DataScadenza, RinnovoAutomatico, IBAN, ID_Cliente) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement psAbb = conn.prepareStatement(insertAbbonamento, Statement.RETURN_GENERATED_KEYS);
                
                psAbb.setString(1, abb.getTipo().name());
                psAbb.setString(2, abb.getLivello().name());
                psAbb.setDate(3, new java.sql.Date(abb.getDataScadenza().getTime()));
                
                /* Tracciamento diagnostico del flag booleano pre-persistenza */
                System.out.println("[DEBUG DAO] Stato rinnovo automatico: " + abb.isRinnovoAutomatico());
                
                psAbb.setBoolean(4, abb.isRinnovoAutomatico()); 
                psAbb.setString(5, abb.getIban());
                psAbb.setInt(6, idGenerato); 
                
                psAbb.executeUpdate();
                
                ResultSet rsAbbKeys = psAbb.getGeneratedKeys();
                if (rsAbbKeys.next()) {
                    idAbbonamentoGenerato = rsAbbKeys.getInt(1);
                }
                psAbb.close();

                /* Fase 4: Registrazione del primo Pagamento associato all'abbonamento */
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

            /* Consolidamento della transazione (Commit) */
            conn.commit();
            conn.close(); 
            return true;

        } catch (SQLException e) {
            System.err.println("Errore SQL, eseguo ROLLBACK. Dettagli: " + e.getMessage());
            /* Gestione del fallimento: ripristino dello stato del DB precedente all'inizio della transazione */
            if (conn != null) {
                try {
                    conn.rollback(); 
                    conn.close(); 
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } 
    }

    /**
     * Aggiorna lo stato di un'entità Cliente preesistente sul data store.
     * <p>
     * <i>Metodo attualmente stubbato, in attesa di implementazione concreta.</i>
     * </p>
     * * @param c L'entità Cliente contenente le modifiche da applicare.
     */
    @Override
    public void updateCliente(Cliente c) {
        // TODO: Implementare logica di aggiornamento (es. modifica dati anagrafici)
    }

    /**
     * Rimuove un'entità Cliente dal data store.
     * <p>
     * <i>Metodo attualmente stubbato, in attesa di implementazione concreta.</i>
     * </p>
     * * @param codiceFiscale La chiave naturale che identifica il record da eliminare.
     */
    @Override
    public void deleteCliente(String codiceFiscale) {
    }
}