package it.unipv.posfw.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso; 

public class DashboardClienteView extends JFrame {
    
    private Cliente utenteCorrente;

    private JLabel lblBenvenuto;
    private JLabel lblAbbonamento;
    private JPanel panelPrenotazioni;
    
    public JButton btnAreaPremium;
    public JButton btnPrenotaCorsi; 
    private final Font fontTitolo = new Font("SansSerif", Font.BOLD, 22);
    private final Font fontSottotitolo = new Font("SansSerif", Font.PLAIN, 16);
    private final Font fontBottoni = new Font("SansSerif", Font.BOLD, 14);

    public DashboardClienteView() {
        setTitle("ScriptActive - Dashboard Cliente");
        setSize(600, 500); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        inizializzaComponenti();
    }

    private void inizializzaComponenti() {
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

        panelPrenotazioni = new JPanel();
        panelPrenotazioni.setLayout(new BoxLayout(panelPrenotazioni, BoxLayout.Y_AXIS));
        panelPrenotazioni.setBackground(Color.WHITE);
        
        JScrollPane scrollPrenotazioni = new JScrollPane(panelPrenotazioni);
        scrollPrenotazioni.setBorder(BorderFactory.createTitledBorder(null, "I Tuoi Prossimi Corsi", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontBottoni));
        add(scrollPrenotazioni, BorderLayout.CENTER);
        
        JPanel panelBottoni = new JPanel(new GridLayout(1, 2, 15, 0)); 
        
        btnPrenotaCorsi = new JButton("VAI AL PALINSESTO CORSI");
        btnPrenotaCorsi.setFont(fontBottoni);
        btnPrenotaCorsi.setBackground(new Color(70, 130, 180)); // Blu
        btnPrenotaCorsi.setForeground(Color.WHITE);
        btnPrenotaCorsi.setFocusPainted(false);
        btnPrenotaCorsi.setOpaque(true);
        btnPrenotaCorsi.setBorderPainted(false);
        btnPrenotaCorsi.setPreferredSize(new Dimension(200, 50));
        
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

    public void impostaDatiCliente(Cliente cliente) {
        this.utenteCorrente = cliente;
        
        lblBenvenuto.setText("Bentornato, " + cliente.getNome() + " " + cliente.getCognome() + "!");
        
        // =====================================================================
        // ALGORITMO INGEGNERIZZATO: DATA-DRIVEN UI
        // Leggiamo i dati incrociando l'anagrafica Cliente e il suo Abbonamento
        // =====================================================================
        
        // 1. Estraiamo il tipo di abbonamento (Base o Premium)
        String tipoAbb = "BASE"; 
        if (cliente.getTipoAbbonamento() != null) {
            tipoAbb = cliente.getTipoAbbonamento().toString(); 
        }
        
        // 2. Estraiamo la durata passando dall'oggetto AbbonamentoAttivo!
        String durataAbb = "N/A";
        if (cliente.getAbbonamentoAttivo() != null && cliente.getAbbonamentoAttivo().getLivello() != null) {
            durataAbb = cliente.getAbbonamentoAttivo().getLivello().toString(); 
        }
        
        // 3. Stampiamo tutto in una singola riga pulita
        lblAbbonamento.setText("Abbonamento: " + tipoAbb.toUpperCase() + " | Durata: " + durataAbb);
        
        // 4. Manteniamo un tocco di colore dinamico
        if ("PREMIUM".equalsIgnoreCase(tipoAbb)) {
            lblAbbonamento.setForeground(new Color(77, 43, 107)); // Colore speciale per Premium
        } else {
            lblAbbonamento.setForeground(Color.DARK_GRAY); // Grigio per Base
        }
        // =====================================================================
    }
    
    public Cliente getUtenteCorrente() {
        return this.utenteCorrente;
    }
    
    public void mostraCorsiPrenotati(List<Corso> corsi) {
        panelPrenotazioni.removeAll();

        if (corsi == null || corsi.isEmpty()) {
            mostraMessaggioPrenotazioni("Non hai ancora prenotato nessun corso per i prossimi giorni.");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

            for (Corso corso : corsi) {
                JPanel panelScheda = new JPanel(new BorderLayout(10, 10));
                panelScheda.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 10, 5, 10),
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)
                ));
                panelScheda.setBackground(Color.WHITE);

                StringBuilder sb = new StringBuilder();
                sb.append("🏋️ ").append(corso.getNome().toUpperCase()).append("\n");
                sb.append("📅 ").append(corso.getDataOra().format(formatter)).append("\n");
                
                // =========================================================
                // INIZIO CEROTTO GRAFICO PER IL TRAINER
                // =========================================================
                if (corso.getTrainerAssegnato() != null) {
                    String nomePt = corso.getTrainerAssegnato().getNome();
                    String cognomePt = corso.getTrainerAssegnato().getCognome();
                    
                    // Se il DAO ci sta mandando l'oggetto "finto" con scritto solo "Trainer"
                    if ("Trainer".equalsIgnoreCase(nomePt) || nomePt == null || nomePt.trim().isEmpty()) {
                        sb.append("👤 Trainer: Assegnazione in corso...");
                    } else {
                        // Se invece è un nome vero, lo stampa normalmente!
                        sb.append("👤 Trainer: ").append(nomePt).append(" ").append(cognomePt != null ? cognomePt : "");
                    }
                }
           

                JTextArea txtDettagli = new JTextArea(sb.toString());
                txtDettagli.setFont(new Font("SansSerif", Font.PLAIN, 14));
                txtDettagli.setEditable(false);
                txtDettagli.setOpaque(false);

                panelScheda.add(txtDettagli, BorderLayout.CENTER);
                
                panelPrenotazioni.add(panelScheda);
                panelPrenotazioni.add(Box.createRigidArea(new Dimension(0, 5))); 
            }
        }

        panelPrenotazioni.revalidate();
        panelPrenotazioni.repaint();
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