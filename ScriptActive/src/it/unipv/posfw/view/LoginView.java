package it.unipv.posfw.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import it.unipv.posfw.controller.LoginController;

public class LoginView extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnAccedi;
    private LoginController controller;

    public LoginView() {
        setTitle("ScriptActive - Login");
        setSize(350, 200);
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
        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panelForm.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        panelForm.add(txtEmail);

        panelForm.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panelForm.add(txtPassword);

        add(panelForm, BorderLayout.CENTER);

        JPanel panelBottoni = new JPanel();
        btnAccedi = new JButton("Accedi");
        btnAccedi.setBackground(new Color(30, 144, 255)); // Blu
        btnAccedi.setForeground(Color.WHITE);
        btnAccedi.setFocusPainted(false);
        
        // Quando clicco "Accedi", passo i dati al Controller
        btnAccedi.addActionListener((ActionEvent e) -> {
            if (controller != null) {
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());
                controller.effettuaLogin(email, password);
            }
        });

        panelBottoni.add(btnAccedi);
        add(panelBottoni, BorderLayout.SOUTH);
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore di Accesso", JOptionPane.ERROR_MESSAGE);
    }
}