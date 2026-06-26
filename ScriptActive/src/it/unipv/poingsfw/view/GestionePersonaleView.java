package it.unipv.poingsfw.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


/**
 * View Swing per la gestione grafica dei Personal Trainer.
 *
 * La classe costruisce la schermata e inizializza i componenti grafici usati
 * dal caso d'uso di gestione del personale.
 */
public class GestionePersonaleView extends JFrame {

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

    /**
     * Crea la finestra di gestione del personale e inizializza tutti i componenti grafici.
     */
    public GestionePersonaleView() {
        /*
         * Uso il Look & Feel del sistema operativo.
         */
        impostaLookAndFeel();
        inizializzaFinestra();
        inizializzaComponenti();
        
    }

    /**
     * Imposta il Look and Feel del sistema operativo, se disponibile.
     */
    private void impostaLookAndFeel() {
        try {
            System.setProperty("apple.awt.application.appearance", "system");
            System.setProperty("apple.laf.useScreenMenuBar", "true");

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {
            System.out.println("Look & Feel di sistema non disponibile. Uso quello standard.");
        }
    }

    /**
     * Imposta le proprietà principali della finestra.
     */
    private void inizializzaFinestra() {
        setTitle("ScriptActive - Gestione Personale");
        setSize(1120, 760);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(COLORE_SFONDO);
    }

    /**
     * Inizializza e dispone i componenti principali della schermata.
     */
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

    /**
     * Crea l'intestazione superiore della finestra.
     *
     * @return pannello contenente titolo e sottotitolo
     */
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

    /**
     * Crea il pannello che contiene la tabella dei PersonalTrainer.
     *
     * @return pannello con tabella e scroll
     */
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

    /**
     * Crea la tabella usata per mostrare graficamente i Personal Trainer.
     */
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
    }

    /**
     * Configura l'aspetto grafico della tabella.
     */
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

    /**
     * Crea il pannello che contiene le operazioni di assunzione e licenziamento.
     *
     * @return pannello delle operazioni principali
     */
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

    /**
     * Crea il form grafico per l'assunzione di un nuovo PersonalTrainer.
     *
     * @return pannello del form di assunzione
     */
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
        

        GridBagConstraints gbcBottone = new GridBagConstraints();
        gbcBottone.gridx = 1;
        gbcBottone.gridy = 7;
        gbcBottone.fill = GridBagConstraints.HORIZONTAL;
        gbcBottone.insets = new Insets(10, 8, 0, 0);
        form.add(btnAssumi, gbcBottone);

        return form;
    }

    /**
     * Crea il form grafico per il licenziamento con o senza sostituto.
     *
     * @return pannello del form di licenziamento
     */
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
        

        GridBagConstraints gbcBottoneConSostituto = new GridBagConstraints();
        gbcBottoneConSostituto.gridx = 1;
        gbcBottoneConSostituto.gridy = 3;
        gbcBottoneConSostituto.fill = GridBagConstraints.HORIZONTAL;
        gbcBottoneConSostituto.insets = new Insets(10, 8, 0, 0);
        form.add(btnLicenziaConSostituto, gbcBottoneConSostituto);

        btnLicenziaSenzaSostituto = creaBottone("Licenzia senza sostituto");
        

        GridBagConstraints gbcBottoneSenzaSostituto = new GridBagConstraints();
        gbcBottoneSenzaSostituto.gridx = 1;
        gbcBottoneSenzaSostituto.gridy = 4;
        gbcBottoneSenzaSostituto.fill = GridBagConstraints.HORIZONTAL;
        gbcBottoneSenzaSostituto.insets = new Insets(8, 8, 0, 0);
        form.add(btnLicenziaSenzaSostituto, gbcBottoneSenzaSostituto);

        return form;
    }

    /**
     * Crea la barra inferiore con stato della schermata e pulsanti di supporto.
     *
     * @return pannello della barra delle azioni
     */
    private JPanel creaBarraAzioni() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);

        lblStato = new JLabel("Pronto.");
        lblStato.setFont(FONT_SOTTOTITOLO);
        lblStato.setForeground(COLORE_TESTO_SECONDARIO);

        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pulsanti.setOpaque(false);

        btnAggiorna = creaBottone("Aggiorna elenco");
        

        btnCalcolaRetribuzioni = creaBottone("Calcola retribuzioni");
        

        pulsanti.add(btnAggiorna);
        pulsanti.add(btnCalcolaRetribuzioni);

        barra.add(lblStato, BorderLayout.WEST);
        barra.add(pulsanti, BorderLayout.EAST);

        return barra;
    }

    /**
     * Crea un pannello con bordo e titolo, usato come contenitore grafico.
     *
     * @param titoloSezione titolo da mostrare nel pannello
     * @return pannello configurato come card
     */
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

    /**
     * Crea un campo di testo con lo stile usato nella finestra.
     *
     * @return campo di testo configurato
     */
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

    /**
     * Crea una combo box con lo stile usato nella finestra.
     *
     * @return combo box configurata
     */
    private JComboBox<String> creaComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(FONT_BASE);
        combo.setForeground(COLORE_TESTO);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(250, 32));
        return combo;
    }

    /**
     * Crea un bottone con lo stile usato nella finestra.
     *
     * @param testo testo da mostrare sul bottone
     * @return bottone configurato
     */
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

    /**
     * Aggiunge una riga etichetta-campo a un form grafico.
     *
     * @param form pannello a cui aggiungere la riga
     * @param riga indice della riga nel GridBagLayout
     * @param testoLabel testo della label
     * @param componenteInput componente grafico da inserire accanto alla label
     */
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
    
    /**
     * Restituisce il pulsante per l'assunzione del Personal Trainer.
     *
     * @return pulsante di assunzione
     */
    public JButton getBtnAssumi() {
        return btnAssumi;
    }

    /**
     * Restituisce il pulsante per il licenziamento con sostituto.
     *
     * @return pulsante di licenziamento con sostituto
     */
    public JButton getBtnLicenziaConSostituto() {
        return btnLicenziaConSostituto;
    }

    /**
     * Restituisce il pulsante per il licenziamento senza sostituto.
     *
     * @return pulsante di licenziamento senza sostituto
     */
    public JButton getBtnLicenziaSenzaSostituto() {
        return btnLicenziaSenzaSostituto;
    }

    /**
     * Restituisce il pulsante per aggiornare l'elenco dei Personal Trainer.
     *
     * @return pulsante di aggiornamento
     */
    public JButton getBtnAggiorna() {
        return btnAggiorna;
    }

    /**
     * Restituisce il pulsante per il calcolo delle retribuzioni.
     *
     * @return pulsante di calcolo retribuzioni
     */
    public JButton getBtnCalcolaRetribuzioni() {
        return btnCalcolaRetribuzioni;
    }

    /**
     * Restituisce la tabella dei Personal Trainer.
     *
     * @return tabella dei Personal Trainer
     */
    public JTable getTabellaPT() {
        return tabellaPT;
    }

    /**
     * Restituisce il modello della tabella dei Personal Trainer.
     *
     * @return modello della tabella
     */
    public DefaultTableModel getModelloTabella() {
        return modelloTabella;
    }

    /**
     * Restituisce il campo del nome.
     *
     * @return campo del nome
     */
    public JTextField getTxtNome() {
        return txtNome;
    }

    /**
     * Restituisce il campo del cognome.
     *
     * @return campo del cognome
     */
    public JTextField getTxtCognome() {
        return txtCognome;
    }

    /**
     * Restituisce il campo dell'email.
     *
     * @return campo dell'email
     */
    public JTextField getTxtEmail() {
        return txtEmail;
    }

    /**
     * Restituisce il campo dell'identificativo del trainer.
     *
     * @return campo dell'identificativo del trainer
     */
    public JTextField getTxtIdTrainer() {
        return txtIdTrainer;
    }

    /**
     * Restituisce il campo della specializzazione.
     *
     * @return campo della specializzazione
     */
    public JTextField getTxtSpecializzazione() {
        return txtSpecializzazione;
    }

    /**
     * Restituisce il campo dell'importo della retribuzione.
     *
     * @return campo dell'importo della retribuzione
     */
    public JTextField getTxtImportoRetribuzione() {
        return txtImportoRetribuzione;
    }

    /**
     * Restituisce la combo box del tipo di retribuzione.
     *
     * @return combo box del tipo di retribuzione
     */
    public JComboBox<String> getComboTipoRetribuzione() {
        return comboTipoRetribuzione;
    }

    /**
     * Restituisce il campo dell'identificativo del trainer da licenziare.
     *
     * @return campo del trainer da licenziare
     */
    public JTextField getTxtIdDaLicenziare() {
        return txtIdDaLicenziare;
    }

    /**
     * Restituisce la combo box dei sostituti.
     *
     * @return combo box dei sostituti
     */
    public JComboBox<String> getComboSostituto() {
        return comboSostituto;
    }

    /**
     * Restituisce la label di stato della schermata.
     *
     * @return label di stato
     */
    public JLabel getLblStato() {
        return lblStato;
    }
    
    /**
     * Mostra un messaggio informativo all'utente.
     *
     * Il metodo contiene solo logica grafica di presentazione.
     *
     * @param messaggio testo da mostrare
     */
    public void mostraMessaggioInformativo(String messaggio) {
        JOptionPane.showMessageDialog(
                this,
                messaggio,
                "Operazione completata",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Mostra un messaggio di errore all'utente.
     *
     * Il metodo contiene solo logica grafica di presentazione.
     *
     * @param messaggio testo dell'errore da mostrare
     */
    public void mostraMessaggioErrore(String messaggio) {
        JOptionPane.showMessageDialog(
                this,
                messaggio,
                "Errore",
                JOptionPane.ERROR_MESSAGE
        );
    }
}