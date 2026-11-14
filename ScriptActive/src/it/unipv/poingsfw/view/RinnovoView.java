package it.unipv.poingsfw.view;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import it.unipv.poingsfw.controller.RinnovoController;
import it.unipv.poingsfw.controller.vista.VistaRinnovo;
import it.unipv.poingsfw.domain.LivelloAbbonamento;

/**
 * La classe {@code RinnovoView} gestisce l'interfaccia grafica per il rinnovo 
 * manuale di un abbonamento scaduto.
 * <p>
 * Implementata con <b>Java Swing</b>, questa finestra viene 
 * tipicamente innescata durante la fase di autenticazione, qualora il sistema rilevi 
 * che l'utente possiede credenziali valide ma un piano di abbonamento non più attivo.
 * Permette all'utente di selezionare un nuovo piano tariffario.
 * </p>
 * <p>
 * La vista si limita a raccogliere la scelta dell'utente e a mostrarne l'esito:
 * l'aggiornamento dell'abbonamento e il ritorno al login sono compito del
 * {@link RinnovoController}.
 * </p>
 * * @author Arianna Padula
 * @version 2.0
 */

public class RinnovoView extends JFrame implements VistaRinnovo {

    /* Le voci del menu seguono l'ordine dei valori di LivelloAbbonamento */
    private static final LivelloAbbonamento[] LIVELLI = {
            LivelloAbbonamento.MENSILE, LivelloAbbonamento.SEMESTRALE, LivelloAbbonamento.ANNUALE
    };
    private static final String[] PIANI = { "Mensile - €40.00", "Semestrale - €200.00", "Annuale - €360.00" };

    private JComboBox<String> comboPiani;
    private JButton btnPaga;
    private JLabel lblMessaggio;
    private RinnovoController controller;

    public RinnovoView() {
        setTitle("Rinnovo Abbonamento");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la finestra
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNord = new JPanel();
        lblMessaggio = new JLabel("Il tuo abbonamento è scaduto. Seleziona un piano per rinnovare:");
        lblMessaggio.setFont(new Font("Arial", Font.BOLD, 12));
        pnlNord.add(lblMessaggio);
        add(pnlNord, BorderLayout.NORTH);

        JPanel pnlCentro = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        pnlCentro.add(new JLabel("Scegli la durata del nuovo abbonamento:"));
        
        comboPiani = new JComboBox<>(PIANI);
        pnlCentro.add(comboPiani);
        add(pnlCentro, BorderLayout.CENTER);

        JPanel pnlSud = new JPanel();
        btnPaga = new JButton("Paga e Attiva");
        btnPaga.setFont(new Font("Arial", Font.BOLD, 14));
        
        btnPaga.addActionListener(e -> {
            if (controller != null) {
                controller.confermaRinnovo(getLivelloSelezionato());
            }
        });
        
        pnlSud.add(btnPaga);
        add(pnlSud, BorderLayout.SOUTH);

        /* Chiudere la finestra equivale a rinunciare al rinnovo: decide il Controller dove tornare */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (controller != null) {
                    controller.annulla();
                } else {
                    dispose();
                }
            }
        });
    }

    public void setController(RinnovoController controller) {
        this.controller = controller;
    }

    private LivelloAbbonamento getLivelloSelezionato() {
        int indice = comboPiani.getSelectedIndex();
        return indice >= 0 ? LIVELLI[indice] : null;
    }

    @Override
    public void mostraRinnovoCompletato(LivelloAbbonamento livello) {
        JOptionPane.showMessageDialog(this, 
            "Pagamento simulato con successo!\nIl tuo abbonamento è stato aggiornato con il piano: " + livello, 
            "Rinnovo Completato", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void chiudi() {
        dispose();
    }
}
