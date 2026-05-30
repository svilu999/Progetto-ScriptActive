package it.unipv.posfw.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import it.unipv.posfw.controller.GestoreCorsi;
import it.unipv.posfw.dao.CorsoDAOImpl; // Nota: Qui usiamo il DAO per leggere i dati
import it.unipv.posfw.domain.Corso;

public class PalinsestoCorsiView extends JFrame {

    private JTable tabellaCorsi;
    private DefaultTableModel modelloTabella;
    private JButton btnAggiorna, btnIndietro;

    public PalinsestoCorsiView() {
        setTitle("ScriptActive - Palinsesto Completo (UC3)");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chiude solo questa finestra
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Creazione della Tabella
        String[] colonne = {"ID Corso", "Nome", "Data e Ora", "Trainer", "Stato"};
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
        // Puliamo la tabella
        modelloTabella.setRowCount(0);

        // CHIAMATA MVC CORRETTA: Chiediamo i dati al Controller (Singleton)
        List<Corso> lista = GestoreCorsi.getInstance().getElencoCorsi();

        for (Corso c : lista) {
            Object[] riga = {
                c.getIdCorso(),
                c.getNome(),
                c.getDataOra().toString().replace("T", " "), 
                c.getTrainerAssegnato().getIdTrainer(),
                c.getStato()
            };
            modelloTabella.addRow(riga);
        }
    }
     
}