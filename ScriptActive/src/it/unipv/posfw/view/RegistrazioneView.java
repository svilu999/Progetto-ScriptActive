package it.unipv.posfw.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

// Imports del Controller
import it.unipv.posfw.controller.GestoreRegistrazione;
// Imports del Domain
import it.unipv.posfw.domain.LivelloAbbonamento;
import it.unipv.posfw.domain.Sede;
import it.unipv.posfw.domain.TipoAbbonamento;

public class RegistrazioneView extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GestoreRegistrazione gestore;

    // Campi dati (Step 1)
    private JTextField txtNome, txtCognome, txtCF, txtEmail;
    private JPasswordField txtPassword;

    // Scelte (Step 2)
    private JComboBox<String> comboSede;
    private JComboBox<TipoAbbonamento> comboTipo;
    private JComboBox<LivelloAbbonamento> comboLivello;

    // Pagamento (Step 3)
    private JTextField txtIBAN;
    
    // Dimensioni standard per input e pulsanti
    private final Dimension fieldSize = new Dimension(280, 35);
    private final Dimension buttonSize = new Dimension(200, 45);

    public RegistrazioneView() {
        this.gestore = GestoreRegistrazione.getIstanza();
        initUI();
    }

    private void initUI() {
        setTitle("Palestra POSFW - Iscrizione");
        setSize(500, 750); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Color.WHITE);

        // Creazione dei tre step
        mainPanel.add(creaStep1(), "STEP1");
        mainPanel.add(creaStep2(), "STEP2");
        mainPanel.add(creaStep3(), "STEP3");

        add(mainPanel);
    }

    // --- STEP 1: DATI ANAGRAFICI (MODIFICATO CON BLOCCO VALIDAZIONE) ---
    private JPanel creaStep1() {
        JPanel p = creaBasePanel("1. Crea il tuo Account ScriptActive!");
        
        aggiungiLabelEInput(p, "Nome", txtNome = new JTextField());
        aggiungiLabelEInput(p, "Cognome", txtCognome = new JTextField());
        aggiungiLabelEInput(p, "Codice Fiscale", txtCF = new JTextField());
        aggiungiLabelEInput(p, "Email", txtEmail = new JTextField());
        aggiungiLabelEInput(p, "Password", txtPassword = new JPasswordField());

        JButton btnNext = creaBlueButton("AVANTI");
        
        // LOGICA DI BLOCCO INSERITA QUI
        btnNext.addActionListener(e -> {
            String password = new String(txtPassword.getPassword());
            
            // 1. Controllo se qualche campo è vuoto
            if (txtNome.getText().trim().isEmpty() ||
                txtCognome.getText().trim().isEmpty() ||
                txtCF.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() ||
                password.trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(this, "Tutti i campi sono obbligatori per poter procedere.", "Dati Mancanti", JOptionPane.WARNING_MESSAGE);
                return; // INTERROMPE L'ESECUZIONE: l'utente non va al prossimo step!
            }
            
            // 2. Controllo lunghezza password (coerente con le regole del GestoreRegistrazione)
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "La password deve contenere almeno 6 caratteri.", "Password Debole", JOptionPane.WARNING_MESSAGE);
                return; // INTERROMPE L'ESECUZIONE
            }
            
            // Se i controlli passano, allora mostra lo Step 2
            cardLayout.show(mainPanel, "STEP2");
        });
        
        p.add(Box.createVerticalStrut(30));
        p.add(creaCenteredButtonPanel(btnNext));

        return p;
    }

    // --- STEP 2: SCELTA SEDE E ABBONAMENTO ---
    private JPanel creaStep2() {
        JPanel p = creaBasePanel("2. Sede e Piano");

        JLabel lblSede = new JLabel("Sede di Riferimento");
        lblSede.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblSede);
        String[] sediDisponibili = {"Sede Centrale Milano", "Sede Roma Sud", "Sede Torino Centro", "Sede Napoli Vomero"};
        comboSede = new JComboBox<>(sediDisponibili);
        configuresComponenteFixed(comboSede);
        p.add(comboSede);
        p.add(Box.createVerticalStrut(20));

        JLabel lblTipo = new JLabel("Durata Abbonamento");
        lblTipo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTipo);
        comboTipo = new JComboBox<>(TipoAbbonamento.values());
        configuresComponenteFixed(comboTipo);
        p.add(comboTipo);
        p.add(Box.createVerticalStrut(20));

        JLabel lblLivello = new JLabel("Livello Account");
        lblLivello.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblLivello);
        comboLivello = new JComboBox<>(LivelloAbbonamento.values());
        configuresComponenteFixed(comboLivello);
        p.add(comboLivello);

        JButton btnNext = creaBlueButton("PROCEDI AL PAGAMENTO");
        btnNext.addActionListener(e -> cardLayout.show(mainPanel, "STEP3"));
        
        p.add(Box.createVerticalStrut(40));
        p.add(creaCenteredButtonPanel(btnNext));

        return p;
    }

    // --- STEP 3: PAGAMENTO ---
    private JPanel creaStep3() {
        JPanel p = creaBasePanel("3. Concludi Iscrizione");

        aggiungiLabelEInput(p, "IBAN per l'addebito", txtIBAN = new JTextField());

        JButton btnConfirm = creaBlueButton("PAGA E ATTIVA ORA");
        btnConfirm.addActionListener(this::handleFinalRegistration);
        
        p.add(Box.createVerticalStrut(40));
        p.add(creaCenteredButtonPanel(btnConfirm));

        JButton btnBack = new JButton("Torna indietro");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "STEP1"));
        p.add(Box.createVerticalStrut(15));
        p.add(btnBack);

        return p;
    }

    // --- LOGICA DI SALVATAGGIO ---
    private void handleFinalRegistration(ActionEvent e) {
        try {
            // Controllo validità formale dell'IBAN
            String ibanInserito = txtIBAN.getText().trim();
            if (ibanInserito.isEmpty() || ibanInserito.length() < 15 || !ibanInserito.toUpperCase().startsWith("IT")) {
                JOptionPane.showMessageDialog(this, 
                    "L'IBAN inserito non è nel formato valido.\nDeve iniziare con 'IT' ed avere almeno 15 caratteri.", 
                    "IBAN Non Valido", 
                    JOptionPane.WARNING_MESSAGE);
                return; // Blocca immediatamente l'esecuzione
            }

            String nomeSedeScelta = (String) comboSede.getSelectedItem();
            Sede sedeSelezionata = new Sede("S01", nomeSedeScelta); 
            
            gestore.registraNuovoCliente(
                    txtNome.getText(), txtCognome.getText(), txtEmail.getText(),
                    new String(txtPassword.getPassword()), txtCF.getText(),
                    sedeSelezionata, (TipoAbbonamento)comboTipo.getSelectedItem(),
                    (LivelloAbbonamento)comboLivello.getSelectedItem(), txtIBAN.getText()
            );

            JOptionPane.showMessageDialog(this, "Iscrizione completata! Benvenuto in palestra.", "Successo", JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); 

        } catch (it.unipv.posfw.exceptions.DatiRegistrazioneNonValidiException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dati Incompleti", JOptionPane.WARNING_MESSAGE);
        } catch (it.unipv.posfw.exceptions.UtenteGiaEsistenteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Utente Duplicato", JOptionPane.ERROR_MESSAGE);
            cardLayout.show(mainPanel, "STEP1"); 
        } catch (it.unipv.posfw.exceptions.PagamentoFallitoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Transazione Negata", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore di sistema: " + ex.getMessage(), "Errore Critico", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =================================================================================
    // METODI DI UTILITA' ESTETICA
    // =================================================================================

    private JPanel creaBasePanel(String titolo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); 
        p.setBorder(new EmptyBorder(40, 40, 40, 40));
        p.setBackground(Color.WHITE);

        JLabel lblTitolo = new JLabel(titolo);
        lblTitolo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitolo.setForeground(new Color(30, 41, 59));
        lblTitolo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTitolo);
        p.add(Box.createVerticalStrut(30));
        
        return p;
    }

    private void aggiungiLabelEInput(JPanel panel, String labelText, JComponent inputField) {
        JLabel lbl = new JLabel(labelText);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT); 
        panel.add(lbl);
        
        configuresComponenteFixed(inputField); 
        panel.add(inputField);
        panel.add(Box.createVerticalStrut(15)); 
    }

    private void configuresComponenteFixed(JComponent comp) {
        comp.setAlignmentX(Component.CENTER_ALIGNMENT);
        comp.setPreferredSize(fieldSize);
        comp.setMaximumSize(fieldSize); 
    }

    private JButton creaBlueButton(String testo) {
        JButton btn = new JButton(testo);
        btn.setBackground(new Color(37, 99, 235)); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(buttonSize);
        btn.setMinimumSize(buttonSize);
        return btn;
    }

    private JPanel creaCenteredButtonPanel(JButton button) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(Color.WHITE);
        p.add(button);
        p.setAlignmentX(Component.CENTER_ALIGNMENT); 
        return p;
    }

    public void mostraModuloRegistrazione() {
        this.setVisible(true);
    }
}
