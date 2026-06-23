package it.unipv.poingsfw.view;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox; 
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import it.unipv.poingsfw.controller.GestoreRegistrazione;
import it.unipv.poingsfw.database.SedeDAOMySQL;
import it.unipv.poingsfw.domain.LivelloAbbonamento;
import it.unipv.poingsfw.domain.Sede;
import it.unipv.poingsfw.domain.TipoAbbonamento;

/**
 * La classe {@code RegistrazioneView} gestisce l'interfaccia grafica (GUI) per 
 * l'iscrizione di un nuovo cliente al sistema.
 * <p>
 * Sviluppata utilizzando il framework <b>Java Swing</b>, questa classe rappresenta il 
 * livello <b>View</b> nel pattern architetturale MVC. Implementa un'interfaccia guidata a più step) sfruttando il {@link CardLayout} per guidare l'utente 
 * dall'inserimento dei dati anagrafici fino al pagamento, mantenendo lo stato 
 * della compilazione in un'unica finestra.
 * </p>
 * * @author Arianna Padula
 * @version 1.2
 */

public class RegistrazioneView extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GestoreRegistrazione gestore;

    // Campi dati (Step 1)
    private JTextField txtNome, txtCognome, txtCF, txtEmail;
    private JPasswordField txtPassword;

    // Scelte (Step 2)
    private JComboBox<Sede> comboSede;
    private JComboBox<TipoAbbonamento> comboTipo;
    private JComboBox<LivelloAbbonamento> comboLivello;

    // Pagamento (Step 3)
    private JTextField txtIBAN;
    private JCheckBox chkRinnovo; // VARIABILE AGGIUNTA
    
    // Dimensioni standard per input e pulsanti
    private final Dimension fieldSize = new Dimension(280, 35);
    private final Dimension buttonSize = new Dimension(200, 45);

    /**
     * Costruttore della vista. Inizializza il collegamento con il Controller 
     * e costruisce l'interfaccia utente.
     */
    
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

    // --- STEP 1: DATI ANAGRAFICI ---
    private JPanel creaStep1() {
        JPanel p = creaBasePanel("1. Crea il tuo Account ScriptActive!");
        
        aggiungiLabelEInput(p, "Nome", txtNome = new JTextField());
        aggiungiLabelEInput(p, "Cognome", txtCognome = new JTextField());
        aggiungiLabelEInput(p, "Codice Fiscale", txtCF = new JTextField());
        aggiungiLabelEInput(p, "Email", txtEmail = new JTextField());
        aggiungiLabelEInput(p, "Password", txtPassword = new JPasswordField());

      
        JButton btnNext = creaBlueButton("AVANTI");
     
        btnNext.setBackground(new Color(77, 43, 107));
        btnNext.setForeground(Color.WHITE);
        btnNext.setFocusPainted(false);
        btnNext.setOpaque(true);
        btnNext.setBorderPainted(false);
        btnNext.addActionListener(e -> {
            String password = new String(txtPassword.getPassword());
            
            if (txtNome.getText().trim().isEmpty() ||
                txtCognome.getText().trim().isEmpty() ||
                txtCF.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() ||
                password.trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(this, "Tutti i campi sono obbligatori per poter procedere.", "Dati Mancanti", JOptionPane.WARNING_MESSAGE);
                return; 
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "La password deve contenere almeno 6 caratteri.", "Password Debole", JOptionPane.WARNING_MESSAGE);
                return; 
            }
            
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

        comboSede = new JComboBox<>(); 

        SedeDAOMySQL sedeDao = new SedeDAOMySQL();
        java.util.List<Sede> listaSedi = sedeDao.getTutteLeSedi();

        for (Sede s : listaSedi) {
            comboSede.addItem(s);
        }
        
        configuresComponenteFixed(comboSede); 
        p.add(comboSede); 
        
        p.add(Box.createVerticalStrut(20));

        JLabel lblTipo = new JLabel("Piano Abbonamento");
        lblTipo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTipo);
        comboTipo = new JComboBox<>(TipoAbbonamento.values());
        configuresComponenteFixed(comboTipo);
        p.add(comboTipo);
        p.add(Box.createVerticalStrut(20));

        JLabel lblLivello = new JLabel("Durata Abbonamento");
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

        chkRinnovo = new JCheckBox("Attiva il rinnovo automatico alla scadenza");
        chkRinnovo.setBackground(Color.WHITE);
        chkRinnovo.setFocusPainted(false);
        chkRinnovo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(chkRinnovo);
        p.add(Box.createVerticalStrut(20));

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

    /**
     * Raccoglie tutti i dati dai vari componenti grafici e invoca il Controller 
     * per finalizzare il processo di registrazione.
     * <p>
     * Intercetta e gestisce visivamente tutte le eccezioni di business sollevate dal 
     * Controller (es. utente duplicato, pagamento rifiutato), mostrando all'utente 
     * finestre di dialogo dedicate (JOptionPane).
     * </p>
     * * @param e L'evento scatenato dalla pressione del pulsante finale.
     */
    
    private void handleFinalRegistration(ActionEvent e) {
        try {
            String ibanInserito = txtIBAN.getText().trim();
            if (ibanInserito.isEmpty() || ibanInserito.length() < 15 || !ibanInserito.toUpperCase().startsWith("IT")) {
                JOptionPane.showMessageDialog(this, 
                    "L'IBAN inserito non è nel formato valido.\nDeve iniziare con 'IT' ed avere almeno 15 caratteri.", 
                    "IBAN Non Valido", 
                    JOptionPane.WARNING_MESSAGE);
                return; 
            }

            Sede sedeSelezionata = (Sede) comboSede.getSelectedItem(); 
            boolean rinnovoScelto = chkRinnovo.isSelected(); // LETTURA DELLA SPUNTA
            System.out.println("[DEBUG VIEW] La spunta nell'interfaccia vale: " + rinnovoScelto);
            
            gestore.registraNuovoCliente(
                    txtNome.getText(), txtCognome.getText(), txtEmail.getText(),
                    new String(txtPassword.getPassword()), txtCF.getText(),
                    sedeSelezionata, (TipoAbbonamento)comboTipo.getSelectedItem(),
                    (LivelloAbbonamento)comboLivello.getSelectedItem(), 
                    rinnovoScelto, // PASSATO AL CONTROLLER
                    txtIBAN.getText()
            );

            JOptionPane.showMessageDialog(this, "Iscrizione completata! Benvenuto in palestra.", "Successo", JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); 

        } catch (it.unipv.poingsfw.exceptions.DatiRegistrazioneNonValidiException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dati Incompleti", JOptionPane.WARNING_MESSAGE);
        } catch (it.unipv.poingsfw.exceptions.UtenteGiaEsistenteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Utente Duplicato", JOptionPane.ERROR_MESSAGE);
            cardLayout.show(mainPanel, "STEP1"); 
        } catch (it.unipv.poingsfw.exceptions.PagamentoFallitoException ex) {
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
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                RegistrazioneView view = new RegistrazioneView();
                view.mostraModuloRegistrazione(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}