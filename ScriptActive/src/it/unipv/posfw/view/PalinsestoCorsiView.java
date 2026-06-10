package it.unipv.posfw.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.dao.CorsoDAOMySQL;
import it.unipv.posfw.domain.Corso;

public class PalinsestoCorsiView extends JFrame {

    private JTable tabellaCorsi;
    private DefaultTableModel modelloTabella;
    private JButton btnAggiorna, btnIndietro;

    public PalinsestoCorsiView() {
        setTitle("ScriptActive - Palinsesto Completo (UC3)");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Creazione della Tabella (Ho aggiunto la colonna "Posti")
        String[] colonne = {"ID Corso", "Nome", "Data e Ora", "Trainer", "Posti", "Stato"};
        modelloTabella = new DefaultTableModel(colonne, 0);
        tabellaCorsi = new JTable(modelloTabella);
        tabellaCorsi.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(tabellaCorsi);
        add(scrollPane, BorderLayout.CENTER);

        // 2. Pannello Pulsanti
        JPanel pnlBottoni = new JPanel();
        btnAggiorna = new JButton("Aggiorna Palinsesto");
        btnIndietro = new JButton("Torna alla Dashboard");
        
        pnlBottoni.add(btnAggiorna);
        pnlBottoni.add(btnIndietro);
        add(pnlBottoni, BorderLayout.SOUTH);

        // 3. Azioni
        btnAggiorna.addActionListener(e -> caricaDati());
        btnIndietro.addActionListener(e -> this.dispose());

        // Caricamento iniziale
        caricaDati();
    }

    public void caricaDati() {
        // Puliamo la tabella prima di ricaricare
        modelloTabella.setRowCount(0);

        // Bypassiamo il GestoreCorsi per non rompere il codice dei compagni
        // e usiamo direttamente il nostro VERO motore MySQL!
        CorsoDAO databaseCorsi = new CorsoDAOMySQL();
        List<Corso> lista = databaseCorsi.getPalinsesto();

        for (Corso c : lista) {
            // Uniamo nome e cognome ereditati dalla classe Utente
            String nomeCompletoTrainer = c.getTrainerAssegnato().getNome() + " " + c.getTrainerAssegnato().getCognome();

            Object[] riga = {
                c.getIdCorso(),
                c.getNome(),
                c.getDataOra().toString().replace("T", " "), 
                nomeCompletoTrainer, // <-- Ora stamperà "Luigi Bianchi" !
                "N/D", // <-- Sostituisci con c.getPostiDisponibili() appena il collega aggiunge il getter
                c.getStato()
            };
            modelloTabella.addRow(riga);
        }
    }
}