package it.unipv.posfw.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import it.unipv.posfw.controller.LoginController;

public class LoginView extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnAccedi;
    private JButton btnRegistrati; // NUOVO BOTTONE
    private LoginController controller;

    public LoginView() {
        setTitle("ScriptActive - Login");
        setSize(350, 240); // Finestra leggermente più alta per far spazio al link
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la finestra sullo schermo
        setResizable(false);
        setLayout(new BorderLayout());

        inizializzaComponenti();
    }

    public void setController(LoginController controller) {
        this.controller = controller;
    }

    private void inizializzaComponenti() {
        // --- FORM DI INPUT ---
        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        panelForm.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        panelForm.add(txtEmail);

        panelForm.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panelForm.add(txtPassword);

        add(panelForm, BorderLayout.CENTER);

        // --- BOTTONI E LINK ---
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
        
        btnAccedi.addActionListener((ActionEvent e) -> {
            if (controller != null) {
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());
                controller.effettuaLogin(email, password);
            }
        });

        btnRegistrati = new JButton("<html><u>Non sei registrato? Registrati</u></html>");
        btnRegistrati.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegistrati.setContentAreaFilled(false);
        btnRegistrati.setBorderPainted(false);
        btnRegistrati.setForeground(new Color(77, 43, 107)); 
  
        
        
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

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore di Accesso", JOptionPane.ERROR_MESSAGE);
    }
}