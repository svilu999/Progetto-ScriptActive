package it.unipv.posfw.view;

import it.unipv.posfw.controller.GestorePersonale;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.exceptions.SostitutoNonValidoException;
import it.unipv.posfw.exceptions.TrainerGiaAssuntoException;
import it.unipv.posfw.exceptions.TrainerNonLicenziabileException;
import it.unipv.posfw.strategy.RetribuzioneFissa;
import it.unipv.posfw.strategy.RetribuzioneProvvigione;
import it.unipv.posfw.strategy.StrategiaRetribuzione;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * View Swing per la Gestione dei Contratti del Personale.
 *
 * Funzioni principali:
 * - visualizzare i Personal Trainer;
 * - assumere un nuovo Personal Trainer;
 * - licenziare un PT con sostituto compatibile;
 * - licenziare direttamente un PT senza corsi futuri;
 * - filtrare i sostituti per specializzazione;
 * - calcolare le retribuzioni mensili.
 *
 * La view NON contiene query SQL.
 * La view comunica solo con GestorePersonale.
 */
public class GestionePersonaleView extends JFrame {

    private GestorePersonale gestorePersonale;

    private JTable tabellaPT;
    private DefaultTableModel modelloTabella;

    private JTextField txtNome;
    private JTextField txtCognome;
    private JTextField txtEmail;
    private JTextField txtIdTrainer;
    private JTextField txtSpecializzazione;
    private JTextField txtImportoRetribuzione;

    private JComboBox<String> comboTipoRetribuzione;

    private JTextField txtIdDaLicenziare;
    private JComboBox<String> comboSostituto;

    private JButton btnAggiorna;
    private JButton btnAssumi;
    private JButton btnLicenziaConSostituto;
    private JButton btnLicenziaSenzaSostituto;
    private JButton btnCalcolaRetribuzioni;

    private JLabel lblStato;

    /*
     * Colori e font della view.
     * Sono colori chiari e semplici, compatibili anche con Windows Look & Feel.
     */
    private final Color COLORE_SFONDO = new Color(245, 245, 247);
    private final Color COLORE_CARD = Color.WHITE;
    private final Color COLORE_TESTO = new Color(28, 28, 30);
    private final Color COLORE_TESTO_SECONDARIO = new Color(90, 90, 95);
    private final Color COLORE_BORDO = new Color(215, 215, 220);
    private final Color COLORE_HEADER_TABELLA = new Color(248, 248, 250);
    private final Color COLORE_RIGA_ALTERNATA = new Color(250, 250, 252);
    private final Color COLORE_SELEZIONE = new Color(210, 232, 255);

    private final Font FONT_TITOLO = new Font("SansSerif", Font.BOLD, 22);
    private final Font FONT_SOTTOTITOLO = new Font("SansSerif", Font.PLAIN, 13);
    private final Font FONT_SEZIONE = new Font("SansSerif", Font.BOLD, 14);
    private final Font FONT_BASE = new Font("SansSerif", Font.PLAIN, 13);
    private final Font FONT_BOTTONE = new Font("SansSerif", Font.BOLD, 13);

    public GestionePersonaleView() {
        /*
         * Uso il Look & Feel del sistema operativo.
         */
        impostaLookAndFeel();

        this.gestorePersonale = GestorePersonale.getInstance();

        inizializzaFinestra();
        inizializzaComponenti();
        caricaPersonalTrainer();
    }

    private void impostaLookAndFeel() {
        try {
            System.setProperty("apple.awt.application.appearance", "system");
            System.setProperty("apple.laf.useScreenMenuBar", "true");

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {
            System.out.println("Look & Feel di sistema non disponibile. Uso quello standard.");
        }
    }

    private void inizializzaFinestra() {
        setTitle("ScriptActive - Gestione Personale");
        setSize(1120, 760);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(COLORE_SFONDO);
    }

    private void inizializzaComponenti() {
        add(creaHeader(), BorderLayout.NORTH);

        /*
         * Uso un contenitore centrale con GridBagLayout:
         * - la tabella prende più spazio verticale;
         * - i pannelli sotto restano più compatti.
         */
        JPanel contenuto = new JPanel(new GridBagLayout());
        contenuto.setBackground(COLORE_SFONDO);
        contenuto.setBorder(new EmptyBorder(0, 24, 22, 24));

        GridBagConstraints gbcTabella = new GridBagConstraints();
        gbcTabella.gridx = 0;
        gbcTabella.gridy = 0;
        gbcTabella.fill = GridBagConstraints.BOTH;
        gbcTabella.weightx = 1.0;
        gbcTabella.weighty = 1.0;
        gbcTabella.insets = new Insets(0, 0, 14, 0);

        contenuto.add(creaCardTabella(), gbcTabella);

        GridBagConstraints gbcOperazioni = new GridBagConstraints();
        gbcOperazioni.gridx = 0;
        gbcOperazioni.gridy = 1;
        gbcOperazioni.fill = GridBagConstraints.HORIZONTAL;
        gbcOperazioni.weightx = 1.0;
        gbcOperazioni.weighty = 0.0;
        gbcOperazioni.insets = new Insets(0, 0, 10, 0);

        contenuto.add(creaPannelloOperazioni(), gbcOperazioni);

        GridBagConstraints gbcBarra = new GridBagConstraints();
        gbcBarra.gridx = 0;
        gbcBarra.gridy = 2;
        gbcBarra.fill = GridBagConstraints.HORIZONTAL;
        gbcBarra.weightx = 1.0;
        gbcBarra.weighty = 0.0;

        contenuto.add(creaBarraAzioni(), gbcBarra);

        add(contenuto, BorderLayout.CENTER);
    }

    private JPanel creaHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLORE_SFONDO);
        header.setBorder(new EmptyBorder(22, 24, 12, 24));

        JLabel titolo = new JLabel("Gestione Personale");
        titolo.setFont(FONT_TITOLO);
        titolo.setForeground(COLORE_TESTO);

        JLabel sottotitolo = new JLabel(
                "Assunzione, licenziamento diretto o con sostituto compatibile e calcolo retribuzioni dei Personal Trainer."
        );
        sottotitolo.setFont(FONT_SOTTOTITOLO);
        sottotitolo.setForeground(COLORE_TESTO_SECONDARIO);

        JPanel testi = new JPanel(new BorderLayout(0, 4));
        testi.setBackground(COLORE_SFONDO);
        testi.add(titolo, BorderLayout.NORTH);
        testi.add(sottotitolo, BorderLayout.CENTER);

        header.add(testi, BorderLayout.CENTER);
        return header;
    }

    private JPanel creaCardTabella() {
        JPanel card = creaCard("Elenco Personal Trainer");

        creaTabella();

        JScrollPane scrollPane = new JScrollPane(tabellaPT);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLORE_BORDO));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(900, 260));

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void creaTabella() {
        /*
         * Definisco le colonne della tabella.
         * Ogni riga rappresenta un Personal Trainer presente nel database.
         */
        String[] colonne = {
                "ID Trainer",
                "Nome Completo",
                "Email",
                "Specializzazione",
                "Stato Contratto",
                "Attivo"
        };

        /*
         * Creo il modello della tabella.
         * Sovrascrivo isCellEditable per impedire la modifica diretta delle celle.
         * La tabella serve solo per visualizzare e selezionare i PT.
         */
        modelloTabella = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabellaPT = new JTable(modelloTabella);
        configuraTabella();

        /*
         * Listener sulla selezione della tabella.
         *
         * Quando il Direttore clicca su una riga:
         * 1. leggo l'ID del PT selezionato;
         * 2. leggo la sua specializzazione;
         * 3. inserisco automaticamente l'ID nel campo "PT da licenziare";
         * 4. carico nella tendina solo i sostituti compatibili.
         */
        tabellaPT.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int rigaSelezionata = tabellaPT.getSelectedRow();

                if (rigaSelezionata >= 0 && txtIdDaLicenziare != null) {

                    /*
                     * Converto l'indice della riga visualizzata nell'indice reale del modello.
                     * È utile se in futuro si aggiungono ordinamento o filtri alla tabella.
                     */
                    int rigaModello = tabellaPT.convertRowIndexToModel(rigaSelezionata);

                    /*
                     * Colonna 0 = ID Trainer.
                     */
                    String idTrainer = modelloTabella
                            .getValueAt(rigaModello, 0)
                            .toString();

                    /*
                     * Colonna 3 = Specializzazione.
                     */
                    String specializzazione = modelloTabella
                            .getValueAt(rigaModello, 3)
                            .toString();

                    /*
                     * Compilo automaticamente il campo del PT da licenziare.
                     * In questo modo si evitano errori di digitazione.
                     */
                    txtIdDaLicenziare.setText(idTrainer);

                    /*
                     * Aggiorno la tendina dei sostituti mostrando solo:
                     * - PT attivi;
                     * - PT diversi da quello selezionato;
                     * - PT con la stessa specializzazione.
                     */
                    caricaSostitutiCompatibili(idTrainer, specializzazione);
                    aggiornaStato("PT selezionato: " + idTrainer + " - " + specializzazione);
                }
            }
        });
    }

    private void configuraTabella() {
        tabellaPT.setFont(FONT_BASE);
        tabellaPT.setForeground(COLORE_TESTO);
        tabellaPT.setRowHeight(32);
        tabellaPT.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaPT.setShowGrid(false);
        tabellaPT.setIntercellSpacing(new Dimension(0, 0));
        tabellaPT.setFillsViewportHeight(true);
        tabellaPT.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = tabellaPT.getTableHeader();
        header.setFont(FONT_SEZIONE);
        header.setForeground(COLORE_TESTO);
        header.setBackground(COLORE_HEADER_TABELLA);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));

        /*
         * Renderer personalizzato:
         * - righe alternate;
         * - selezione più morbida;
         * - padding interno alle celle.
         */
        tabellaPT.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                Component componente = super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column
                );

                if (isSelected) {
                    componente.setBackground(COLORE_SELEZIONE);
                    componente.setForeground(COLORE_TESTO);
                } else {
                    componente.setBackground(row % 2 == 0 ? Color.WHITE : COLORE_RIGA_ALTERNATA);
                    componente.setForeground(COLORE_TESTO);
                }

                setBorder(new EmptyBorder(0, 10, 0, 10));
                return componente;
            }
        });

        tabellaPT.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabellaPT.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabellaPT.getColumnModel().getColumn(2).setPreferredWidth(260);
        tabellaPT.getColumnModel().getColumn(3).setPreferredWidth(160);
        tabellaPT.getColumnModel().getColumn(4).setPreferredWidth(140);
        tabellaPT.getColumnModel().getColumn(5).setPreferredWidth(80);
    }

    private JPanel creaPannelloOperazioni() {
        JPanel pannelloContenitore = new JPanel(new GridBagLayout());
        pannelloContenitore.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JPanel cardAssunzione = creaCard("Assunzione Personal Trainer");
        cardAssunzione.add(creaFormAssunzione(), BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        pannelloContenitore.add(cardAssunzione, gbc);

        JPanel cardLicenziamento = creaCard("Licenziamento e sostituzione");
        cardLicenziamento.add(creaFormLicenziamento(), BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 12, 0, 0);
        pannelloContenitore.add(cardLicenziamento, gbc);

        return pannelloContenitore;
    }

    private JPanel creaFormAssunzione() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        txtNome = creaCampoTesto();
        txtCognome = creaCampoTesto();
        txtEmail = creaCampoTesto();
        txtIdTrainer = creaCampoTesto();
        txtIdTrainer.setText("Generato automaticamente");
        txtIdTrainer.setEditable(false);
        txtIdTrainer.setBackground(new Color(238, 238, 240));

        txtSpecializzazione = creaCampoTesto();
        txtImportoRetribuzione = creaCampoTesto();

        comboTipoRetribuzione = creaComboBox();
        comboTipoRetribuzione.addItem("FISSA_MENSILE");
        comboTipoRetribuzione.addItem("A_LEZIONE");
        comboTipoRetribuzione.setSelectedItem("FISSA_MENSILE");

        aggiungiRigaForm(form, 0, "Nome", txtNome);
        aggiungiRigaForm(form, 1, "Cognome", txtCognome);
        aggiungiRigaForm(form, 2, "Email", txtEmail);
        aggiungiRigaForm(form, 3, "ID Trainer", txtIdTrainer);
        aggiungiRigaForm(form, 4, "Specializzazione", txtSpecializzazione);
        aggiungiRigaForm(form, 5, "Tipo retribuzione", comboTipoRetribuzione);
        aggiungiRigaForm(form, 6, "Importo retribuzione", txtImportoRetribuzione);

        btnAssumi = creaBottone("Assumi PT");
        btnAssumi.addActionListener(e -> assumiPersonalTrainer());

        GridBagConstraints gbcBottone = new GridBagConstraints();
        gbcBottone.gridx = 1;
        gbcBottone.gridy = 7;
        gbcBottone.fill = GridBagConstraints.HORIZONTAL;
        gbcBottone.insets = new Insets(10, 8, 0, 0);
        form.add(btnAssumi, gbcBottone);

        return form;
    }

    private JPanel creaFormLicenziamento() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        txtIdDaLicenziare = creaCampoTesto();
        txtIdDaLicenziare.setEditable(false);
        txtIdDaLicenziare.setBackground(new Color(238, 238, 240));

        comboSostituto = creaComboBox();
        comboSostituto.addItem("Seleziona sostituto...");

        aggiungiRigaForm(form, 0, "PT da licenziare", txtIdDaLicenziare);
        aggiungiRigaForm(form, 1, "Sostituto compatibile", comboSostituto);

        JLabel nota = new JLabel("<html>Seleziona un PT dalla tabella: la tendina mostra solo sostituti attivi con la stessa specializzazione.</html>");
        nota.setFont(FONT_SOTTOTITOLO);
        nota.setForeground(COLORE_TESTO_SECONDARIO);

        GridBagConstraints gbcNota = new GridBagConstraints();
        gbcNota.gridx = 0;
        gbcNota.gridy = 2;
        gbcNota.gridwidth = 2;
        gbcNota.fill = GridBagConstraints.HORIZONTAL;
        gbcNota.insets = new Insets(8, 0, 6, 0);
        form.add(nota, gbcNota);

        btnLicenziaConSostituto = creaBottone("Licenzia con sostituto");
        btnLicenziaConSostituto.addActionListener(e -> licenziaPersonalTrainerConSostituto());

        GridBagConstraints gbcBottoneConSostituto = new GridBagConstraints();
        gbcBottoneConSostituto.gridx = 1;
        gbcBottoneConSostituto.gridy = 3;
        gbcBottoneConSostituto.fill = GridBagConstraints.HORIZONTAL;
        gbcBottoneConSostituto.insets = new Insets(10, 8, 0, 0);
        form.add(btnLicenziaConSostituto, gbcBottoneConSostituto);

        btnLicenziaSenzaSostituto = creaBottone("Licenzia senza sostituto");
        btnLicenziaSenzaSostituto.addActionListener(e -> licenziaPersonalTrainerSenzaSostituto());

        GridBagConstraints gbcBottoneSenzaSostituto = new GridBagConstraints();
        gbcBottoneSenzaSostituto.gridx = 1;
        gbcBottoneSenzaSostituto.gridy = 4;
        gbcBottoneSenzaSostituto.fill = GridBagConstraints.HORIZONTAL;
        gbcBottoneSenzaSostituto.insets = new Insets(8, 8, 0, 0);
        form.add(btnLicenziaSenzaSostituto, gbcBottoneSenzaSostituto);

        return form;
    }

    private JPanel creaBarraAzioni() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);

        lblStato = new JLabel("Pronto.");
        lblStato.setFont(FONT_SOTTOTITOLO);
        lblStato.setForeground(COLORE_TESTO_SECONDARIO);

        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pulsanti.setOpaque(false);

        btnAggiorna = creaBottone("Aggiorna elenco");
        btnAggiorna.addActionListener(e -> caricaPersonalTrainer());

        btnCalcolaRetribuzioni = creaBottone("Calcola retribuzioni");
        btnCalcolaRetribuzioni.addActionListener(e -> calcolaRetribuzioniMensili());

        pulsanti.add(btnAggiorna);
        pulsanti.add(btnCalcolaRetribuzioni);

        barra.add(lblStato, BorderLayout.WEST);
        barra.add(pulsanti, BorderLayout.EAST);

        return barra;
    }

    private JPanel creaCard(String titoloSezione) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(COLORE_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLORE_BORDO),
                new EmptyBorder(16, 18, 18, 18)
        ));

        JLabel titolo = new JLabel(titoloSezione);
        titolo.setFont(FONT_SEZIONE);
        titolo.setForeground(COLORE_TESTO);

        card.add(titolo, BorderLayout.NORTH);
        return card;
    }

    private JTextField creaCampoTesto() {
        JTextField campo = new JTextField();
        campo.setFont(FONT_BASE);
        campo.setForeground(COLORE_TESTO);
        campo.setPreferredSize(new Dimension(250, 32));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLORE_BORDO),
                new EmptyBorder(5, 9, 5, 9)
        ));
        return campo;
    }

    private JComboBox<String> creaComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(FONT_BASE);
        combo.setForeground(COLORE_TESTO);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(250, 32));
        return combo;
    }

    private JButton creaBottone(String testo) {
        /*
         * Uso lo stile nativo del sistema per evitare problemi di testo invisibile
         * su Windows Look & Feel.
         */
        JButton bottone = new JButton(testo);
        bottone.setFont(FONT_BOTTONE);
        bottone.setFocusPainted(false);
        bottone.setPreferredSize(new Dimension(170, 34));
        return bottone;
    }

    private void aggiungiRigaForm(JPanel form, int riga, String testoLabel, Component componenteInput) {
        JLabel label = new JLabel(testoLabel);
        label.setFont(FONT_BASE);
        label.setForeground(COLORE_TESTO);
        label.setHorizontalAlignment(SwingConstants.LEFT);

        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = riga;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 0, 5, 8);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = 1;
        gbcInput.gridy = riga;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.weightx = 1.0;
        gbcInput.insets = new Insets(5, 8, 5, 0);

        form.add(label, gbcLabel);
        form.add(componenteInput, gbcInput);
    }

    private void caricaSostitutiCompatibili(String idDaLicenziare, String specializzazioneRichiesta) {
        /*
         * Viene pulita la tendina ogni volta che viene selezionato un nuovo PT.
         * In questo modo non rimangono sostituti caricati da selezioni precedenti.
         */
        comboSostituto.removeAllItems();

        /*
         * Prima voce standard della tendina.
         * Serve per evitare che venga selezionato automaticamente un sostituto.
         */
        comboSostituto.addItem("Seleziona sostituto...");

        try {
            /*
             * Recupero tutti i Personal Trainer dal controller.
             * Il controller va a coordinare il DAO MySQL per prendere i dati dal database.
             */
            List<PersonalTrainer> elenco = gestorePersonale.getElencoPersonalTrainer();

            /*
             * Scorro tutti i PT e tengo solo quelli compatibili.
             */
            for (PersonalTrainer pt : elenco) {
                /*
                 * Un PT non può sostituire sé stesso.
                 */
                boolean stessoTrainer = pt.getIdTrainer().equalsIgnoreCase(idDaLicenziare);

                /*
                 * Il sostituto deve essere attivo.
                 */
                boolean attivo = pt.isAttivo();

                /*
                 * Il sostituto deve avere la stessa specializzazione del PT da licenziare.
                 */
                boolean stessaSpecializzazione = pt.getSpecializzazione() != null
                        && pt.getSpecializzazione().equalsIgnoreCase(specializzazioneRichiesta);

                /*
                 * Aggiungo alla tendina solo i PT realmente compatibili filtrandoli per specializzazione.
                 */
                if (!stessoTrainer && attivo && stessaSpecializzazione) {
                    comboSostituto.addItem(
                            pt.getIdTrainer()
                                    + " - "
                                    + pt.getNomeCompleto()
                                    + " - "
                                    + pt.getSpecializzazione()
                    );
                }
            }

            /*
             * Se la tendina contiene solo la voce iniziale, significa che non esistono sostituti compatibili.
             */
            if (comboSostituto.getItemCount() == 1) {
                comboSostituto.addItem("Nessun sostituto compatibile");
            }

        } catch (Exception e) {
            mostraErrore("Errore durante il caricamento dei sostituti compatibili:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void caricaPersonalTrainer() {
        try {
            modelloTabella.setRowCount(0);

            List<PersonalTrainer> listaPT = gestorePersonale.getElencoPersonalTrainer();

            for (PersonalTrainer pt : listaPT) {
                Object[] riga = {
                        pt.getIdTrainer(),
                        pt.getNomeCompleto(),
                        pt.getEmail(),
                        pt.getSpecializzazione(),
                        pt.getStatoContratto(),
                        pt.isAttivo() ? "Sì" : "No"
                };

                modelloTabella.addRow(riga);
            }

            aggiornaStato("Elenco aggiornato. PT caricati: " + listaPT.size());

        } catch (Exception e) {
            mostraErrore("Errore durante il caricamento dei Personal Trainer:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void assumiPersonalTrainer() {
        try {
            String nome = txtNome.getText().trim();
            String cognome = txtCognome.getText().trim();
            String email = txtEmail.getText().trim();
            String idTrainer = "AUTO";
            String specializzazione = txtSpecializzazione.getText().trim();
            String tipoRetribuzione = (String) comboTipoRetribuzione.getSelectedItem();
            String importoTesto = txtImportoRetribuzione.getText().trim();

            if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty()
                    || specializzazione.isEmpty()
                    || importoTesto.isEmpty()) {

                mostraErrore("Compila tutti i campi prima di assumere il PT.");
                return;
            }

            double importo = Double.parseDouble(importoTesto);

            if (importo < 0) {
                mostraErrore("L'importo della retribuzione non può essere negativo.");
                return;
            }

            StrategiaRetribuzione strategia;

            if ("FISSA_MENSILE".equals(tipoRetribuzione)) {
                strategia = new RetribuzioneFissa(importo);
            } else {
                strategia = new RetribuzioneProvvigione(importo);
            }

            gestorePersonale.assumiPT(
                    nome,
                    cognome,
                    email,
                    idTrainer,
                    specializzazione,
                    strategia
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Personal Trainer assunto correttamente.",
                    "Operazione completata",
                    JOptionPane.INFORMATION_MESSAGE
            );

            pulisciCampiAssunzione();
            caricaPersonalTrainer();
            aggiornaStato("PT assunto correttamente: " + nome + " " + cognome);

        } catch (NumberFormatException e) {
            mostraErrore("L'importo della retribuzione deve essere un numero. Esempio: 1400 oppure 25");

        } catch (TrainerGiaAssuntoException e) {
            mostraErrore(e.getMessage());

        } catch (Exception e) {
            mostraErrore("Errore durante l'assunzione del Personal Trainer:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void licenziaPersonalTrainerConSostituto() {
        try {
            String idDaLicenziare = txtIdDaLicenziare.getText().trim();

            /*
             * Leggo dalla tendina il sostituto selezionato.
             * La voce ha formato:
             * ID - Nome Cognome - Specializzazione
             */
            String selezioneSostituto = (String) comboSostituto.getSelectedItem();

            /*
             * Controllo che l'utente abbia selezionato un sostituto valido.
             */
            if (selezioneSostituto == null
                    || selezioneSostituto.equals("Seleziona sostituto...")
                    || selezioneSostituto.equals("Nessun sostituto compatibile")) {

                mostraErrore("Seleziona un PT sostituto compatibile.");
                return;
            }

            /*
             * Estraggo solo l'ID del sostituto dalla stringa della tendina.
             */
            String idSostituto = selezioneSostituto.split(" - ")[0].trim();

            if (idDaLicenziare.isEmpty() || idSostituto.isEmpty()) {
                mostraErrore("Seleziona il PT da licenziare e un sostituto compatibile.");
                return;
            }

            int conferma = JOptionPane.showConfirmDialog(
                    this,
                    "Confermi il licenziamento del PT " + idDaLicenziare
                            + " con sostituto " + idSostituto + "?",
                    "Conferma licenziamento",
                    JOptionPane.YES_NO_OPTION
            );

            if (conferma != JOptionPane.YES_OPTION) {
                return;
            }

            gestorePersonale.licenziaPT(idDaLicenziare, idSostituto);

            JOptionPane.showMessageDialog(
                    this,
                    "Personal Trainer licenziato correttamente.",
                    "Operazione completata",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetCampiLicenziamento();
            caricaPersonalTrainer();
            aggiornaStato("PT licenziato: " + idDaLicenziare + ". Sostituto: " + idSostituto);

        } catch (SostitutoNonValidoException | TrainerNonLicenziabileException e) {
            mostraErrore(e.getMessage());

        } catch (Exception e) {
            mostraErrore("Errore durante il licenziamento del Personal Trainer:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void licenziaPersonalTrainerSenzaSostituto() {
        try {
            String idDaLicenziare = txtIdDaLicenziare.getText().trim();

            if (idDaLicenziare.isEmpty()) {
                mostraErrore("Seleziona dalla tabella il PT da licenziare.");
                return;
            }

            int conferma = JOptionPane.showConfirmDialog(
                    this,
                    "Confermi il licenziamento del PT " + idDaLicenziare
                            + " senza sostituto?\n"
                            + "L'operazione riuscirà solo se il PT non ha corsi attivi o futuri.",
                    "Conferma licenziamento senza sostituto",
                    JOptionPane.YES_NO_OPTION
            );

            if (conferma != JOptionPane.YES_OPTION) {
                return;
            }

            gestorePersonale.licenziaPT(idDaLicenziare);

            JOptionPane.showMessageDialog(
                    this,
                    "Personal Trainer licenziato correttamente senza sostituto.",
                    "Operazione completata",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetCampiLicenziamento();
            caricaPersonalTrainer();
            aggiornaStato("PT licenziato senza sostituto: " + idDaLicenziare);

        } catch (SostitutoNonValidoException | TrainerNonLicenziabileException e) {
            mostraErrore(e.getMessage());

        } catch (Exception e) {
            mostraErrore("Errore durante il licenziamento senza sostituto:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void calcolaRetribuzioniMensili() {
        try {
            double totale = gestorePersonale.calcolaTotaleStipendiMensili();

            JOptionPane.showMessageDialog(
                    this,
                    String.format("Totale retribuzioni mensili PT attivi: € %.2f", totale),
                    "Calcolo retribuzioni",
                    JOptionPane.INFORMATION_MESSAGE
            );

            aggiornaStato(String.format("Totale retribuzioni mensili: € %.2f", totale));

        } catch (Exception e) {
            mostraErrore("Errore durante il calcolo delle retribuzioni:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void pulisciCampiAssunzione() {
        txtNome.setText("");
        txtCognome.setText("");
        txtEmail.setText("");
        txtIdTrainer.setText("Generato automaticamente");
        txtSpecializzazione.setText("");
        txtImportoRetribuzione.setText("");
        comboTipoRetribuzione.setSelectedIndex(0);
    }

    private void resetCampiLicenziamento() {
        txtIdDaLicenziare.setText("");
        comboSostituto.removeAllItems();
        comboSostituto.addItem("Seleziona sostituto...");
    }

    private void mostraErrore(String messaggio) {
        aggiornaStato("Errore: operazione non completata.");

        JOptionPane.showMessageDialog(
                this,
                messaggio,
                "Errore",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void aggiornaStato(String messaggio) {
        if (lblStato != null) {
            lblStato.setText(messaggio);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GestionePersonaleView view = new GestionePersonaleView();
            view.setVisible(true);
        });
    }
}