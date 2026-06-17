package it.unipv.posfw.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import it.unipv.posfw.controller.StoricoAllenamentiController;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.DatiFormPojo;
import it.unipv.posfw.domain.SessioneAllenamento;
import it.unipv.posfw.exceptions.*;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoricoAllenamentiView extends JFrame {
    private StoricoAllenamentiController controller;
    
    private JPanel panelForm;
    private JTextField txtData, txtNomeEsercizio, txtCarichi, txtRipetizioni;
    private JComboBox<String> comboCategoria; 
    private JButton btnAggiungiEsercizio; 
    private JButton btnSalvaSessione;     
    private JTextArea txtAnteprimaBozza;  
    
    private JPanel panelStoricoContainer; 
    
    public JButton btnSimulaAccesso; 
    public JButton btnIndietro; 
    
    private Cliente utenteCorrente; 
    
    private List<DatiFormPojo> eserciziInBozza;

    private final Font fontTitoli = new Font("SansSerif", Font.BOLD, 14);
    private final Font fontTesto = new Font("SansSerif", Font.PLAIN, 14);

    public StoricoAllenamentiView() {
        eserciziInBozza = new ArrayList<>(); 
        
        setTitle("ScriptActive - I Miei Allenamenti");
        setSize(600, 800); 
        setLocationRelativeTo(null); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15)); 
        
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inizializzaComponenti();
    }

    public void setController(StoricoAllenamentiController controller) {
        this.controller = controller;
    }

    private void inizializzaComponenti() {
     
        JPanel panelTop = new JPanel(new BorderLayout()); 
        
        // 1. Bottone Indietro (A Sinistra)
        btnIndietro = new JButton("⬅ Indietro");
        btnIndietro.setFont(fontTesto);
        btnIndietro.setFocusPainted(false);
        btnIndietro.setBackground(new Color(230, 230, 230));
        panelTop.add(btnIndietro, BorderLayout.WEST);

        // =====================================================================
        // STILE DEL BOTTONE PREMIUM (Come il bottone "Accedi" Viola)
        // =====================================================================
        btnSimulaAccesso = new JButton("Sblocca la tua Area Premium");
        btnSimulaAccesso.setFont(fontTitoli);
        btnSimulaAccesso.setBackground(new Color(77, 43, 107)); // Il viola di ScriptActive
        btnSimulaAccesso.setForeground(Color.WHITE); // Testo bianco
        btnSimulaAccesso.setFocusPainted(false);
        btnSimulaAccesso.setOpaque(true); // Fondamentale per far vedere il colore su Mac/Windows
        btnSimulaAccesso.setBorderPainted(false);
        btnSimulaAccesso.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursore a manina
        // =====================================================================
        
        add(panelTop, BorderLayout.NORTH);

        //  Form di Creazione Scheda 
        panelForm = new JPanel(new BorderLayout(10, 10));
        panelForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(null, "Crea Nuova Sessione di Allenamento", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontTitoli),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel panelInput = new JPanel(new GridLayout(5, 2, 10, 10));
        
        JLabel lblData = new JLabel("Data (dd/MM/yyyy):");
        lblData.setFont(fontTesto);
        panelInput.add(lblData);
        txtData = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        txtData.setFont(fontTesto);
        panelInput.add(txtData);

        // IL MENU A TENDINA
        JLabel lblCategoria = new JLabel("Gruppo Muscolare:");
        lblCategoria.setFont(fontTesto);
        panelInput.add(lblCategoria);
        String[] categorie = {"Seleziona...", "PETTO", "DORSO", "GAMBE", "BRACCIA", "SPALLE", "ADDOME", "FULL BODY"};
        comboCategoria = new JComboBox<>(categorie);
        comboCategoria.setFont(fontTesto);
        panelInput.add(comboCategoria);

        JLabel lblNome = new JLabel("Nome Esercizio:");
        lblNome.setFont(fontTesto);
        panelInput.add(lblNome);
        txtNomeEsercizio = new JTextField();
        txtNomeEsercizio.setFont(fontTesto);
        panelInput.add(txtNomeEsercizio);

        JLabel lblCarico = new JLabel("Carico (kg):");
        lblCarico.setFont(fontTesto);
        panelInput.add(lblCarico);
        txtCarichi = new JTextField();
        txtCarichi.setFont(fontTesto);
        panelInput.add(txtCarichi);

        JLabel lblRipetizioni = new JLabel("Ripetizioni:");
        lblRipetizioni.setFont(fontTesto);
        panelInput.add(lblRipetizioni);
        txtRipetizioni = new JTextField();
        txtRipetizioni.setFont(fontTesto);
        panelInput.add(txtRipetizioni);

        btnAggiungiEsercizio = new JButton("Aggiungi Esercizio alla Scheda");
        btnAggiungiEsercizio.setFont(fontTitoli);
        btnAggiungiEsercizio.setBackground(new Color(77, 43, 107));
        btnAggiungiEsercizio.setForeground(Color.WHITE);
        btnAggiungiEsercizio.setFocusPainted(false);
        btnAggiungiEsercizio.setOpaque(true);
        btnAggiungiEsercizio.setBorderPainted(false);
        btnAggiungiEsercizio.addActionListener(e -> clickAggiungiEsercizio());

        //  Anteprima
        txtAnteprimaBozza = new JTextArea(5, 20);
        txtAnteprimaBozza.setEditable(false);
        txtAnteprimaBozza.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollBozza = new JScrollPane(txtAnteprimaBozza);
        scrollBozza.setBorder(BorderFactory.createTitledBorder(null, "Esercizi in questa sessione", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontTesto));

        // Salva Sessione
        btnSalvaSessione = new JButton("SALVA SESSIONE COMPLETA");
        btnSalvaSessione.setFont(fontTitoli);
        btnSalvaSessione.setBackground(new Color(46, 139, 87)); 
        btnSalvaSessione.setForeground(Color.WHITE);
        btnSalvaSessione.setPreferredSize(new Dimension(100, 40));
        btnSalvaSessione.setFocusPainted(false);
        btnSalvaSessione.setOpaque(true);
        btnSalvaSessione.setBorderPainted(false); 
        btnSalvaSessione.addActionListener(e -> clickSalvaSessioneCompleta());

        JPanel panelMiddleContainer = new JPanel(new BorderLayout(10, 10));
        panelMiddleContainer.add(panelInput, BorderLayout.NORTH);
        panelMiddleContainer.add(btnAggiungiEsercizio, BorderLayout.CENTER);
        panelMiddleContainer.add(scrollBozza, BorderLayout.SOUTH);

        panelForm.add(panelMiddleContainer, BorderLayout.CENTER);
        panelForm.add(btnSalvaSessione, BorderLayout.SOUTH);

        add(panelForm, BorderLayout.CENTER);

        // Storico Allenamenti
        panelStoricoContainer = new JPanel();
        panelStoricoContainer.setLayout(new BoxLayout(panelStoricoContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panelStoricoContainer);
        scrollPane.setBorder(BorderFactory.createTitledBorder(null, "I Miei Allenamenti", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontTitoli));
        scrollPane.setPreferredSize(new Dimension(500, 250));
        add(scrollPane, BorderLayout.SOUTH);

        panelForm.setVisible(false);
        impostaStatoForm(false);
    }

    public void setUtenteCorrente(Cliente cliente) {
        this.utenteCorrente = cliente;
    }

    public void clickAccediStorico(Cliente cliente) {
        if (controller != null && cliente != null) {
            try {
                controller.gestisciAccessoSezione(cliente);
            } catch (UtenteNonPremiumException e) {
                mostraBloccoUpgradePremium();
            }
        }
    }

    private void clickAggiungiEsercizio() {
        try {
            String categoria = (String) comboCategoria.getSelectedItem();
            if (categoria.equals("Seleziona...")) {
                JOptionPane.showMessageDialog(this, "Seleziona un gruppo muscolare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nomeEsercizio = txtNomeEsercizio.getText();
            if (nomeEsercizio == null || nomeEsercizio.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci il nome dell'esercizio.", "Attenzione", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double carichi = Double.parseDouble(txtCarichi.getText());
            int ripetizioni = Integer.parseInt(txtRipetizioni.getText());

            String nomeEsercizioFormattato = "[" + categoria + "] " + nomeEsercizio;

            DatiFormPojo nuovoEsercizio = new DatiFormPojo(nomeEsercizioFormattato, carichi, ripetizioni);
            eserciziInBozza.add(nuovoEsercizio);
            
            aggiornaAnteprimaBozza();
            
            txtNomeEsercizio.setText("");
            txtCarichi.setText("");
            txtRipetizioni.setText("");
            txtNomeEsercizio.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Carico e Ripetizioni devono essere numeri validi.", "Errore Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    // GESTIONE DELLE ECCEZIONI
    private void clickSalvaSessioneCompleta() {
        try {
            Date data = new SimpleDateFormat("dd/MM/yyyy").parse(txtData.getText());
            
            controller.salvaSessioneCompleta(data, eserciziInBozza, utenteCorrente);
            
            eserciziInBozza.clear();
            aggiornaAnteprimaBozza();
            JOptionPane.showMessageDialog(this, "Sessione di allenamento salvata con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato data errato. Utilizza gg/mm/aaaa.", "Errore Input", JOptionPane.WARNING_MESSAGE);
            
        } catch (SchedaVuotaException ex) {
            JOptionPane.showMessageDialog(this, "Non hai inserito nessun esercizio. La scheda non può essere vuota!", "Scheda Vuota", JOptionPane.WARNING_MESSAGE);
            
        } catch (DatiAllenamentoNonValidiException ex) {
            JOptionPane.showMessageDialog(this, "I parametri inseriti non sono validi. Controlla che ripetizioni e carichi siano corretti.", "Dati Non Validi", JOptionPane.WARNING_MESSAGE);
            
        } catch (SalvataggioFallitoException ex) {
            JOptionPane.showMessageDialog(this, "Impossibile contattare il server. Riprova più tardi.\nDettaglio: " + ex.getMessage(), "Errore di Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void aggiornaAnteprimaBozza() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (DatiFormPojo ex : eserciziInBozza) {
            sb.append(i++).append(". ").append(ex.getNomeEsercizio().toUpperCase())
              .append(" | ").append(ex.getCarichi()).append(" kg x ").append(ex.getRipetizioni()).append(" rep \n");
        }
        txtAnteprimaBozza.setText(sb.toString());
    }

    // =========================================================================
    // INSERITO IL BOTTONE SBLOCCA PREMIUM NEL CONTAINER CENTRALE
    // =========================================================================
    public void mostraBloccoUpgradePremium() {
        panelForm.setVisible(false); 
        impostaStatoForm(false);
        
        panelStoricoContainer.removeAll();
        
        // Creiamo la label col messaggio
        JLabel lblMessaggio = new JLabel("Accesso Negato: L'area allenamenti è riservata agli utenti Premium.");
        lblMessaggio.setFont(fontTesto);
        lblMessaggio.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra il testo
        
        // Centriamo anche il bottone
        btnSimulaAccesso.setAlignmentX(Component.CENTER_ALIGNMENT); 
        // Dimensioni personalizzate per renderlo simile a un tasto "Accedi"
        btnSimulaAccesso.setMaximumSize(new Dimension(300, 45)); 
        
        // Aggiungiamo un po' di spazio e poi i componenti
        panelStoricoContainer.add(Box.createVerticalStrut(30)); 
        panelStoricoContainer.add(lblMessaggio);
        panelStoricoContainer.add(Box.createVerticalStrut(20)); // Spazio tra scritta e bottone
        panelStoricoContainer.add(btnSimulaAccesso);
        
        this.revalidate();
        this.repaint();
    }
    // =========================================================================

    public void mostraModuloInserimento() {
        panelForm.setVisible(true); 
        impostaStatoForm(true);
        mostraMessaggioNelContainer("Modulo sbloccato. Visualizza qui i tuoi allenamenti precedenti:");
        this.revalidate();
        this.repaint();
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
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true) 
                ));
                panelScheda.setBackground(Color.WHITE);

                String gruppoMuscolare = "Mista"; 
                
                if (!sessione.getEsercizi().isEmpty()) {
                    java.util.Set<String> categorieUniche = new java.util.HashSet<>();
                    
                    for (DatiFormPojo esercizio : sessione.getEsercizi()) {
                        String nomeEs = esercizio.getNomeEsercizio();
                        if (nomeEs.startsWith("[")) {
                            String cat = nomeEs.substring(1, nomeEs.indexOf("]"));
                            categorieUniche.add(cat);
                        }
                    }
                    
                    if (categorieUniche.size() == 1) {
                        gruppoMuscolare = categorieUniche.iterator().next(); 
                    } else if (categorieUniche.size() > 1) {
                        gruppoMuscolare = "MISTA";
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Sessione di ").append(gruppoMuscolare).append(" in data : ").append(sdf.format(sessione.getData())).append("\n\n");
                
                for (DatiFormPojo esercizio : sessione.getEsercizi()) {
                
                    String nomePulito = esercizio.getNomeEsercizio();
                    if (nomePulito.startsWith("[")) {
                        nomePulito = nomePulito.substring(nomePulito.indexOf("]") + 1).trim();
                    }
                    
                    sb.append(String.format("   ▸ %s | %.1f kg | %d rep\n", 
                              nomePulito.toUpperCase(), esercizio.getCarichi(), esercizio.getRipetizioni()));
                }

                JTextArea txtDettagli = new JTextArea(sb.toString());
                txtDettagli.setFont(new Font("Monospaced", Font.PLAIN, 13));
                txtDettagli.setEditable(false);
                txtDettagli.setOpaque(false); 

                JButton btnElimina = new JButton("Elimina");
                btnElimina.setForeground(Color.RED);
                btnElimina.setBackground(Color.WHITE);
                btnElimina.setFocusPainted(false);
                
                btnElimina.addActionListener(e -> {
                    int conferma = JOptionPane.showConfirmDialog(this, "Vuoi eliminare questa sessione?", "Conferma", JOptionPane.YES_NO_OPTION);
                    if (conferma == JOptionPane.YES_OPTION) {
                        try {
                            controller.eliminaSessioneSelezionata(sessione, utenteCorrente);
                        } catch (SalvataggioFallitoException ex) {
                            JOptionPane.showMessageDialog(this, "Impossibile eliminare dal Database: " + ex.getMessage(), "Errore di Sistema", JOptionPane.ERROR_MESSAGE);
                        }
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
        comboCategoria.setEnabled(abilitato);
        txtNomeEsercizio.setEnabled(abilitato);
        txtCarichi.setEnabled(abilitato);
        txtRipetizioni.setEnabled(abilitato);
        btnAggiungiEsercizio.setEnabled(abilitato);
        btnSalvaSessione.setEnabled(abilitato);
    }
    
    private void mostraMessaggioNelContainer(String messaggio) {
        panelStoricoContainer.removeAll();
        JLabel lblMessaggio = new JLabel(messaggio);
        lblMessaggio.setFont(fontTesto);
        lblMessaggio.setAlignmentX(Component.CENTER_ALIGNMENT); 
        lblMessaggio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelStoricoContainer.add(lblMessaggio);
        panelStoricoContainer.revalidate();
        panelStoricoContainer.repaint();
    }
}