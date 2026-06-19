package it.unipv.posfw.view;
/**
 * Classe Boundary deputata alla gestione dell'interfaccia utente per il palinsesto corsi.
 * 
 * In conformità con lo stile architetturale Model-View-Controller (MVC), questa classe ricopre 
 * il ruolo di View, incaricata della presentazione dei dati e della cattura degli eventi utente.
 * Rispetta il "Principio di separazione Modello-Vista": la View non possiede logica applicativa 
 * né accede direttamente alla persistenza, ma delega le operazioni di sistema ai Controller 
 * competenti (GestoreCorsi e GestorePrenotazioni) realizzando il Low Coupling.
 * 
 * La classe implementa l'interfaccia Observer per supportare il meccanismo di "Push from below", 
 * permettendo al Modello di Dominio di notificare i cambiamenti di stato (es. cancellazione 
 * corsi in UC3) senza creare accoppiamento diretto verso lo strato di presentazione.
 * 
 * @author Lorenzo
 * @version 2.0
 * @see Observer
 * @see GestoreCorsi
 */
import javax.swing.*;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import it.unipv.posfw.controller.GestoreCorsi;
import it.unipv.posfw.controller.GestorePrenotazioni;
import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.database.CorsoDAOMySQL;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.exceptions.CorsoAlCompletoException;
import it.unipv.posfw.exceptions.PrenotazioneGiaEffettuataException;
import it.unipv.posfw.exceptions.PrenotazioneInesistenteException;

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
    private Cliente clienteLoggato;

    public PalinsestoCorsiView() {
        setTitle("ScriptActive - Palinsesto Completo");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        btnIndietro = new JButton("Torna alla Dashboard");

        pnlBottoni.add(btnAggiorna);
        pnlBottoni.add(btnPrenota);
        pnlBottoni.add(btnAnnulla);
        pnlBottoni.add(btnIndietro);

        add(pnlBottoni, BorderLayout.SOUTH);

        btnAggiorna.addActionListener(e -> caricaDati());
        btnPrenota.addActionListener(e -> prenotaCorsoCliccato());
        btnAnnulla.addActionListener(e -> annullaCorsoCliccato());
        btnIndietro.addActionListener(e -> dispose());

        caricaDati();
    }

    public void caricaDati() {
        modelloTabella.setRowCount(0);

        corsiAttuali = GestoreCorsi.getInstance().getPalinsestoCorsi(); // MVC PURO

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

        /*
         * Recupera il corso collegato alla riga selezionata.
         */
        Corso corsoDaPrenotare = corsiAttuali.get(rigaSelezionata);

        

        GestorePrenotazioni gestore = new GestorePrenotazioni();

        try {
            gestore.prenotaCorso(clienteLoggato, corsoDaPrenotare);

            JOptionPane.showMessageDialog(
                    this,
                    "Prenotazione al corso di "
                            + corsoDaPrenotare.getNome()
                            + " effettuata con successo.",
                    "Successo",
                    JOptionPane.INFORMATION_MESSAGE
            );

            /*
             * Ricarica il palinsesto per mostrare i posti aggiornati.
             */
            caricaDati();

        } catch (CorsoAlCompletoException ex) {
            // POPUP GIALLO: L'utente è finito in lista d'attesa
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Info: Lista d'Attesa",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (PrenotazioneGiaEffettuataException ex) {
            // POPUP ROSSO: L'utente era già prenotato a questo corso
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Impossibile Prenotare",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception ex) {
            // POPUP ROSSO: Qualsiasi altro errore generico o di database
            JOptionPane.showMessageDialog(
                    this,
                    "Errore di sistema: " + ex.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PalinsestoCorsiView view = new PalinsestoCorsiView();
            view.setVisible(true);
        });
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

       

        GestorePrenotazioni gestore = new GestorePrenotazioni();

        try {
            gestore.annullaPrenotazione(clienteLoggato, corsoDaAnnullare);

            JOptionPane.showMessageDialog(
                    this,
                    "Prenotazione al corso di " + corsoDaAnnullare.getNome() + " annullata con successo.",
                    "Successo",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Ricarica la tabella per mostrare i posti aggiornati
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
    
 // Metodo per iniettare l'utente dopo che la finestra è stata creata
    public void setClienteLoggato(Cliente cliente) {
        this.clienteLoggato = cliente;
    }
}