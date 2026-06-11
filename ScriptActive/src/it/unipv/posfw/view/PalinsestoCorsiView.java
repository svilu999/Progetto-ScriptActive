package it.unipv.posfw.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.database.CorsoDAOMySQL;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.controller.GestorePrenotazioni;
import it.unipv.posfw.exceptions.CorsoAlCompletoException;
import it.unipv.posfw.exceptions.PrenotazioneGiaEffettuataException;

public class PalinsestoCorsiView extends JFrame {

    private JTable tabellaCorsi;
    private DefaultTableModel modelloTabella;
    // 1. Aggiunto il bottone Prenota
    private JButton btnAggiorna, btnIndietro, btnPrenota; 
    
    // 2. Salviamo la lista dei corsi per poter recuperare facilmente l'oggetto cliccato
    private List<Corso> corsiAttuali; 

    public PalinsestoCorsiView() {
        setTitle("ScriptActive - Palinsesto Completo (UC3 & UC2)");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Creazione della Tabella
        String[] colonne = {"ID Corso", "Nome", "Data e Ora", "Trainer", "Posti", "Stato"};
        modelloTabella = new DefaultTableModel(colonne, 0);
        tabellaCorsi = new JTable(modelloTabella);
        tabellaCorsi.setFillsViewportHeight(true);
        
        // Imposta selezione singola (non si possono prenotare due corsi insieme con un click)
        tabellaCorsi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tabellaCorsi);
        add(scrollPane, BorderLayout.CENTER);

        // Pannello Pulsanti
        JPanel pnlBottoni = new JPanel();
        btnAggiorna = new JButton("Aggiorna Palinsesto");
        btnIndietro = new JButton("Torna alla Dashboard");
        btnPrenota = new JButton("Prenota Corso Selezionato"); // Inizializzato
        
        pnlBottoni.add(btnAggiorna);
        pnlBottoni.add(btnPrenota); // Aggiunto al pannello centrale
        pnlBottoni.add(btnIndietro);
        add(pnlBottoni, BorderLayout.SOUTH);

        // Azioni
        btnAggiorna.addActionListener(e -> caricaDati());
        btnIndietro.addActionListener(e -> this.dispose());
        
        // 3. Azione del NOSTRO bottone
        btnPrenota.addActionListener(e -> prenotaCorsoCliccato());

        // Caricamento iniziale
        caricaDati();
    }

    public void caricaDati() {
        modelloTabella.setRowCount(0);

        CorsoDAO databaseCorsi = new CorsoDAOMySQL();
        corsiAttuali = databaseCorsi.getPalinsesto(); // Salviamo la lista in memoria

        for (Corso c : corsiAttuali) {
            String nomeCompletoTrainer = c.getTrainerAssegnato().getNome() + " " + c.getTrainerAssegnato().getCognome();

            Object[] riga = {
                c.getIdCorso(),
                c.getNome(),
                c.getDataOra().toString().replace("T", " "), 
                nomeCompletoTrainer, 
                c.getPostiDisponibili(), // <-- RISOLTO! Ora stampa il numero vero
                c.getStato()
            };
            modelloTabella.addRow(riga);
        }
    }

    // --- LA NOSTRA LOGICA GRAFICA (UC2) ---
    private void prenotaCorsoCliccato() {
        int rigaSelezionata = tabellaCorsi.getSelectedRow();
        
        // Controllo: l'utente ha cliccato su una riga?
        if (rigaSelezionata == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona un corso dalla tabella prima di cliccare su Prenota!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Recuperiamo il corso corrispondente alla riga cliccata
        Corso corsoDaPrenotare = corsiAttuali.get(rigaSelezionata);

        // SIMULIAMO il cliente loggato (in una versione finale lo prendereste dalla sessione)
        Cliente clienteLoggato = new Cliente("Giulia", "Cliente", "cli@test.com", "3", TipoAbbonamento.BASE);

        // Invio al Controller
        GestorePrenotazioni gestore = new GestorePrenotazioni();
        
        try {
            gestore.prenotaCorso(clienteLoggato, corsoDaPrenotare);
            
            // Se arriviamo qui, niente eccezioni! Mostriamo il popup verde.
            JOptionPane.showMessageDialog(this, 
                "Prenotazione al corso di " + corsoDaPrenotare.getNome() + " effettuata con successo!", 
                "Successo", 
                JOptionPane.INFORMATION_MESSAGE);
                
            caricaDati(); // Ricarica la tabella per mostrare il numero di posti scalato in tempo reale
            
        } catch (CorsoAlCompletoException | PrenotazioneGiaEffettuataException ex) {
            // Intercettiamo le TUE eccezioni e mostriamo il messaggio in un popup rosso
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Impossibile Prenotare", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore di sistema: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
    
 // --- METODO MAIN TEMPORANEO PER TESTARE LA GRAFICA ---
    public static void main(String[] args) {
        // SwingUtilities assicura che la grafica si carichi nel modo corretto
        SwingUtilities.invokeLater(() -> {
            PalinsestoCorsiView view = new PalinsestoCorsiView();
            view.setVisible(true); // Rende visibile la finestra
        });
    }
}