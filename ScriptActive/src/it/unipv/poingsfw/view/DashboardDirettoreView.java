package it.unipv.poingsfw.view;
/**
 * Classe Boundary che realizza l'interfaccia principale per l'attore "Direttore", 
 * deputata alla gestione operativa del palinsesto (UC3).
 * 
 * In conformità con lo stile architetturale Model-View-Controller (MVC), questa componente 
 * ricopre il ruolo di View, fungendo da mediatore tra l'utente fisico e la logica 
 * applicativa del sistema. La classe implementa il "Principio di separazione 
 * Modello-Vista", garantendo che la GUI non contenga logica di business né acceda 
 * direttamente alla persistenza (Low Coupling).
 * 
 * La comunicazione verso lo strato inferiore avviene tramite il meccanismo di delega 
 * degli eventi: le azioni dell'utente vengono catturate dai listener e inoltrate al 
 * Controller competente (GestoreCorsi), rispettando i flussi definiti nei diagrammi 
 * di sequenza di progetto.
 * 
 * Il Controller viene ricevuto dal costruttore e la navigazione verso gli altri moduli
 * è delegata a callback registrate dall'esterno: la View non istanzia altre finestre,
 * non usa Singleton e non conosce il modo in cui il sistema viene assemblato.
 * 
 * @author Lorenzo
 * @version 3.0
 * @see GestoreCorsi
 * @see Direttore
 */
import javax.swing.*;

import it.unipv.poingsfw.controller.GestoreCorsi;
import it.unipv.poingsfw.exceptions.CorsoNonTrovatoException;
import it.unipv.poingsfw.exceptions.SovrapposizioneOrarioException;
import it.unipv.poingsfw.exceptions.TrainerNonValidoException;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class DashboardDirettoreView extends JFrame {

    // Componenti della finestra dichiarati a livello globale
    private JTextField txtNomeCorso, txtDataOra, txtCapienza, txtIdPT, txtIdAnnulla;
    private JButton btnOrganizza, btnAnnulla, btnVediPalinsesto, btnGestionePersonale;
    private JTextArea areaLog;

    private static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final GestoreCorsi gestoreCorsi;
    private Runnable azioneVisualizzaPalinsesto = () -> { };
    private Runnable azioneGestionePersonale = () -> { };

    /**
     * @param gestoreCorsi Controller dello UC3 a cui inoltrare le richieste dell'utente.
     */
    public DashboardDirettoreView(GestoreCorsi gestoreCorsi) {
        this.gestoreCorsi = Objects.requireNonNull(gestoreCorsi, "Il Controller non può essere null.");

        // 1. Impostazioni base della finestra
        setTitle("ScriptActive - Dashboard Direttore");
        setSize(550, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));

        // --- ZONA NORD: Creazione Corso ---
        JPanel pnlInput = new JPanel(new GridLayout(5, 2, 5, 10)); 
        pnlInput.setBorder(BorderFactory.createTitledBorder("1. Organizza Nuovo Corso"));

        pnlInput.add(new JLabel(" Nome Corso:"));
        txtNomeCorso = new JTextField();
        pnlInput.add(txtNomeCorso);

        pnlInput.add(new JLabel(" Data/Ora (dd/MM/yyyy HH:mm):"));
        txtDataOra = new JTextField("01/07/2026 18:00");
        pnlInput.add(txtDataOra);

        pnlInput.add(new JLabel(" Capienza Massima:"));
        txtCapienza = new JTextField();
        pnlInput.add(txtCapienza);

        pnlInput.add(new JLabel(" ID Personal Trainer:"));
        txtIdPT = new JTextField();
        pnlInput.add(txtIdPT);

        btnOrganizza = new JButton("Pubblica Corso");
        btnVediPalinsesto = new JButton("Visualizza Palinsesto"); 
        pnlInput.add(btnOrganizza);
        pnlInput.add(btnVediPalinsesto);

        // --- ZONA CENTRO: Contenitore per Annullamento e Navigazione ---
        JPanel pnlCentro = new JPanel();
        pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Sotto-pannello 2.1: Annullamento
        JPanel pnlAnnulla = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pnlAnnulla.setBorder(BorderFactory.createTitledBorder("2. Gestione Corsi Esistenti"));
        pnlAnnulla.add(new JLabel("ID Corso da annullare:"));
        txtIdAnnulla = new JTextField(12);
        btnAnnulla = new JButton("Annulla Corso");
        btnAnnulla.setForeground(Color.RED); 
        
        pnlAnnulla.add(txtIdAnnulla);
        pnlAnnulla.add(btnAnnulla);

        // Sotto-pannello 2.2: Navigazione Esterna
        JPanel pnlNavigazione = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlNavigazione.setBorder(BorderFactory.createTitledBorder("3. Integrazioni Sistema"));
        
        // CORREZIONE APPLICATA QUI: Rimosso "JButton" all'inizio per evitare lo shadowing
        btnGestionePersonale = new JButton("Apri Modulo Gestione Personale");
        pnlNavigazione.add(btnGestionePersonale);

        // Aggiungiamo i due sotto-pannelli al contenitore centrale
        pnlCentro.add(pnlAnnulla);
        pnlCentro.add(Box.createRigidArea(new Dimension(0, 10))); 
        pnlCentro.add(pnlNavigazione);

        // --- ZONA SUD: Area Log Matrix ---
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(Color.BLACK);
        areaLog.setForeground(Color.GREEN);
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setPreferredSize(new Dimension(550, 180));

        // Aggiunta pannelli principali al frame
        add(pnlInput, BorderLayout.NORTH);
        add(pnlCentro, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // Configurazione dei Listener per i click
        configuraEventi();
    }

    /**
     * Registra l'azione da eseguire quando il Direttore chiede di consultare il palinsesto.
     */
    public void onVisualizzaPalinsesto(Runnable azione) {
        this.azioneVisualizzaPalinsesto = Objects.requireNonNull(azione);
    }

    /**
     * Registra l'azione da eseguire quando il Direttore apre il modulo Gestione Personale.
     */
    public void onGestionePersonale(Runnable azione) {
        this.azioneGestionePersonale = Objects.requireNonNull(azione);
    }

    private void configuraEventi() {
        btnOrganizza.addActionListener(e -> organizzaCorso());
        btnAnnulla.addActionListener(e -> annullaCorso());

        btnVediPalinsesto.addActionListener(e -> {
            azioneVisualizzaPalinsesto.run();
            areaLog.append("[VIEW] Apertura finestra Palinsesto Corsi.\n");
        });

        btnGestionePersonale.addActionListener(e -> {
            azioneGestionePersonale.run();
            areaLog.append("[VIEW] Apertura finestra Gestione Personale.\n");
        });
    }

    /**
     * Raccoglie i dati del form, li converte nei tipi attesi dal Controller e gli inoltra
     * la richiesta di creazione del corso (UC3). Gli errori di formato sono errori di input
     * e vengono segnalati direttamente dalla View.
     */
    private void organizzaCorso() {
        String nome = txtNomeCorso.getText().trim();
        String idPT = txtIdPT.getText().trim();
        int capienza;
        LocalDateTime dataOra;

        try {
            capienza = Integer.parseInt(txtCapienza.getText().trim());
        } catch (NumberFormatException ex) {
            mostraErrore("La capienza deve essere un numero intero.");
            return;
        }

        try {
            dataOra = LocalDateTime.parse(txtDataOra.getText().trim(), FORMATO_DATA_ORA);
        } catch (DateTimeParseException ex) {
            mostraErrore("Data e ora non valide. Usa il formato dd/MM/yyyy HH:mm.");
            return;
        }

        try {
            // CHIAMATA AL CONTROLLER (UC3)
            gestoreCorsi.organizzaNuovoCorso(nome, dataOra, capienza, idPT);

            areaLog.append("[SUCCESS] Corso '" + nome + "' creato correttamente.\n");
            JOptionPane.showMessageDialog(this, "Corso creato con successo!");
        } catch (TrainerNonValidoException | SovrapposizioneOrarioException ex) {
            mostraErrore(ex.getMessage());
        } catch (RuntimeException ex) {
            mostraErrore("Impossibile creare il corso. Controlla i dati inseriti e riprova.");
        }
    }

    private void annullaCorso() {
        String id = txtIdAnnulla.getText().trim();
        if (id.isEmpty()) {
            mostraErrore("Inserisci l'ID del corso da annullare.");
            return;
        }

        try {
            gestoreCorsi.annullaCorso(id);

            areaLog.append("[SUCCESS] Corso " + id + " annullato. Notifiche inviate agli iscritti.\n");
            JOptionPane.showMessageDialog(this, "Corso annullato con successo!");
        } catch (CorsoNonTrovatoException ex) {
            mostraErrore(ex.getMessage());
        } catch (RuntimeException ex) {
            mostraErrore("Impossibile annullare il corso: verifica che l'ID sia corretto.");
        }
    }

    private void mostraErrore(String messaggio) {
        areaLog.append("[ERRORE] " + messaggio + "\n");
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
}
