package it.unipv.poingsfw.view;
/**
 * Classe Boundary deputata alla gestione dell'interfaccia utente per il palinsesto corsi.
 * 
 * In conformità con lo stile architetturale Model-View-Controller (MVC), questa classe ricopre 
 * il ruolo di View, incaricata della presentazione dei dati e della cattura degli eventi utente.
 * Rispetta il "Principio di separazione Modello-Vista": la View non possiede logica applicativa 
 * né accede direttamente alla persistenza, ma delega le operazioni di sistema ai Controller 
 * competenti (GestoreCorsi e GestorePrenotazioni) realizzando il Low Coupling.
 * 
 * I Controller vengono ricevuti dal costruttore: la View non li istanzia e non usa Singleton.
 * Se viene aperta senza un cliente autenticato (Direttore, Personal Trainer) funziona in sola
 * consultazione e nasconde i comandi di prenotazione. Il ritorno alla schermata precedente
 * è delegato a una callback registrata dall'esterno.
 * 
 * @author Lorenzo
 * @version 3.0
 * @see GestoreCorsi
 * @see GestorePrenotazioni
 */
import javax.swing.*;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Objects;

import it.unipv.poingsfw.controller.GestoreCorsi;
import it.unipv.poingsfw.controller.GestorePrenotazioni;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;
import it.unipv.poingsfw.exceptions.CorsoAlCompletoException;
import it.unipv.poingsfw.exceptions.PrenotazioneGiaEffettuataException;
import it.unipv.poingsfw.exceptions.PrenotazioneInesistenteException;

public class PalinsestoCorsiView extends JFrame {

    private JTable tabellaCorsi;
    private DefaultTableModel modelloTabella;

    private JButton btnAggiorna;
    private JButton btnIndietro;
    private JButton btnPrenota;
    private JButton btnAnnulla;

    /*
     * Lista mantenuta in memoria per recuperare l'oggetto Corso
     * corrispondente alla riga selezionata nella tabella.
     */
    private List<Corso> corsiAttuali;

    private final GestoreCorsi gestoreCorsi;
    private final GestorePrenotazioni gestorePrenotazioni;
    private final Cliente clienteLoggato;
    private Runnable azioneIndietro = () -> { };

    /**
     * @param gestoreCorsi        Controller da cui ottenere il palinsesto.
     * @param gestorePrenotazioni Controller a cui inoltrare prenotazioni e annullamenti.
     * @param clienteLoggato      Il cliente autenticato, oppure {@code null} per la sola consultazione.
     */
    public PalinsestoCorsiView(GestoreCorsi gestoreCorsi, GestorePrenotazioni gestorePrenotazioni,
                               Cliente clienteLoggato) {
        this.gestoreCorsi = Objects.requireNonNull(gestoreCorsi, "Il Controller dei corsi non può essere null.");
        this.gestorePrenotazioni = Objects.requireNonNull(gestorePrenotazioni, "Il Controller delle prenotazioni non può essere null.");
        this.clienteLoggato = clienteLoggato;

        setTitle("ScriptActive - Palinsesto Completo");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));


        String[] colonne = {
                "ID Corso",
                "Nome",
                "Data e Ora",
                "Trainer",
                "Posti",
                "Stato"
        };

        modelloTabella = new DefaultTableModel(colonne, 0) {
            /*
             * Impedisce la modifica diretta delle celle della tabella.
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabellaCorsi = new JTable(modelloTabella);
        tabellaCorsi.setFillsViewportHeight(true);
        tabellaCorsi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tabellaCorsi);
        add(scrollPane, BorderLayout.CENTER);

        JPanel pnlBottoni = new JPanel();

        btnAggiorna = new JButton("Aggiorna Palinsesto");
        btnPrenota = new JButton("Prenota Corso Selezionato");
        btnAnnulla = new JButton("Annulla Prenotazione");
        btnIndietro = new JButton("Indietro");

        /* Senza un cliente autenticato il palinsesto è in sola consultazione */
        boolean prenotazioniAbilitate = clienteLoggato != null;
        btnPrenota.setVisible(prenotazioniAbilitate);
        btnAnnulla.setVisible(prenotazioniAbilitate);

        pnlBottoni.add(btnAggiorna);
        pnlBottoni.add(btnPrenota);
        pnlBottoni.add(btnAnnulla);
        pnlBottoni.add(btnIndietro);

        add(pnlBottoni, BorderLayout.SOUTH);

        btnAggiorna.addActionListener(e -> caricaDati());
        btnPrenota.addActionListener(e -> prenotaCorsoCliccato());
        btnAnnulla.addActionListener(e -> annullaCorsoCliccato());
        btnIndietro.addActionListener(e -> tornaIndietro());

        /* La chiusura della finestra equivale al comando "Indietro" */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tornaIndietro();
            }
        });

        caricaDati();
    }

    /**
     * Registra l'azione da eseguire dopo la chiusura del palinsesto
     * (es. riaprire la dashboard da cui si proviene).
     */
    public void onIndietro(Runnable azione) {
        this.azioneIndietro = Objects.requireNonNull(azione);
    }

    private void tornaIndietro() {
        dispose();
        azioneIndietro.run();
    }

    public void caricaDati() {
        modelloTabella.setRowCount(0);

        corsiAttuali = gestoreCorsi.getPalinsestoCorsi();

        for (Corso c : corsiAttuali) {
            String nomeCompletoTrainer = "Nessun trainer";

            if (c.getTrainerAssegnato() != null) {
                nomeCompletoTrainer =
                        c.getTrainerAssegnato().getNome()
                                + " "
                                + c.getTrainerAssegnato().getCognome();
            }

            Object[] riga = {
                    c.getIdCorso(),
                    c.getNome(),
                    c.getDataOra().toString().replace("T", " "),
                    nomeCompletoTrainer,
                    c.getPostiDisponibili(),
                    c.getStato()
            };

            modelloTabella.addRow(riga);
        }
    }

    private void prenotaCorsoCliccato() {
        int rigaSelezionata = tabellaCorsi.getSelectedRow();

        if (rigaSelezionata == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleziona un corso dalla tabella prima di cliccare su Prenota.",
                    "Attenzione",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }


        Corso corsoDaPrenotare = corsiAttuali.get(rigaSelezionata);

        

        try {
            gestorePrenotazioni.prenotaCorso(clienteLoggato, corsoDaPrenotare);

            JOptionPane.showMessageDialog(
                    this,
                    "Prenotazione al corso di "
                            + corsoDaPrenotare.getNome()
                            + " effettuata con successo.",
                    "Successo",
                    JOptionPane.INFORMATION_MESSAGE
            );


            caricaDati();

        } catch (CorsoAlCompletoException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Info: Lista d'Attesa",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (PrenotazioneGiaEffettuataException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Impossibile Prenotare",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Errore di sistema: " + ex.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void annullaCorsoCliccato() {
        int rigaSelezionata = tabellaCorsi.getSelectedRow();

        if (rigaSelezionata == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleziona un corso dalla tabella prima di cliccare su Annulla.",
                    "Attenzione",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Corso corsoDaAnnullare = corsiAttuali.get(rigaSelezionata);

       

        try {
            gestorePrenotazioni.annullaPrenotazione(clienteLoggato, corsoDaAnnullare);

            JOptionPane.showMessageDialog(
                    this,
                    "Prenotazione al corso di " + corsoDaAnnullare.getNome() + " annullata con successo.",
                    "Successo",
                    JOptionPane.INFORMATION_MESSAGE
            );


            caricaDati();

        } catch (PrenotazioneInesistenteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Impossibile Annullare",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Errore di sistema: " + ex.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
