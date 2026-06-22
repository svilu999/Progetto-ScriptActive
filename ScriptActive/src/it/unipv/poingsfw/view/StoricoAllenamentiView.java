package it.unipv.poingsfw.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import it.unipv.poingsfw.controller.StoricoAllenamentiController;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.DatiFormPojo;
import it.unipv.poingsfw.domain.SessioneAllenamento;
import it.unipv.poingsfw.exceptions.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Nel rispetto rigoroso del <b>Principio di Separazione Modello-Vista</b>, questa classe si occupa unicamente della presentazione 
 * dei dati e dell'inizializzazione degli elementi grafici (Component e Container), garantendo l'assenza di logica di business.
 * Le interazioni dell'utente vengono catturate e delegate al Controller mediante l'<i>Event Delegation Model</i>.
 * </p>
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Costituisce la GUI principale per lo <b>Use Case UC4: Registrazione e Monitoraggio Prestazioni</b>.
 * Mette a disposizione del <i>Cliente Premium</i> i form per l'inserimento dei parametri quantitativi degli allenamenti 
 * (Main Success Scenario) e visualizza l'aggregazione di tali dati nella vista riepilogativa. Gestisce inoltre il 
 * rendering del <i>Flusso Alternativo 1</i>, bloccando l'accesso e proponendo l'upgrade qualora il sistema rilevi un Utente Base.
 * </p>
 * * @author Vilucchi
 * @version 1.1
 * @see javax.swing.JFrame
 * @see it.unipv.poingsfw.controller.StoricoAllenamentiController
 */
public class StoricoAllenamentiView extends JFrame {

    /**
     * Riferimento al <b>Controller</b> associato a questa vista.
     * Gestisce la logica applicativa disaccoppiando la View dal Modello.
     */
    private StoricoAllenamentiController controller;
    
    private JPanel panelForm;
    private JTextField txtData, txtNomeEsercizio, txtCarichi, txtRipetizioni;
    private JComboBox<String> comboCategoria; 
    private JButton btnAggiungiEsercizio; 
    private JButton btnSalvaSessione;     
    private JTextArea txtAnteprimaBozza;  
    
    private JPanel panelStoricoContainer; 
    
    /* * Risoluzione Violazione Architetturale: Information Hiding.
     * I componenti UI sono stati resi privati. L'accesso dall'esterno (es. dal Controller) 
     * avviene esclusivamente tramite i metodi pubblici di registrazione dei listener,
     * garantendo un basso accoppiamento e incapsulando la struttura interna della View.
     */
    private JButton btnSimulaAccesso; 
    private JButton btnIndietro; 
    
    private Cliente utenteCorrente; 
    
    private List<DatiFormPojo> eserciziInBozza;

    private final Font fontTitoli = new Font("SansSerif", Font.BOLD, 14);
    private final Font fontTesto = new Font("SansSerif", Font.PLAIN, 14);

    /**
     * Costruttore di default per la classe {@code StoricoAllenamentiView}.
     * <p>
     * Inizializza la struttura dati temporanea per il buffer della UI e configura i parametri 
     * fondamentali del <i>Top-level container</i> (titolo, dimensioni, layout base). Delega 
     * l'istanziazione del <i>Component Tree</i> a un metodo privato dedicato.
     * </p>
     */
    public StoricoAllenamentiView() {
        eserciziInBozza = new ArrayList<>(); 
        
        setTitle("ScriptActive - I Miei Allenamenti");
        setSize(600, 800); 
        setLocationRelativeTo(null); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15)); 
        
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inizializzaComponenti();
    }

    /**
     * Inietta la dipendenza del Controller in questa Vista.
     * <p>
     * Permette alla View di notificare gli eventi di input dell'utente al controller.
     * </p>
     * * @param controller Istanza concreta del {@link StoricoAllenamentiController}.
     */
    public void setController(StoricoAllenamentiController controller) {
        this.controller = controller;
    }

    private void inizializzaComponenti() {
        JPanel panelTop = new JPanel(new BorderLayout()); 
        btnIndietro = new JButton("⬅ Indietro");
        btnIndietro.setFont(fontTesto);
        btnIndietro.setFocusPainted(false);
        btnIndietro.setBackground(new Color(230, 230, 230));
        panelTop.add(btnIndietro, BorderLayout.WEST);

        /* * Definizione del pulsante per la Call to Action (Upgrade Premium).
         * Questo componente sarà utilizzato nel Flusso Alternativo 1 dello Use Case UC4.
         */
        btnSimulaAccesso = new JButton("Sblocca la tua Area Premium");
        btnSimulaAccesso.setFont(fontTitoli);
        btnSimulaAccesso.setBackground(new Color(77, 43, 107)); 
        btnSimulaAccesso.setForeground(Color.WHITE); 
        btnSimulaAccesso.setFocusPainted(false);
        btnSimulaAccesso.setOpaque(true); 
        btnSimulaAccesso.setBorderPainted(false);
        btnSimulaAccesso.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        
        add(panelTop, BorderLayout.NORTH);

        /* Configurazione del Pannello Form (Main Success Scenario) */
        panelForm = new JPanel(new BorderLayout(10, 10));
        panelForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(null, "Crea Nuova Sessione di Allenamento", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontTitoli),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JPanel panelInput = new JPanel(new GridLayout(5, 2, 10, 10));
        
        JLabel lblData = new JLabel("Data (dd/MM/yyyy):");
        lblData.setFont(fontTesto);
        panelInput.add(lblData);
        txtData = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        txtData.setFont(fontTesto);
        panelInput.add(txtData);

        JLabel lblCategoria = new JLabel("Gruppo Muscolare:");
        lblCategoria.setFont(fontTesto);
        panelInput.add(lblCategoria);
        String[] categorie = {"Seleziona...", "PETTO", "DORSO", "GAMBE", "BRACCIA", "SPALLE", "ADDOME", "FULL BODY"};
        comboCategoria = new JComboBox<>(categorie);
        comboCategoria.setFont(fontTesto);
        panelInput.add(comboCategoria);

        JLabel lblNome = new JLabel("Nome Esercizio:");
        lblNome.setFont(fontTesto);
        panelInput.add(lblNome);
        txtNomeEsercizio = new JTextField();
        txtNomeEsercizio.setFont(fontTesto);
        panelInput.add(txtNomeEsercizio);

        JLabel lblCarico = new JLabel("Carico (kg):");
        lblCarico.setFont(fontTesto);
        panelInput.add(lblCarico);
        txtCarichi = new JTextField();
        txtCarichi.setFont(fontTesto);
        panelInput.add(txtCarichi);

        JLabel lblRipetizioni = new JLabel("Ripetizioni:");
        lblRipetizioni.setFont(fontTesto);
        panelInput.add(lblRipetizioni);
        txtRipetizioni = new JTextField();
        txtRipetizioni.setFont(fontTesto);
        panelInput.add(txtRipetizioni);

        btnAggiungiEsercizio = new JButton("Aggiungi Esercizio alla Scheda");
        btnAggiungiEsercizio.setFont(fontTitoli);
        btnAggiungiEsercizio.setBackground(new Color(77, 43, 107));
        btnAggiungiEsercizio.setForeground(Color.WHITE);
        btnAggiungiEsercizio.setFocusPainted(false);
        btnAggiungiEsercizio.setOpaque(true);
        btnAggiungiEsercizio.setBorderPainted(false);
        btnAggiungiEsercizio.addActionListener(e -> clickAggiungiEsercizio());

        /* Componente per il rendering testuale temporaneo dello stato in bozza */
        txtAnteprimaBozza = new JTextArea(5, 20);
        txtAnteprimaBozza.setEditable(false);
        txtAnteprimaBozza.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollBozza = new JScrollPane(txtAnteprimaBozza);
        scrollBozza.setBorder(BorderFactory.createTitledBorder(null, "Esercizi in questa sessione", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontTesto));

        btnSalvaSessione = new JButton("SALVA SESSIONE COMPLETA");
        btnSalvaSessione.setFont(fontTitoli);
        btnSalvaSessione.setBackground(new Color(46, 139, 87)); 
        btnSalvaSessione.setForeground(Color.WHITE);
        btnSalvaSessione.setPreferredSize(new Dimension(100, 40));
        btnSalvaSessione.setFocusPainted(false);
        btnSalvaSessione.setOpaque(true);
        btnSalvaSessione.setBorderPainted(false); 
        
        btnSalvaSessione.addActionListener(e -> clickSalvaSessioneCompleta());

        JPanel panelMiddleContainer = new JPanel(new BorderLayout(10, 10));
        panelMiddleContainer.add(panelInput, BorderLayout.NORTH);
        panelMiddleContainer.add(btnAggiungiEsercizio, BorderLayout.CENTER);
        panelMiddleContainer.add(scrollBozza, BorderLayout.SOUTH);

        panelForm.add(panelMiddleContainer, BorderLayout.CENTER);
        panelForm.add(btnSalvaSessione, BorderLayout.SOUTH);

        add(panelForm, BorderLayout.CENTER);

        /* Contenitore per lo Storico Allenamenti (Postcondition ) */
        panelStoricoContainer = new JPanel();
        panelStoricoContainer.setLayout(new BoxLayout(panelStoricoContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panelStoricoContainer);
        scrollPane.setBorder(BorderFactory.createTitledBorder(null, "I Miei Allenamenti", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontTitoli));
        scrollPane.setPreferredSize(new Dimension(500, 250));
        add(scrollPane, BorderLayout.SOUTH);

        // Stato iniziale UI di default
        panelForm.setVisible(false);
        impostaStatoForm(false);
    }

    /**
     * Registra un {@link java.awt.event.ActionListener} sul componente grafico delegato all'azione "Indietro".
     * <p>
     * Implementa rigorosamente il principio dell'<b>Information Hiding</b>. Mantenendo il componente 
     * {@code btnIndietro} privato, la Vista nasconde la propria implementazione interna (basso accoppiamento).
     * Il Controller funge da <i>Observer</i> registrandosi agli eventi UI tramite questo metodo pubblico,
     * conformemente al pattern <i>Event Delegation Model</i>.
     * </p>
     * * @param listener L'ascoltatore degli eventi fornito dal Controller per gestire l'azione di ritorno.
     */
    public void addIndietroListener(ActionListener listener) {
        this.btnIndietro.addActionListener(listener);
    }

    /**
     * Registra un {@link java.awt.event.ActionListener} sul componente grafico delegato all'azione "Simula Accesso / Upgrade Premium".
     * <p>
     * Applicazione del principio di <b>Incapsulamento</b>: la Vista espone solo l'intento semantico dell'azione 
     * (richiesta di upgrade), senza rivelare se l'evento è scaturito da un {@code JButton}, un menù o altro componente Swing.
     * </p>
     * * @param listener L'ascoltatore degli eventi fornito dal Controller per avviare il flusso di Upgrade.
     */
    public void addSimulaAccessoListener(ActionListener listener) {
        this.btnSimulaAccesso.addActionListener(listener);
    }

    /**
     * Mantiene in sessione locale il riferimento all'attore primario che sta operando sulla vista.
     * <p>
     * Garantisce che gli eventi scaturiti dai pulsanti possiedano il contesto di dominio necessario
     * (es. identificazione del Cliente) da trasmettere al Controller.
     * </p>
     * * @param cliente L'oggetto {@link Cliente} in sessione (autenticato).
     */
    public void setUtenteCorrente(Cliente cliente) {
        this.utenteCorrente = cliente;
    }

    /**
     * Avvia la richiesta di accesso al modulo di gestione allenamenti delegandola al Controller.
     * <p>
     * Cattura un'eventuale eccezione derivante da vincoli di dominio (mancanza privilegi Premium)
     * e adegua l'interfaccia invocando il Flusso Alternativo.
     * </p>
     * * @param cliente Il {@link Cliente} che tenta di accedere al modulo.
     */
    public void clickAccediStorico(Cliente cliente) {
        if (controller != null && cliente != null) {
            try {
                controller.gestisciAccessoSezione(cliente);
            } catch (UtenteNonPremiumException e) {
                mostraBloccoUpgradePremium();
            }
        }
    }

    /**
     * Gestisce la logica di presentazione relativa all'inserimento di un singolo esercizio in bozza.
     * <p>
     * Applica controlli UI preliminari per la validazione formale dell'input (es. formato numerico) 
     * prima di aggiornare lo stato temporaneo della lista in memoria. Se i dati sono corretti,
     * aggiorna il <i>Data Transfer Object</i> interno ({@link DatiFormPojo}) e forza un aggiornamento
     * visivo sul <i>Event Dispatch Thread (EDT)</i>.
     * </p>
     */
    private void clickAggiungiEsercizio() {
        try {
            String categoria = (String) comboCategoria.getSelectedItem();
            if (categoria.equals("Seleziona...")) {
                JOptionPane.showMessageDialog(this, "Seleziona un gruppo muscolare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nomeEsercizio = txtNomeEsercizio.getText();
            if (nomeEsercizio == null || nomeEsercizio.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci il nome dell'esercizio.", "Attenzione", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double carichi = Double.parseDouble(txtCarichi.getText());
            int ripetizioni = Integer.parseInt(txtRipetizioni.getText());

            String nomeEsercizioFormattato = "[" + categoria + "] " + nomeEsercizio;

            DatiFormPojo nuovoEsercizio = new DatiFormPojo(nomeEsercizioFormattato, carichi, ripetizioni);
            eserciziInBozza.add(nuovoEsercizio);
            
            aggiornaAnteprimaBozza();
            
            // Reset campi form dopo inserimento
            txtNomeEsercizio.setText("");
            txtCarichi.setText("");
            txtRipetizioni.setText("");
            txtNomeEsercizio.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Carico e Ripetizioni devono essere numeri validi.", "Errore Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Intercetta la richiesta utente di consolidamento della sessione di allenamento.
     * <p>
     * Raccoglie i dati temporanei della View e demanda l'onere dell'elaborazione di business al 
     * {@link StoricoAllenamentiController}. Provvede al mapping delle eccezioni di dominio e di 
     * persistenza, trasformandole in feedback visivi (<i>Dialog</i>) idonei alla comprensione dell'utente.
     * </p>
     */
    private void clickSalvaSessioneCompleta() {
        try {
            Date data = new SimpleDateFormat("dd/MM/yyyy").parse(txtData.getText());
            
            controller.salvaSessioneCompleta(data, eserciziInBozza, utenteCorrente);
            
            eserciziInBozza.clear();
            aggiornaAnteprimaBozza();
            JOptionPane.showMessageDialog(this, "Sessione di allenamento salvata con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato data errato. Utilizza gg/mm/aaaa.", "Errore Input", JOptionPane.WARNING_MESSAGE);
            
        } catch (SchedaVuotaException ex) {
            JOptionPane.showMessageDialog(this, "Non hai inserito nessun esercizio. La scheda non può essere vuota!", "Scheda Vuota", JOptionPane.WARNING_MESSAGE);
            
        } catch (DatiAllenamentoNonValidiException ex) {
            JOptionPane.showMessageDialog(this, "I parametri inseriti non sono validi. Controlla che ripetizioni e carichi siano corretti.", "Dati Non Validi", JOptionPane.WARNING_MESSAGE);
            
        } catch (SalvataggioFallitoException ex) {
            JOptionPane.showMessageDialog(this, "Impossibile contattare il server. Riprova più tardi.\nDettaglio: " + ex.getMessage(), "Errore di Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Provvede al data-binding unidirezionale dello stato temporaneo della sessione (Bozza) all'interno
     * del componente di testo dedicato all'anteprima.
     */
    private void aggiornaAnteprimaBozza() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (DatiFormPojo ex : eserciziInBozza) {
            sb.append(i++).append(". ").append(ex.getNomeEsercizio().toUpperCase())
              .append(" | ").append(ex.getCarichi()).append(" kg x ").append(ex.getRipetizioni()).append(" rep \n");
        }
        txtAnteprimaBozza.setText(sb.toString());
    }

    /**
     * Esegue il rendering grafico associato al <i>Flusso Alternativo 1</i> .
     * <p>
     * Se il sistema deduce l'assenza del livello di abbonamento "Premium", la View oscura dinamicamente
     * il Modulo di inserimento e altera il <i>Component Tree</i> per esporre la <i>Call to Action</i> per l'upgrade.
     * </p>
     */
    public void mostraBloccoUpgradePremium() {
        panelForm.setVisible(false); 
        impostaStatoForm(false);
        
        panelStoricoContainer.removeAll();
        
        JLabel lblMessaggio = new JLabel("Accesso Negato: L'area allenamenti è riservata agli utenti Premium.");
        lblMessaggio.setFont(fontTesto);
        lblMessaggio.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        btnSimulaAccesso.setAlignmentX(Component.CENTER_ALIGNMENT); 
        btnSimulaAccesso.setMaximumSize(new Dimension(300, 45)); 
        
        panelStoricoContainer.add(Box.createVerticalStrut(30)); 
        panelStoricoContainer.add(lblMessaggio);
        panelStoricoContainer.add(Box.createVerticalStrut(20)); 
        panelStoricoContainer.add(btnSimulaAccesso);
        
        this.revalidate(); // funzioni swing per sistemare la schermata a video (ricalcolo geoetrico e pixell
        this.repaint();
    }

    /**
     * Esegue il rendering grafico associato al Main Success Scenario dell'UC4.
     * <p>
     * Ripristina la visibilità e l'interattività del Form di inserimento parametri, notificando visivamente
     * lo stato applicativo sul Thread della GUI (EDT).
     * </p>
     */
    public void mostraModuloInserimento() {
        panelForm.setVisible(true); 
        impostaStatoForm(true);
        mostraMessaggioNelContainer("Modulo sbloccato. Visualizza qui i tuoi allenamenti precedenti:");
        this.revalidate();
        this.repaint();
    }

    /**
     * Itera sulla collezione di oggetti di dominio {@link SessioneAllenamento} ricevuta per 
     * costruire in runtime il contenitore storico.
     * <p>
     * Rappresenta la concretizzazione delle Postcondizioni dell'UC4 (<i>Il sistema aggrega i dati 
     * e genera la vista riepilogativa</i>). Include pulsanti reattivi generati dinamicamente per
     * intercettare comandi di eliminazione per ogni singola entità renderizzata.
     * </p>
     * * @param storico Lista degli allenamenti registrati dal dominio e recuperati dalla persistenza.
     */
    public void mostraStorico(List<SessioneAllenamento> storico) {
        panelStoricoContainer.removeAll(); 

        if (storico.isEmpty()) {
            mostraMessaggioNelContainer("Nessun allenamento registrato.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            for (SessioneAllenamento sessione : storico) {
                JPanel panelScheda = new JPanel(new BorderLayout(10, 10));
                panelScheda.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5), 
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true) 
                ));
                panelScheda.setBackground(Color.WHITE);

                String gruppoMuscolare = "Mista"; 
                
                if (!sessione.getEsercizi().isEmpty()) {
                    java.util.Set<String> categorieUniche = new java.util.HashSet<>();
                    
                    for (DatiFormPojo esercizio : sessione.getEsercizi()) {
                        String nomeEs = esercizio.getNomeEsercizio();
                        if (nomeEs.startsWith("[")) {
                            String cat = nomeEs.substring(1, nomeEs.indexOf("]"));
                            categorieUniche.add(cat);
                        }
                    }
                    
                    if (categorieUniche.size() == 1) {
                        gruppoMuscolare = categorieUniche.iterator().next(); 
                    } else if (categorieUniche.size() > 1) {
                        gruppoMuscolare = "MISTA";
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Sessione di ").append(gruppoMuscolare).append(" in data : ").append(sdf.format(sessione.getData())).append("\n\n");
                
                for (DatiFormPojo esercizio : sessione.getEsercizi()) {
                
                    String nomePulito = esercizio.getNomeEsercizio();
                    if (nomePulito.startsWith("[")) {
                        nomePulito = nomePulito.substring(nomePulito.indexOf("]") + 1).trim();
                    }
                    
                    sb.append(String.format("   ▸ %s | %.1f kg | %d rep\n", 
                              nomePulito.toUpperCase(), esercizio.getCarichi(), esercizio.getRipetizioni()));
                }

                JTextArea txtDettagli = new JTextArea(sb.toString());
                txtDettagli.setFont(new Font("Monospaced", Font.PLAIN, 13));
                txtDettagli.setEditable(false);
                txtDettagli.setOpaque(false); 

                JButton btnElimina = new JButton("Elimina");
                btnElimina.setForeground(Color.RED);
                btnElimina.setBackground(Color.WHITE);
                btnElimina.setFocusPainted(false);
                
                /* Configurazione EventListener Inline per la rimozione dell'entità delegata al Controller */
                btnElimina.addActionListener(e -> {
                    int conferma = JOptionPane.showConfirmDialog(this, "Vuoi eliminare questa sessione?", "Conferma", JOptionPane.YES_NO_OPTION);
                    if (conferma == JOptionPane.YES_OPTION) {
                        try {
                            controller.eliminaSessioneSelezionata(sessione, utenteCorrente);
                        } catch (SalvataggioFallitoException ex) {
                            JOptionPane.showMessageDialog(this, "Impossibile eliminare dal Database: " + ex.getMessage(), "Errore di Sistema", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                panelScheda.add(txtDettagli, BorderLayout.CENTER);
                panelScheda.add(btnElimina, BorderLayout.EAST);
                panelStoricoContainer.add(panelScheda);
            }
        }

        panelStoricoContainer.revalidate();
        panelStoricoContainer.repaint();
    }

 
    public void mostraMessaggio(String messaggio, String titolo, int tipoMessaggio) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, tipoMessaggio);
    }

    /**
     * Commuta  lo stato di editabilità dei campi del form (es. TextField, ComboBox, Bottoni).
     * <p>
     * Adoperato per assecondare la logica condizionale dettata dal livello di abbonamento (UC4).
     * </p>
     * * @param abilitato Valore booleano per attivare o inibire l'interazione utente sui componenti.
     */
    private void impostaStatoForm(boolean abilitato) {
        txtData.setEnabled(abilitato);
        comboCategoria.setEnabled(abilitato);
        txtNomeEsercizio.setEnabled(abilitato);
        txtCarichi.setEnabled(abilitato);
        txtRipetizioni.setEnabled(abilitato);
        btnAggiungiEsercizio.setEnabled(abilitato);
        btnSalvaSessione.setEnabled(abilitato);
    }
    
    /**
     * Ripristina lo stato del contenitore storico mostrando un messaggio statico informativo.
     * <p>
     * Utilizzato per gestire l'<i>Empty State</i> (nessun allenamento presente) in conformità alle
     * best practice architetturali UI.
     * </p>
     * * @param messaggio Il testo di cortesia generato per colmare il layout vuoto.
     */
    private void mostraMessaggioNelContainer(String messaggio) {
        panelStoricoContainer.removeAll();
        JLabel lblMessaggio = new JLabel(messaggio);
        lblMessaggio.setFont(fontTesto);
        lblMessaggio.setAlignmentX(Component.CENTER_ALIGNMENT); 
        lblMessaggio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelStoricoContainer.add(lblMessaggio);
        panelStoricoContainer.revalidate();
        panelStoricoContainer.repaint();
    }
}