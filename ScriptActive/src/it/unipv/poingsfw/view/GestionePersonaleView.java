package it.unipv.poingsfw.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * View Swing per la gestione grafica del personale.
 *
 * La classe costruisce e aggiorna esclusivamente i componenti grafici della
 * schermata. Non conosce Controller, Service, DAO, DTO, Strategy o oggetti
 * appartenenti al dominio.
 */
public class GestionePersonaleView extends JFrame {

    private static final String TESTO_ID_AUTOMATICO =
            "Generato automaticamente";

    private static final String TESTO_SELEZIONA_SOSTITUTO =
            "Seleziona sostituto...";

    private static final String TESTO_NESSUN_SOSTITUTO =
            "Nessun sostituto compatibile";

    private static final Color COLORE_SFONDO = new Color(245, 245, 247);
    private static final Color COLORE_CARD = Color.WHITE;
    private static final Color COLORE_TESTO = new Color(28, 28, 30);
    private static final Color COLORE_TESTO_SECONDARIO = new Color(90, 90, 95);
    private static final Color COLORE_BORDO = new Color(215, 215, 220);
    private static final Color COLORE_HEADER_TABELLA = new Color(248, 248, 250);
    private static final Color COLORE_RIGA_ALTERNATA = new Color(250, 250, 252);
    private static final Color COLORE_SELEZIONE = new Color(210, 232, 255);
    private static final Color COLORE_CAMPO_NON_MODIFICABILE =
            new Color(238, 238, 240);

    private static final Font FONT_TITOLO =
            new Font("SansSerif", Font.BOLD, 22);

    private static final Font FONT_SOTTOTITOLO =
            new Font("SansSerif", Font.PLAIN, 13);

    private static final Font FONT_SEZIONE =
            new Font("SansSerif", Font.BOLD, 14);

    private static final Font FONT_BASE =
            new Font("SansSerif", Font.PLAIN, 13);

    private static final Font FONT_BOTTONE =
            new Font("SansSerif", Font.BOLD, 13);

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
     * Numero di vere opzioni presenti nella combo dei sostituti.
     * È uno stato esclusivamente grafico, usato per validare l'indice selezionato.
     */
    private int numeroSostitutiVisualizzati;

    /**
     * Costruisce la finestra e inizializza i componenti grafici.
     */
    public GestionePersonaleView() {
        impostaLookAndFeel();
        inizializzaFinestra();
        inizializzaComponenti();
    }

    /**
     * Imposta il Look and Feel del sistema operativo.
     */
    private void impostaLookAndFeel() {
        try {
            System.setProperty(
                    "apple.awt.application.appearance",
                    "system"
            );

            System.setProperty(
                    "apple.laf.useScreenMenuBar",
                    "true"
            );

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

        } catch (Exception ignored) {
            /*
             * Swing utilizza automaticamente il Look and Feel standard.
             */
        }
    }

    /**
     * Configura la finestra principale.
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
     * Costruisce e dispone i componenti principali.
     */
    private void inizializzaComponenti() {
        add(creaHeader(), BorderLayout.NORTH);

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
     * Crea l'intestazione della schermata.
     *
     * @return pannello dell'intestazione
     */
    private JPanel creaHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLORE_SFONDO);
        header.setBorder(new EmptyBorder(22, 24, 12, 24));

        JLabel titolo = new JLabel("Gestione Personale");
        titolo.setFont(FONT_TITOLO);
        titolo.setForeground(COLORE_TESTO);

        JLabel sottotitolo = new JLabel(
                "Assunzione, licenziamento e calcolo "
                + "delle retribuzioni dei Personal Trainer."
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
     * Crea il pannello che contiene la tabella.
     *
     * @return pannello della tabella
     */
    private JPanel creaCardTabella() {
        JPanel card = creaCard("Elenco Personal Trainer");

        creaTabella();

        JScrollPane scrollPane = new JScrollPane(tabellaPT);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(COLORE_BORDO)
        );

        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(900, 260));

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    /**
     * Crea la tabella grafica.
     */
    private void creaTabella() {
        String[] colonne = {
                "ID Trainer",
                "Nome Completo",
                "Email",
                "Specializzazione",
                "Stato Contratto",
                "Attivo"
        };

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

        tabellaPT.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                        Component componente =
                                super.getTableCellRendererComponent(
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
                            componente.setBackground(
                                    row % 2 == 0
                                            ? Color.WHITE
                                            : COLORE_RIGA_ALTERNATA
                            );

                            componente.setForeground(COLORE_TESTO);
                        }

                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        return componente;
                    }
                }
        );

        tabellaPT.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabellaPT.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabellaPT.getColumnModel().getColumn(2).setPreferredWidth(260);
        tabellaPT.getColumnModel().getColumn(3).setPreferredWidth(160);
        tabellaPT.getColumnModel().getColumn(4).setPreferredWidth(140);
        tabellaPT.getColumnModel().getColumn(5).setPreferredWidth(80);
    }

    /**
     * Crea i pannelli delle operazioni.
     *
     * @return contenitore delle operazioni
     */
    private JPanel creaPannelloOperazioni() {
        JPanel contenitore = new JPanel(new GridBagLayout());
        contenitore.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JPanel cardAssunzione = creaCard("Assunzione Personal Trainer");
        cardAssunzione.add(creaFormAssunzione(), BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        contenitore.add(cardAssunzione, gbc);

        JPanel cardLicenziamento = creaCard("Licenziamento e sostituzione");
        cardLicenziamento.add(creaFormLicenziamento(), BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 12, 0, 0);
        contenitore.add(cardLicenziamento, gbc);

        return contenitore;
    }

    /**
     * Crea il form grafico di assunzione.
     *
     * @return pannello del form
     */
    private JPanel creaFormAssunzione() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        txtNome = creaCampoTesto();
        txtCognome = creaCampoTesto();
        txtEmail = creaCampoTesto();

        txtIdTrainer = creaCampoTesto();
        txtIdTrainer.setText(TESTO_ID_AUTOMATICO);
        txtIdTrainer.setEditable(false);
        txtIdTrainer.setBackground(COLORE_CAMPO_NON_MODIFICABILE);

        txtSpecializzazione = creaCampoTesto();
        txtImportoRetribuzione = creaCampoTesto();
        comboTipoRetribuzione = creaComboBox();

        aggiungiRigaForm(form, 0, "Nome", txtNome);
        aggiungiRigaForm(form, 1, "Cognome", txtCognome);
        aggiungiRigaForm(form, 2, "Email", txtEmail);
        aggiungiRigaForm(form, 3, "ID Trainer", txtIdTrainer);
        aggiungiRigaForm(form, 4, "Specializzazione", txtSpecializzazione);
        aggiungiRigaForm(
                form,
                5,
                "Tipo retribuzione",
                comboTipoRetribuzione
        );

        aggiungiRigaForm(
                form,
                6,
                "Importo retribuzione",
                txtImportoRetribuzione
        );

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
     * Crea il form grafico di licenziamento.
     *
     * @return pannello del form
     */
    private JPanel creaFormLicenziamento() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        txtIdDaLicenziare = creaCampoTesto();
        txtIdDaLicenziare.setEditable(false);
        txtIdDaLicenziare.setBackground(COLORE_CAMPO_NON_MODIFICABILE);

        comboSostituto = creaComboBox();
        comboSostituto.addItem(TESTO_SELEZIONA_SOSTITUTO);

        aggiungiRigaForm(
                form,
                0,
                "PT da licenziare",
                txtIdDaLicenziare
        );

        aggiungiRigaForm(
                form,
                1,
                "Sostituto compatibile",
                comboSostituto
        );

        JLabel nota = new JLabel(
                "<html>Seleziona un PT dalla tabella "
                + "per visualizzare i sostituti disponibili."
                + "</html>"
        );

        nota.setFont(FONT_SOTTOTITOLO);
        nota.setForeground(COLORE_TESTO_SECONDARIO);

        GridBagConstraints gbcNota = new GridBagConstraints();
        gbcNota.gridx = 0;
        gbcNota.gridy = 2;
        gbcNota.gridwidth = 2;
        gbcNota.fill = GridBagConstraints.HORIZONTAL;
        gbcNota.insets = new Insets(8, 0, 6, 0);

        form.add(nota, gbcNota);

        btnLicenziaConSostituto =
                creaBottone("Licenzia con sostituto");

        GridBagConstraints gbcConSostituto = new GridBagConstraints();
        gbcConSostituto.gridx = 1;
        gbcConSostituto.gridy = 3;
        gbcConSostituto.fill = GridBagConstraints.HORIZONTAL;
        gbcConSostituto.insets = new Insets(10, 8, 0, 0);

        form.add(btnLicenziaConSostituto, gbcConSostituto);

        btnLicenziaSenzaSostituto =
                creaBottone("Licenzia senza sostituto");

        GridBagConstraints gbcSenzaSostituto = new GridBagConstraints();
        gbcSenzaSostituto.gridx = 1;
        gbcSenzaSostituto.gridy = 4;
        gbcSenzaSostituto.fill = GridBagConstraints.HORIZONTAL;
        gbcSenzaSostituto.insets = new Insets(8, 8, 0, 0);

        form.add(btnLicenziaSenzaSostituto, gbcSenzaSostituto);
        return form;
    }

    /**
     * Crea la barra inferiore della schermata.
     *
     * @return barra delle azioni
     */
    private JPanel creaBarraAzioni() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);

        lblStato = new JLabel("Pronto.");
        lblStato.setFont(FONT_SOTTOTITOLO);
        lblStato.setForeground(COLORE_TESTO_SECONDARIO);

        JPanel pulsanti = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );

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
     * Crea un contenitore grafico con titolo.
     *
     * @param titoloSezione titolo del contenitore
     * @return pannello configurato
     */
    private JPanel creaCard(String titoloSezione) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(COLORE_CARD);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLORE_BORDO),
                        new EmptyBorder(16, 18, 18, 18)
                )
        );

        JLabel titolo = new JLabel(titoloSezione);
        titolo.setFont(FONT_SEZIONE);
        titolo.setForeground(COLORE_TESTO);

        card.add(titolo, BorderLayout.NORTH);
        return card;
    }

    /**
     * Crea un campo di testo.
     *
     * @return campo configurato
     */
    private JTextField creaCampoTesto() {
        JTextField campo = new JTextField();

        campo.setFont(FONT_BASE);
        campo.setForeground(COLORE_TESTO);
        campo.setPreferredSize(new Dimension(250, 32));
        campo.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLORE_BORDO),
                        new EmptyBorder(5, 9, 5, 9)
                )
        );

        return campo;
    }

    /**
     * Crea una combo box.
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
     * Crea un pulsante.
     *
     * @param testo testo del pulsante
     * @return pulsante configurato
     */
    private JButton creaBottone(String testo) {
        JButton bottone = new JButton(testo);

        bottone.setFont(FONT_BOTTONE);
        bottone.setFocusPainted(false);
        bottone.setPreferredSize(new Dimension(190, 34));

        return bottone;
    }

    /**
     * Aggiunge una riga etichetta-componente a un form.
     *
     * @param form pannello del form
     * @param riga posizione verticale
     * @param testoLabel testo dell'etichetta
     * @param componenteInput componente grafico
     */
    private void aggiungiRigaForm(
            JPanel form,
            int riga,
            String testoLabel,
            Component componenteInput) {

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

    /*
     * Componenti esposti esclusivamente per permettere al Controller
     * di registrare gli eventi. La View non registra alcun listener.
     */

    public JButton getBtnAggiorna() {
        return btnAggiorna;
    }

    public JButton getBtnAssumi() {
        return btnAssumi;
    }

    public JButton getBtnLicenziaConSostituto() {
        return btnLicenziaConSostituto;
    }

    public JButton getBtnLicenziaSenzaSostituto() {
        return btnLicenziaSenzaSostituto;
    }

    public JButton getBtnCalcolaRetribuzioni() {
        return btnCalcolaRetribuzioni;
    }

    /**
     * Restituisce la tabella esclusivamente come sorgente dell'evento
     * di selezione.
     *
     * Il Controller non deve modificarne modello, righe o colonne.
     *
     * @return tabella grafica
     */
    public JTable getTabellaPT() {
        return tabellaPT;
    }

    /*
     * Lettura degli input grafici.
     */

    public String getNomeInserito() {
        return txtNome.getText();
    }

    public String getCognomeInserito() {
        return txtCognome.getText();
    }

    public String getEmailInserita() {
        return txtEmail.getText();
    }

    public String getSpecializzazioneInserita() {
        return txtSpecializzazione.getText();
    }

    public String getImportoRetribuzioneInserito() {
        return txtImportoRetribuzione.getText();
    }

    public String getTipoRetribuzioneSelezionato() {
        Object selezione = comboTipoRetribuzione.getSelectedItem();

        if (selezione == null) {
            return null;
        }

        return selezione.toString();
    }

    public String getIdTrainerDaLicenziare() {
        return txtIdDaLicenziare.getText();
    }

    /**
     * Restituisce l'indice della riga selezionata.
     *
     * @return indice nel modello grafico, oppure -1
     */
    public int getIndiceTrainerSelezionato() {
        int indiceVista = tabellaPT.getSelectedRow();

        if (indiceVista < 0) {
            return -1;
        }

        return tabellaPT.convertRowIndexToModel(indiceVista);
    }

    /**
     * Restituisce l'indice dell'opzione grafica selezionata nella combo
     * dei sostituti.
     *
     * @return indice dell'opzione reale, oppure -1
     */
    public int getIndiceSostitutoSelezionato() {
        int indiceCombo = comboSostituto.getSelectedIndex();
        int indiceSostituto = indiceCombo - 1;

        if (indiceSostituto < 0
                || indiceSostituto >= numeroSostitutiVisualizzati) {

            return -1;
        }

        return indiceSostituto;
    }

    /*
     * Operazioni esclusivamente grafiche.
     */

    /**
     * Mostra nella tabella righe già preparate dallo strato di presentazione.
     *
     * @param righe valori grafici da visualizzare
     */
    public void mostraRigheTrainer(Object[][] righe) {
        modelloTabella.setRowCount(0);
        tabellaPT.clearSelection();

        if (righe == null) {
            return;
        }

        for (Object[] riga : righe) {
            if (riga != null) {
                modelloTabella.addRow(riga);
            }
        }
    }

    /**
     * Mostra nella combo le opzioni grafiche ricevute.
     *
     * @param tipiRetribuzione testi da visualizzare
     */
    public void mostraTipiRetribuzione(String[] tipiRetribuzione) {
        comboTipoRetribuzione.removeAllItems();

        if (tipiRetribuzione == null) {
            return;
        }

        for (String tipo : tipiRetribuzione) {
            if (tipo != null) {
                comboTipoRetribuzione.addItem(tipo);
            }
        }

        if (comboTipoRetribuzione.getItemCount() > 0) {
            comboTipoRetribuzione.setSelectedIndex(0);
        }
    }

    /**
     * Mostra nella combo le descrizioni grafiche ricevute.
     *
     * @param descrizioni testi da visualizzare
     */
    public void mostraSostituti(String[] descrizioni) {
        comboSostituto.removeAllItems();
        comboSostituto.addItem(TESTO_SELEZIONA_SOSTITUTO);

        numeroSostitutiVisualizzati =
                descrizioni == null ? 0 : descrizioni.length;

        if (numeroSostitutiVisualizzati == 0) {
            comboSostituto.addItem(TESTO_NESSUN_SOSTITUTO);
            return;
        }

        for (String descrizione : descrizioni) {
            comboSostituto.addItem(descrizione);
        }

        comboSostituto.setSelectedIndex(0);
    }

    /**
     * Mostra nel campo grafico l'identificativo ricevuto.
     *
     * @param idTrainer testo da visualizzare
     */
    public void mostraIdTrainerDaLicenziare(String idTrainer) {
        txtIdDaLicenziare.setText(
                idTrainer == null ? "" : idTrainer
        );
    }

    /**
     * Aggiorna la barra grafica di stato.
     *
     * @param messaggio testo da visualizzare
     */
    public void mostraStato(String messaggio) {
        lblStato.setText(
                messaggio == null ? "" : messaggio
        );
    }

    /**
     * Mostra graficamente un messaggio informativo.
     *
     * @param messaggio testo da visualizzare
     */
    public void mostraSuccesso(String messaggio) {
        mostraStato(messaggio);

        JOptionPane.showMessageDialog(
                this,
                messaggio,
                "Operazione completata",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Mostra graficamente un messaggio di errore.
     *
     * @param messaggio testo da visualizzare
     */
    public void mostraErrore(String messaggio) {
        mostraStato(messaggio);

        JOptionPane.showMessageDialog(
                this,
                messaggio,
                "Errore",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Mostra graficamente il totale ricevuto.
     *
     * @param totale valore da visualizzare
     */
    public void mostraTotaleRetribuzioni(double totale) {
        mostraSuccesso(
                String.format(
                        "Totale retribuzioni mensili: %.2f €",
                        totale
                )
        );
    }

    /**
     * Pulisce graficamente il form di assunzione.
     */
    public void pulisciFormAssunzione() {
        txtNome.setText("");
        txtCognome.setText("");
        txtEmail.setText("");
        txtIdTrainer.setText(TESTO_ID_AUTOMATICO);
        txtSpecializzazione.setText("");
        txtImportoRetribuzione.setText("");

        if (comboTipoRetribuzione.getItemCount() > 0) {
            comboTipoRetribuzione.setSelectedIndex(0);
        }
    }

    /**
     * Pulisce graficamente il form di licenziamento.
     */
    public void pulisciFormLicenziamento() {
        txtIdDaLicenziare.setText("");
        tabellaPT.clearSelection();

        numeroSostitutiVisualizzati = 0;

        comboSostituto.removeAllItems();
        comboSostituto.addItem(TESTO_SELEZIONA_SOSTITUTO);
    }
}