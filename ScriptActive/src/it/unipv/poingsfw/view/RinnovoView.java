package it.unipv.poingsfw.view;

import java.awt.*;
import javax.swing.*;

import it.unipv.poingsfw.controller.LoginController;
import it.unipv.poingsfw.database.UtenteDAOMySQL;

public class RinnovoView extends JFrame {
    private JComboBox<String> comboPiani;
    private JButton btnPaga;
    private JLabel lblMessaggio;
    private String emailUtente;

    public RinnovoView(String emailUtente) {
        this.emailUtente = emailUtente;
        
        setTitle("Rinnovo Abbonamento");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la finestra
        setLayout(new BorderLayout(10, 10));

        // Pannello Superiore (Intestazione)
        JPanel pnlNord = new JPanel();
        lblMessaggio = new JLabel("Il tuo abbonamento è scaduto. Seleziona un piano per rinnovare:");
        lblMessaggio.setFont(new Font("Arial", Font.BOLD, 12));
        pnlNord.add(lblMessaggio);
        add(pnlNord, BorderLayout.NORTH);

        // Pannello Centrale (Form di selezione)
        JPanel pnlCentro = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        pnlCentro.add(new JLabel("Scegli la durata del nuovo abbonamento:"));
        
        String[] piani = { "Mensile - €40.00", "Semestrale - €200.00", "Annuale - €360.00" };
        comboPiani = new JComboBox<>(piani);
        pnlCentro.add(comboPiani);
        
        add(pnlCentro, BorderLayout.CENTER);

        // Pannello Inferiore (Pulsante di azione)
        JPanel pnlSud = new JPanel();
        btnPaga = new JButton("Paga e Attiva");
        btnPaga.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Gestione del Click sul pulsante Paga
        btnPaga.addActionListener(e -> {
            effettuaRinnovo();
        });
        
        pnlSud.add(btnPaga);
        add(pnlSud, BorderLayout.SOUTH);
    }

    private void effettuaRinnovo() {
        String pianoSelezionato = (String) comboPiani.getSelectedItem();
        int mesiDaAggiungere = 1;
        String tipoAbbonamento = "MENSILE";

        if (pianoSelezionato.contains("Semestrale")) {
            mesiDaAggiungere = 6;
            tipoAbbonamento = "SEMESTRALE";
        } else if (pianoSelezionato.contains("Annuale")) {
            mesiDaAggiungere = 12;
            tipoAbbonamento = "ANNUALE";
        }

        // =======================================================================
        // MAGIA IN AZIONE: Chiamiamo il database per aggiornare l'abbonamento!
        // =======================================================================
        UtenteDAOMySQL dao = new UtenteDAOMySQL();
        dao.eseguiRinnovo(emailUtente, mesiDaAggiungere, tipoAbbonamento);
        
        JOptionPane.showMessageDialog(this, 
            "Pagamento simulato con successo!\nIl tuo abbonamento è stato aggiornato con il piano: " + tipoAbbonamento, 
            "Rinnovo Completato", 
            JOptionPane.INFORMATION_MESSAGE);
            
        this.dispose(); // Chiude questa schermata
        
        // Riapre il login in modo pulito collegando il controller architetturale
        LoginView login = new LoginView();
        LoginController controller = new LoginController(login, dao);
        login.setController(controller);
        login.setVisible(true);
    }
}