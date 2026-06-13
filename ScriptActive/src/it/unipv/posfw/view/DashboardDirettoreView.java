package it.unipv.posfw.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import it.unipv.posfw.controller.GestoreCorsi;

public class DashboardDirettoreView extends JFrame {

    // Componenti della finestra
    private JTextField txtNomeCorso, txtDataOra, txtCapienza, txtIdPT, txtIdAnnulla;
    private JButton btnOrganizza, btnAnnulla, btnVediPalinsesto; // Aggiunto btnVediPalinsesto
    private JTextArea areaLog;

    public DashboardDirettoreView() {
        // 1. Impostazioni base della finestra
        setTitle("ScriptActive - Dashboard Direttore");
        setSize(520, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la finestra lo schermo
        setLayout(new BorderLayout(10, 10));

        // 2. Pannello Input (Griglia ottimizzata 5x2 per ospitare i due bottoni principali)
        JPanel pnlInput = new JPanel(new GridLayout(5, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Organizza Nuovo Corso"));

        pnlInput.add(new JLabel(" Nome Corso:"));
        txtNomeCorso = new JTextField();
        pnlInput.add(txtNomeCorso);

        pnlInput.add(new JLabel(" Data/Ora (dd/MM/yyyy HH:mm):"));
        txtDataOra = new JTextField("01/06/2026 18:00");
        pnlInput.add(txtDataOra);

        pnlInput.add(new JLabel(" Capienza Massima:"));
        txtCapienza = new JTextField();
        pnlInput.add(txtCapienza);

        pnlInput.add(new JLabel(" ID Personal Trainer:"));
        txtIdPT = new JTextField();
        pnlInput.add(txtIdPT);

        // Riga dei bottoni affiancati
        btnOrganizza = new JButton("Pubblica Corso");
        btnVediPalinsesto = new JButton("Visualizza Palinsesto"); // Il nostro tocco di classe
        pnlInput.add(btnOrganizza);
        pnlInput.add(btnVediPalinsesto);

        // 3. Pannello Annullamento
        JPanel pnlAnnulla = new JPanel(new FlowLayout());
        pnlAnnulla.setBorder(BorderFactory.createTitledBorder("Annulla Corso"));
        pnlAnnulla.add(new JLabel("ID Corso:"));
        txtIdAnnulla = new JTextField(10);
        btnAnnulla = new JButton("Annulla");
        pnlAnnulla.add(txtIdAnnulla);
        pnlAnnulla.add(btnAnnulla);

        // 4. Area Log/Console verde stile Matrix
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(Color.BLACK);
        areaLog.setForeground(Color.GREEN);
        JScrollPane scroll = new JScrollPane(areaLog);

        // Aggiunta pannelli al frame principale
        add(pnlInput, BorderLayout.NORTH);
        add(pnlAnnulla, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);
        scroll.setPreferredSize(new Dimension(520, 200));

        // 5. Configurazione dei Listener per i click
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

        // Click sul bottone VISUALIZZA PALINSESTO (Il collegamento tra le due viste)
        btnVediPalinsesto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Istanziamo la seconda finestra e la rendiamo visibile
                PalinsestoCorsiView finestraPalinsesto = new PalinsestoCorsiView();
                finestraPalinsesto.setVisible(true);
                areaLog.append("[VIEW] Apertura finestra Palinsesto Corsi.\n");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DashboardDirettoreView().setVisible(true);
        });
    }
}