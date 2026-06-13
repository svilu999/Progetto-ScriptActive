package it.unipv.posfw.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import it.unipv.posfw.controller.GestoreCorsi;

public class DashboardDirettoreView extends JFrame {

    // Componenti della finestra dichiarati a livello globale
    private JTextField txtNomeCorso, txtDataOra, txtCapienza, txtIdPT, txtIdAnnulla;
    private JButton btnOrganizza, btnAnnulla, btnVediPalinsesto, btnGestionePersonale;
    private JTextArea areaLog;

    public DashboardDirettoreView() {
        // 1. Impostazioni base della finestra
        setTitle("ScriptActive - Dashboard Direttore");
        setSize(550, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));

        // --- ZONA NORD: Creazione Corso ---
        JPanel pnlInput = new JPanel(new GridLayout(5, 2, 5, 10)); 
        pnlInput.setBorder(BorderFactory.createTitledBorder("1. Organizza Nuovo Corso"));

        pnlInput.add(new JLabel(" Nome Corso:"));
        txtNomeCorso = new JTextField();
        pnlInput.add(txtNomeCorso);

        pnlInput.add(new JLabel(" Data/Ora (dd/MM/yyyy HH:mm):"));
        txtDataOra = new JTextField("01/07/2026 18:00");
        pnlInput.add(txtDataOra);

        pnlInput.add(new JLabel(" Capienza Massima:"));
        txtCapienza = new JTextField();
        pnlInput.add(txtCapienza);

        pnlInput.add(new JLabel(" ID Personal Trainer:"));
        txtIdPT = new JTextField();
        pnlInput.add(txtIdPT);

        btnOrganizza = new JButton("Pubblica Corso");
        btnVediPalinsesto = new JButton("Visualizza Palinsesto"); 
        pnlInput.add(btnOrganizza);
        pnlInput.add(btnVediPalinsesto);

        // --- ZONA CENTRO: Contenitore per Annullamento e Navigazione ---
        JPanel pnlCentro = new JPanel();
        pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Sotto-pannello 2.1: Annullamento
        JPanel pnlAnnulla = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pnlAnnulla.setBorder(BorderFactory.createTitledBorder("2. Gestione Corsi Esistenti"));
        pnlAnnulla.add(new JLabel("ID Corso da annullare:"));
        txtIdAnnulla = new JTextField(12);
        btnAnnulla = new JButton("Annulla Corso");
        btnAnnulla.setForeground(Color.RED); 
        
        pnlAnnulla.add(txtIdAnnulla);
        pnlAnnulla.add(btnAnnulla);

        // Sotto-pannello 2.2: Navigazione Esterna
        JPanel pnlNavigazione = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlNavigazione.setBorder(BorderFactory.createTitledBorder("3. Integrazioni Sistema"));
        
        // CORREZIONE APPLICATA QUI: Rimosso "JButton" all'inizio per evitare lo shadowing
        btnGestionePersonale = new JButton("Apri Modulo Gestione Personale (UC5)");
        pnlNavigazione.add(btnGestionePersonale);

        // Aggiungiamo i due sotto-pannelli al contenitore centrale
        pnlCentro.add(pnlAnnulla);
        pnlCentro.add(Box.createRigidArea(new Dimension(0, 10))); 
        pnlCentro.add(pnlNavigazione);

        // --- ZONA SUD: Area Log Matrix ---
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(Color.BLACK);
        areaLog.setForeground(Color.GREEN);
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setPreferredSize(new Dimension(550, 180));

        // Aggiunta pannelli principali al frame
        add(pnlInput, BorderLayout.NORTH);
        add(pnlCentro, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // Configurazione dei Listener per i click
        configuraEventi();
    }

    private void configuraEventi() {
        // Click sul bottone ORGANIZZA
        btnOrganizza.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String nome = txtNomeCorso.getText();
                    int capienza = Integer.parseInt(txtCapienza.getText());
                    String idPT = txtIdPT.getText();
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    LocalDateTime dataOra = LocalDateTime.parse(txtDataOra.getText(), formatter);

                    // CHIAMATA AL CONTROLLER (UC3)
                    GestoreCorsi.getInstance().organizzaNuovoCorso(nome, dataOra, capienza, idPT);
                    
                    areaLog.append("[SUCCESS] Corso '" + nome + "' creato correttamente.\n");
                    JOptionPane.showMessageDialog(null, "Corso creato con successo!");

                } catch (Exception ex) {
                    areaLog.append("[ERRORE] " + ex.getMessage() + "\n");
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Click sul bottone ANNULLA
        btnAnnulla.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String id = txtIdAnnulla.getText();
                    GestoreCorsi.getInstance().annullaCorso(id);
                    areaLog.append("[SUCCESS] Corso " + id + " annullato. Notifiche inviate agli iscritti.\n");
                    JOptionPane.showMessageDialog(null, "Corso annullato con successo!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Click sul bottone VISUALIZZA PALINSESTO
        btnVediPalinsesto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PalinsestoCorsiView finestraPalinsesto = new PalinsestoCorsiView();
                finestraPalinsesto.setVisible(true);
                areaLog.append("[VIEW] Apertura finestra Palinsesto Corsi.\n");
            }
        });

        // Click sul bottone GESTIONE PERSONALE
        btnGestionePersonale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GestionePersonaleView finestraPersonale = new GestionePersonaleView();
                finestraPersonale.setVisible(true);
                areaLog.append("[VIEW] Apertura finestra Gestione Personale - UC5.\n");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DashboardDirettoreView().setVisible(true);
        });
    }
}