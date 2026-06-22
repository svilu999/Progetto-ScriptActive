package it.unipv.poingsfw.view;

import javax.swing.*;

import it.unipv.poingsfw.controller.LoginController;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * La classe {@code LoginView} rappresenta il componente <b>View</b> nel pattern architetturale <b>MVC (Model-View-Controller)</b>
 * demandato alla gestione del ciclo di vita iniziale dell'applicazione (Autenticazione).
 * <p>
 * Estende {@link javax.swing.JFrame} per l'interfaccia grafica Swing.
 * Aderendo al <b>Principio di Separazione Modello-Vista</b>, la classe isola il rendering grafico e l'acquisizione 
 * degli input (User Actions), demandando la logica di validazione delle credenziali e il routing applicativo 
 * al {@link LoginController}.
 * </p>
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Risponde allo Use Case di <b>Login / Autenticazione</b>. Fornisce all'utente (indipendentemente 
 * dal suo ruolo finale di Cliente, Direttore o Personal Trainer) il modulo di ingresso per l'immissione 
 * delle credenziali, includendo il punto di diramazione (Flusso Alternativo) verso lo Use Case 
 * di <b>Registrazione</b> per i nuovi utenti.
 * </p>
 * * @author Vilucchi
 * @version 1.3
 * @see javax.swing.JFrame
 * @see it.unipv.poingsfw.controller.LoginController
 */
public class LoginView extends JFrame {
    
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnAccedi;
    private JButton btnRegistrati; 
    
    /**
     * Riferimento al Controller associato.
     * Consente l'implementazione del pattern <b>Event Delegation</b>: la vista cattura l'interazione, 
     * estrae lo stato (payload) dai componenti grafici e invoca l'azione corrispondente sul Controller.
     */
    private LoginController controller;

    /**
     * Costruttore di default per la classe {@code LoginView}.
     * <p>
     * Inizializza il <i>Top-level container</i> definendo i vincoli spaziali e di comportamento 
     * della finestra.
     * Per preservare l'integrità del layout del form. Delega la costruzione del <i>Component Tree</i> 
     * al metodo dedicato.
     * </p>
     */
    public LoginView() {
        setTitle("ScriptActive - Login");
        setSize(350, 240); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setResizable(false);
        setLayout(new BorderLayout());

        inizializzaComponenti();
    }

    /**
     * Inietta la dipendenza del Controller in questa Vista (<i>Dependency Injection</i>).
     * <p>
     * Stabilisce il canale di comunicazione unidirezionale (Vista ; Controller) necessario 
     * per notificare i tentativi di accesso.
     * </p>
     * * @param controller L'istanza concreta del {@link LoginController} preposta alla validazione.
     */
    public void setController(LoginController controller) {
        this.controller = controller;
    }

    /**
     * Assembla gerarchicamente i <i>General-purpose containers</i> e i componenti atomici <i>lightweight</i> di Swing.
     * <p>
     * Incapsula la logica di definizione del Layout ({@link GridLayout} per il form, {@link BoxLayout} per i comandi) 
     * e implementa il binding degli eventi (Event Listeners) direttamente sui componenti interattivi.
     * </p>
     */
    private void inizializzaComponenti() {
        
        
        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        panelForm.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        panelForm.add(txtEmail);
        panelForm.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panelForm.add(txtPassword);
        add(panelForm, BorderLayout.CENTER);

        /* Contenitore direzionale per le Call to Action (Accedi / Registrati) */
        JPanel panelBottoni = new JPanel();
        panelBottoni.setLayout(new BoxLayout(panelBottoni, BoxLayout.Y_AXIS));
        panelBottoni.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        btnAccedi = new JButton("Accedi");
        btnAccedi.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAccedi.setBackground(new Color(77, 43, 107)); 
        btnAccedi.setForeground(Color.WHITE);
        btnAccedi.setFocusPainted(false);
        btnAccedi.setOpaque(true);
        btnAccedi.setBorderPainted(false);
        
        /* * Event Delegation: Estrazione del payload (Email e Password raw) e 
         * invocazione del metodo di business sul Controller associato.
         */
        btnAccedi.addActionListener((ActionEvent e) -> {
            if (controller != null) {
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());
                controller.effettuaLogin(email, password);
            }
        });

        /* Rendering visivo e strutturale del pulsante secondario (Registrazione) */
        btnRegistrati = new JButton("<html><u>Non sei registrato? Registrati</u></html>");
        btnRegistrati.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegistrati.setContentAreaFilled(false);
        btnRegistrati.setBorderPainted(false);
        btnRegistrati.setForeground(new Color(77, 43, 107)); 
  
        /*
         * Gestione del routing UI interno.
         * Rilascia le risorse allocate per il modulo di Login e istanzia il frame dedicato 
         * alla fase di Registrazione (transizione di stato della View).
         */
        btnRegistrati.addActionListener(e -> {
            this.dispose();
            RegistrazioneView regView = new RegistrazioneView();
            regView.setVisible(true);
        });
        
        panelBottoni.add(btnAccedi);
        panelBottoni.add(Box.createRigidArea(new Dimension(0, 10))); 
        panelBottoni.add(btnRegistrati);
        
        add(panelBottoni, BorderLayout.SOUTH);
    }

    /**
     * Fornisce un feedback visivo immediato (Modale di Errore) a seguito di una violazione 
     * o di un fallimento rilevato dal Controller.
     * <p>
     * Utilizza {@link JOptionPane} per sospendere temporaneamente il flusso e garantire la 
     * corretta lettura del messaggio diagnostico da parte dell'attore.
     * </p>
     * * @param messaggio Il payload testuale descrittivo della causa di fallimento (es. "Credenziali errate").
     */
    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore di Accesso", JOptionPane.ERROR_MESSAGE);
    }
    /**
     * Mostra un dialog interattivo per il rinnovo dell'abbonamento.
     * Se l'utente accetta, chiude il login e apre la View di rinnovo.
     * * @param email L'email dell'utente bloccato, da passare alla schermata di rinnovo
     * @param messaggio Il messaggio di errore catturato dall'eccezione
     */
    public void mostraPopupRinnovo(String email, String messaggio) {
        int scelta = JOptionPane.showConfirmDialog(
            this,
            messaggio + "\n\nVuoi procedere subito al rinnovo?",
            "Abbonamento Scaduto",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (scelta == JOptionPane.YES_OPTION) {
            this.dispose(); // Chiude la schermata di login
            
            // Apre la tua nuova schermata passando l'email
            RinnovoView schermataRinnovo = new RinnovoView(email);
            schermataRinnovo.setVisible(true);
        }
    }
}