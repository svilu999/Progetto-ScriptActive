package it.unipv.posfw.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import it.unipv.posfw.domain.Cliente;

public class DashboardClienteView extends JFrame {
    
    private Cliente utenteCorrente;

    // Componenti grafici
    private JLabel lblBenvenuto;
    private JLabel lblAbbonamento;
    private JPanel panelPrenotazioni;
    
    public JButton btnAreaPremium;
    public JButton btnPrenotaCorsi; 

    // Font per l'estetica
    private final Font fontTitolo = new Font("SansSerif", Font.BOLD, 22);
    private final Font fontSottotitolo = new Font("SansSerif", Font.PLAIN, 16);
    private final Font fontBottoni = new Font("SansSerif", Font.BOLD, 14);

    public DashboardClienteView() {
        setTitle("ScriptActive - Dashboard Cliente");
        setSize(600, 500); // Finestra più compatta e accogliente
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        
        // Bordo generale
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        inizializzaComponenti();
    }

    private void inizializzaComponenti() {
        // --- 1. SEZIONE IN ALTO: INTESTAZIONE ---
        JPanel panelTop = new JPanel(new GridLayout(2, 1, 5, 5));
        
        lblBenvenuto = new JLabel("Benvenuto!");
        lblBenvenuto.setFont(fontTitolo);
        lblBenvenuto.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblAbbonamento = new JLabel("Livello Abbonamento:");
        lblAbbonamento.setFont(fontSottotitolo);
        lblAbbonamento.setForeground(Color.DARK_GRAY);
        lblAbbonamento.setHorizontalAlignment(SwingConstants.CENTER);
        
        panelTop.add(lblBenvenuto);
        panelTop.add(lblAbbonamento);
        add(panelTop, BorderLayout.NORTH);

        // --- 2. SEZIONE CENTRALE: LE MIE PRENOTAZIONI ---
        panelPrenotazioni = new JPanel();
        panelPrenotazioni.setLayout(new BoxLayout(panelPrenotazioni, BoxLayout.Y_AXIS));
        panelPrenotazioni.setBackground(Color.WHITE);
        
        JScrollPane scrollPrenotazioni = new JScrollPane(panelPrenotazioni);
        scrollPrenotazioni.setBorder(BorderFactory.createTitledBorder(null, "I Tuoi Prossimi Corsi", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontBottoni));
        add(scrollPrenotazioni, BorderLayout.CENTER);

        // --- 3. SEZIONE IN BASSO: BOTTONI DI NAVIGAZIONE ---
        JPanel panelBottoni = new JPanel(new GridLayout(1, 2, 15, 0)); // 1 riga, 2 colonne, spazio in mezzo
        
        // Bottone per il collega (Prenotazione Corsi)
        btnPrenotaCorsi = new JButton("VAI AL PALINSESTO CORSI");
        btnPrenotaCorsi.setFont(fontBottoni);
        btnPrenotaCorsi.setBackground(new Color(70, 130, 180)); // Blu
        btnPrenotaCorsi.setForeground(Color.WHITE);
        btnPrenotaCorsi.setFocusPainted(false);
        btnPrenotaCorsi.setOpaque(true);
        btnPrenotaCorsi.setBorderPainted(false);
        btnPrenotaCorsi.setPreferredSize(new Dimension(200, 50));
        
        // Bottone per la tua area (Storico Allenamenti)
        btnAreaPremium = new JButton("LA MIA AREA PREMIUM");
        btnAreaPremium.setFont(fontBottoni);
        btnAreaPremium.setBackground(new Color(77, 43, 107)); 
        btnAreaPremium.setForeground(Color.WHITE);
        btnAreaPremium.setFocusPainted(false);
        btnAreaPremium.setOpaque(true);
        btnAreaPremium.setBorderPainted(false);
        btnAreaPremium.setPreferredSize(new Dimension(200, 50));

        panelBottoni.add(btnPrenotaCorsi);
        panelBottoni.add(btnAreaPremium);
        
        add(panelBottoni, BorderLayout.SOUTH);
    }

    // Metodo fondamentale: quando apriamo la finestra, le passiamo i dati del cliente!
    public void impostaDatiCliente(Cliente cliente) {
        this.utenteCorrente = cliente;
        
        // Personalizziamo l'intestazione
        lblBenvenuto.setText("Bentornato, " + cliente.getNome() + " " + cliente.getCognome() + "!");
        
        if (cliente.isPremium()) {
            lblAbbonamento.setText("Livello Abbonamento: PREMIUM ");
            lblAbbonamento.setForeground(new Color(77, 43, 107)); // Color oro
        } else {
            lblAbbonamento.setText("Livello Abbonamento: BASE");
        }

       
        mostraMessaggioPrenotazioni("Non hai ancora prenotato nessun corso per i prossimi giorni.");
    }
    
    public Cliente getUtenteCorrente() {
        return this.utenteCorrente;
    }
    
    private void mostraMessaggioPrenotazioni(String messaggio) {
        panelPrenotazioni.removeAll();
        JLabel lblMsg = new JLabel(messaggio);
        lblMsg.setFont(fontSottotitolo);
        lblMsg.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelPrenotazioni.add(lblMsg);
        panelPrenotazioni.revalidate();
        panelPrenotazioni.repaint();
    }
}
