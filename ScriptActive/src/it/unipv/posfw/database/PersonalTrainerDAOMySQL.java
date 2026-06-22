package it.unipv.posfw.database;

import it.unipv.posfw.dao.PersonalTrainerDAO;
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

/**
 * Implementazione MySQL del DAO dei Personal Trainer.
 *
 * Questa classe incapsula tutte le operazioni SQL relative ai Personal Trainer,
 * evitando che il controller GestorePersonale conosca direttamente la struttura
 * delle tabelle o le query del database.
 *
 * Gestisce salvataggio, ricerca, aggiornamento, soft delete
 * e caricamento dell'elenco dei Personal Trainer.
 */
public class PersonalTrainerDAOMySQL implements PersonalTrainerDAO {

    /**
     * Salva un Personal Trainer nel database.
     *
     * Il metodo inserisce o aggiorna prima il record nella tabella Utente e poi
     * salva i dati specifici nella tabella PersonalTrainer. Le due operazioni
     * vengono eseguite nella stessa transazione.
     *
     * @param pt Personal Trainer da salvare
     */
    @Override
    public void salva(PersonalTrainer pt) {
        /*
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

    /**
     * Cerca un Personal Trainer tramite identificativo.
     *
     * Il metodo esegue una join tra Utente e PersonalTrainer per ricostruire
     * l'oggetto di dominio completo.
     *
     * @param idPT identificativo del Personal Trainer da cercare
     * @return Personal Trainer trovato, oppure null se non esiste
     */
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
    
    /**
     * Cerca un Personal Trainer tramite email.
     *
     * Il metodo viene usato dal controller in fase di assunzione per evitare
     * che la stessa persona venga registrata più volte con lo stesso indirizzo email.
     *
     * @param email email del Personal Trainer da cercare
     * @return Personal Trainer trovato, oppure null se non esiste
     */
    @Override
    public PersonalTrainer trovaPerEmail(String email) {
        if (email == null || email.isBlank()) {
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
            WHERE LOWER(u.Email) = LOWER(?)
        """;

        try (
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creaPersonalTrainerDaResultSet(rs);
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la ricerca del Personal Trainer tramite email su MySQL.", e);
        }
    }

    /**
     * Aggiorna i dati di un Personal Trainer già presente nel database.
     *
     * Il metodo aggiorna sia i dati specifici del trainer sia i dati comuni
     * presenti nella tabella Utente, mantenendo coerente lo stato dell'utente
     * con lo stato contrattuale del Personal Trainer.
     *
     * @param pt Personal Trainer con i dati aggiornati
     */
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

    /**
     * Disattiva logicamente un Personal Trainer.
     *
     * Il metodo non elimina fisicamente il record dal database. Imposta il
     * trainer come LICENZIATO e aggiorna lo stato dell'utente associato a
     * Inattivo.
     *
     * @param idPT identificativo del Personal Trainer da disattivare
     */
    @Override
    public void elimina(String idPT) {
        /*
         * Soft delete:
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

    /**
     * Restituisce l'elenco completo dei Personal Trainer presenti nel database.
     *
     * Ogni riga letta dal database viene convertita in un oggetto
     * PersonalTrainer tramite un metodo di supporto dedicato.
     *
     * @return lista dei Personal Trainer disponibili
     */
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

    /**
     * Salva o aggiorna il record Utente associato al Personal Trainer.
     *
     * Poiché nello schema adottato l'identificativo del Personal Trainer coincide
     * con l'identificativo dell'utente, il metodo restituisce l'ID_Utente da usare
     * anche come ID_Trainer.
     *
     * @param conn connessione database attiva
     * @param pt Personal Trainer da associare a un utente
     * @return identificativo dell'utente associato al trainer
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private int salvaOAggiornaUtente(Connection conn, PersonalTrainer pt) throws Exception {
        /*
         * Siccome nello schema comune ID_Trainer coincide con ID_Utente,
         * prima salviamo l'utente e poi usiamo lo stesso ID per PersonalTrainer.
         */

    	Integer idEsistenteDaEmail = trovaIdUtenteDaEmail(conn, pt.getEmail());

    	if (idEsistenteDaEmail != null) {
    	    throw new IllegalStateException(
    	            "Esiste già un utente registrato con email: " + pt.getEmail());
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

    /**
     * Aggiorna i dati della tabella Utente collegati al Personal Trainer.
     *
     * @param conn connessione database attiva
     * @param idUtente identificativo dell'utente da aggiornare
     * @param pt Personal Trainer contenente i dati aggiornati
     * @throws Exception se si verifica un errore durante l'aggiornamento
     */
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

    /**
     * Cerca l'identificativo di un utente tramite email.
     *
     * @param conn connessione database attiva
     * @param email email dell'utente da cercare
     * @return identificativo dell'utente, oppure null se non esiste
     * @throws Exception se si verifica un errore durante la query
     */
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

    /**
     * Recupera un direttore esistente oppure crea un direttore tecnico predefinito.
     *
     * Il metodo serve a rispettare il vincolo dello schema comune, nel quale la
     * tabella PersonalTrainer richiede il riferimento a un direttore.
     *
     * @param conn connessione database attiva
     * @return identificativo del direttore da associare al Personal Trainer
     * @throws Exception se si verifica un errore durante il recupero o la creazione
     */
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

    /**
     * Costruisce un oggetto PersonalTrainer a partire da una riga del ResultSet.
     *
     * Il metodo ricostruisce anche la strategia di retribuzione corretta in base
     * al tipo di retribuzione salvato nel database.
     *
     * @param rs ResultSet posizionato sulla riga da convertire
     * @return oggetto PersonalTrainer ricostruito dai dati del database
     * @throws Exception se si verifica un errore nella lettura dei dati
     */
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

    /**
     * Converte la strategia di retribuzione del Personal Trainer nei campi
     * richiesti dallo schema relazionale.
     *
     * @param pt Personal Trainer da analizzare
     * @return dati retributivi da salvare nella tabella PersonalTrainer
     */
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

    /**
     * Normalizza lo stato contrattuale del Personal Trainer.
     *
     * @param statoContratto stato contrattuale ricevuto dall'oggetto di dominio
     * @return stato contrattuale valido da salvare nel database
     */
    private String normalizzaStatoContratto(String statoContratto) {
        if (statoContratto == null || statoContratto.isBlank()) {
            return "ATTIVO";
        }

        return statoContratto;
    }

    /**
     * Estrae la parte numerica da un identificativo testuale.
     *
     * @param id identificativo da convertire
     * @return valore numerico dell'identificativo, oppure null se non valido
     */
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

    /**
     * Genera un codice fiscale tecnico per i Personal Trainer creati dal sistema.
     *
     * Il metodo viene usato per rispettare il vincolo NOT NULL e UNIQUE del campo
     * CodiceFiscale nella tabella Utente.
     *
     * @param pt Personal Trainer per cui generare il codice tecnico
     * @return codice fiscale tecnico stabile
     */
    private String generaCodiceFiscaleTecnico(PersonalTrainer pt) {
        /*
         * Lo schema richiede CodiceFiscale NOT NULL e UNIQUE.
         * Quindi generiamo un codice tecnico stabile partendo dall'email.
         */
        String base = pt.getEmail();

        if (base == null || base.isBlank()) {
            base = pt.getNome() + "." + pt.getCognome() + "." + System.nanoTime();
        }

        long hash = Math.abs((long) base.hashCode());
        String numeri = String.format("%014d", hash % 100000000000000L);

        return "PT" + numeri;
    }

    /**
     * Oggetto di supporto interno usato per trasferire i dati retributivi
     * calcolati dalla strategia verso la query SQL di salvataggio.
     */
    private static class DatiRetribuzione {
        private final String tipoContratto;
        private final String tipoRetribuzione;
        private final double stipendioMensile;
        private final Double compensoPerLezione;

        /**
         * Costruisce un contenitore dei dati retributivi da salvare su database.
         *
         * @param tipoContratto tipo contrattuale descrittivo
         * @param tipoRetribuzione tipo di retribuzione usato nello schema MySQL
         * @param stipendioMensile importo mensile fisso
         * @param compensoPerLezione compenso previsto per ogni lezione
         */
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