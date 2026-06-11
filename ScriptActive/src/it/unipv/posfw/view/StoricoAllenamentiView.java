package it.unipv.posfw.view;

package provaview;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoricoAllenamentiForm extends JFrame {
    private StoricoAllenamentiController controller;
    
    // Componenti UI Swing
    private JPanel panelForm;
    private JTextField txtData, txtNomeEsercizio, txtCarichi, txtRipetizioni;
    private JButton btnAggiungiEsercizio; // NUOVO
    private JButton btnSalvaSessione;     // NUOVO
    private JTextArea txtAnteprimaBozza;  // NUOVO: Mostra gli esercizi prima di salvare
    
    private JPanel panelStoricoContainer; 
    private JButton btnSimulaAccesso; 
    
    private Cliente utenteCorrente; 
    
    // LISTA TEMPORANEA: Mantiene gli esercizi della sessione corrente prima del salvataggio finale
    private List<DatiForm> eserciziInBozza;

    public StoricoAllenamentiForm() {
        eserciziInBozza = new ArrayList<>(); // Inizializza la lista
        
        setTitle("I Miei Allenamenti");
        setSize(550, 750); // Un po' più alta per contenere l'anteprima
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        inizializzaComponenti();
    }

    public void setController(StoricoAllenamentiController controller) {
        this.controller = controller;
    }

    private void inizializzaComponenti() {
        // --- SEZIONE SUPERIORE ---
        JPanel panelTop = new JPanel();
        btnSimulaAccesso = new JButton("Accedi a 'I miei allenamenti'");
        btnSimulaAccesso.addActionListener(e -> clickAccediStorico(utenteCorrente));
        panelTop.add(btnSimulaAccesso);
        add(panelTop, BorderLayout.NORTH);

        // --- SEZIONE CENTRALE: Form di Creazione Scheda ---
        panelForm = new JPanel(new BorderLayout(5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Crea Nuova Sessione di Allenamento"));

        // Pannello input dati (Griglia)
        JPanel panelInput = new JPanel(new GridLayout(4, 2, 5, 5));
        panelInput.add(new JLabel("Data (dd/MM/yyyy):"));
        txtData = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        panelInput.add(txtData);

        panelInput.add(new JLabel("Nome Esercizio:"));
        txtNomeEsercizio = new JTextField();
        panelInput.add(txtNomeEsercizio);

        panelInput.add(new JLabel("Carico (kg):"));
        txtCarichi = new JTextField();
        panelInput.add(txtCarichi);

        panelInput.add(new JLabel("Ripetizioni:"));
        txtRipetizioni = new JTextField();
        panelInput.add(txtRipetizioni);

        // Bottone per aggiungere un singolo esercizio alla bozza
        btnAggiungiEsercizio = new JButton("Aggiungi Esercizio alla Scheda");
        btnAggiungiEsercizio.addActionListener(e -> clickAggiungiEsercizio());

        // Area di testo per l'anteprima della scheda corrente
        txtAnteprimaBozza = new JTextArea(5, 20);
        txtAnteprimaBozza.setEditable(false);
        txtAnteprimaBozza.setBorder(BorderFactory.createTitledBorder("Esercizi in questa sessione"));
        JScrollPane scrollBozza = new JScrollPane(txtAnteprimaBozza);

        // Bottone per salvare l'intera sessione
     // Bottone per salvare l'intera sessione
     // Bottone per salvare l'intera sessione
        btnSalvaSessione = new JButton("Salva Sessione Completa");
        btnSalvaSessione.setBackground(new Color(60, 179, 113)); // Verde per risaltare
        btnSalvaSessione.setForeground(Color.WHITE);
        btnSalvaSessione.setFocusPainted(false);
        btnSalvaSessione.setOpaque(true);
        btnSalvaSessione.setBorderPainted(false); 
        // ---------------------------
        
        btnSalvaSessione.addActionListener(e -> clickSalvaSessioneCompleta());

        // Assemblaggio della parte centrale
        JPanel panelMiddleContainer = new JPanel(new BorderLayout(5, 5));
        panelMiddleContainer.add(panelInput, BorderLayout.NORTH);
        panelMiddleContainer.add(btnAggiungiEsercizio, BorderLayout.CENTER);
        panelMiddleContainer.add(scrollBozza, BorderLayout.SOUTH);

        panelForm.add(panelMiddleContainer, BorderLayout.CENTER);
        panelForm.add(btnSalvaSessione, BorderLayout.SOUTH);

        add(panelForm, BorderLayout.CENTER);

        // --- SEZIONE INFERIORE: Storico Allenamenti ---
        panelStoricoContainer = new JPanel();
        panelStoricoContainer.setLayout(new BoxLayout(panelStoricoContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panelStoricoContainer);
        scrollPane.setBorder(BorderFactory.createTitledBorder("I Miei Allenamenti"));
        scrollPane.setPreferredSize(new Dimension(500, 300));
        add(scrollPane, BorderLayout.SOUTH);

        impostaStatoForm(false);
    }

    public void setUtenteCorrente(Cliente cliente) {
        this.utenteCorrente = cliente;
    }

    public void clickAccediStorico(Cliente cliente) {
        if (controller != null && cliente != null) {
            controller.gestisciAccessoSezione(cliente);
        }
    }

    // 1. Metodo per mettere in bozza un esercizio
    private void clickAggiungiEsercizio() {
        try {
            String nomeEsercizio = txtNomeEsercizio.getText();
            if (nomeEsercizio == null || nomeEsercizio.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci il nome dell'esercizio.", "Attenzione", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double carichi = Double.parseDouble(txtCarichi.getText());
            int ripetizioni = Integer.parseInt(txtRipetizioni.getText());

            DatiForm nuovoEsercizio = new DatiForm(nomeEsercizio, carichi, ripetizioni);
            eserciziInBozza.add(nuovoEsercizio);
            
            // Aggiorna l'anteprima
            aggiornaAnteprimaBozza();
            
            // Pulisci i campi per il prossimo esercizio (lascio la data intatta)
            txtNomeEsercizio.setText("");
            txtCarichi.setText("");
            txtRipetizioni.setText("");
            txtNomeEsercizio.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Carico e Ripetizioni devono essere numeri validi.", "Errore Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 2. Metodo per inviare tutta la lista al DB
    private void clickSalvaSessioneCompleta() {
        if (eserciziInBozza.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aggiungi almeno un esercizio prima di salvare la sessione!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Date data = new SimpleDateFormat("dd/MM/yyyy").parse(txtData.getText());
            
            // Invia al controller la data e tutta la lista di esercizi
            controller.salvaSessioneCompleta(data, eserciziInBozza, utenteCorrente);
            
            // Se va a buon fine, svuota la bozza
            eserciziInBozza.clear();
            aggiornaAnteprimaBozza();
            JOptionPane.showMessageDialog(this, "Sessione di allenamento salvata con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Errore nel formato della data (usa dd/MM/yyyy).", "Errore Data", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aggiornaAnteprimaBozza() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (DatiForm ex : eserciziInBozza) {
            sb.append(i++).append(". ").append(ex.getNomeEsercizio().toUpperCase())
              .append(" - ").append(ex.getCarichi()).append("kg x ").append(ex.getRipetizioni()).append(" ripetizioni \n");
        }
        txtAnteprimaBozza.setText(sb.toString());
    }

    // ==========================================
    // METODI RICHIAMATI DAL CONTROLLER
    // ==========================================

    public void mostraBloccoUpgradePremium() {
        impostaStatoForm(false);
        mostraMessaggioNelContainer("Devi essere un utente Premium per vedere il tuo storico.");
    }

    public void mostraModuloInserimento() {
        impostaStatoForm(true);
        mostraMessaggioNelContainer("Modulo sbloccato. Inserisci la tua prima sessione.");
    }

    public void mostraStorico(List<SessioneAllenamento> storico) {
        panelStoricoContainer.removeAll(); 

        if (storico.isEmpty()) {
            mostraMessaggioNelContainer("Nessun allenamento registrato.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            for (SessioneAllenamento sessione : storico) {
                JPanel panelScheda = new JPanel(new BorderLayout(10, 10));
                panelScheda.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5), 
                    BorderFactory.createLineBorder(Color.GRAY, 1, true) 
                ));

                StringBuilder sb = new StringBuilder();
                sb.append("Sessione avvenuta in data : ").append(sdf.format(sessione.getData())).append("\n");
                for (DatiForm esercizio : sessione.getEsercizi()) {
                    sb.append(String.format("   - %s | %.1f kg | %d ripetizioni\n", 
                              esercizio.getNomeEsercizio().toUpperCase(), esercizio.getCarichi(), esercizio.getRipetizioni()));
                }

                JTextArea txtDettagli = new JTextArea(sb.toString());
                txtDettagli.setEditable(false);
                txtDettagli.setOpaque(false); 

                JButton btnElimina = new JButton("Elimina");
                btnElimina.setForeground(Color.RED);
                btnElimina.setFocusPainted(false);
                btnElimina.addActionListener(e -> {
                    int conferma = JOptionPane.showConfirmDialog(this, "Vuoi eliminare questa sessione?", "Conferma", JOptionPane.YES_NO_OPTION);
                    if (conferma == JOptionPane.YES_OPTION) {
                        controller.eliminaSessioneSelezionata(sessione, utenteCorrente);
                    }
                });

                panelScheda.add(txtDettagli, BorderLayout.CENTER);
                panelScheda.add(btnElimina, BorderLayout.EAST);
                panelStoricoContainer.add(panelScheda);
            }
        }

        panelStoricoContainer.revalidate();
        panelStoricoContainer.repaint();
    }

    public void mostraMessaggio(String messaggio, String titolo, int tipoMessaggio) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, tipoMessaggio);
    }

    private void impostaStatoForm(boolean abilitato) {
        txtData.setEnabled(abilitato);
        txtNomeEsercizio.setEnabled(abilitato);
        txtCarichi.setEnabled(abilitato);
        txtRipetizioni.setEnabled(abilitato);
        btnAggiungiEsercizio.setEnabled(abilitato);
        btnSalvaSessione.setEnabled(abilitato);
    }
    
    private void mostraMessaggioNelContainer(String messaggio) {
        panelStoricoContainer.removeAll();
        JLabel lblMessaggio = new JLabel(messaggio);
        lblMessaggio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelStoricoContainer.add(lblMessaggio);
        panelStoricoContainer.revalidate();
        panelStoricoContainer.repaint();
    }
}
