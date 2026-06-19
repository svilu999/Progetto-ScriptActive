package it.unipv.posfw.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.Corso;

/**
 * La classe {@code DashboardClienteView} rappresenta il componente <b>View</b> nel pattern architetturale <b>MVC (Model-View-Controller)</b>.
 * <p>
 * Estende {@link javax.swing.JFrame}, agendo da <i>Top-level container</i> per la Graphical User Interface (GUI) sviluppata con la 
 * libreria <i>Swing</i>. Nel rispetto del <b>Principio di separazione Modello-Vista</b>, questa classe si occupa esclusivamente di 
 * presentare i dati di dominio all'utente e di istanziare i componenti per l'interazione, demandando al Controller la gestione 
 * degli eventi (tramite l'<i>Event Delegation Model</i>) e la logica di business.
 * </p>
 * <p>
 * <b>Tracciabilità Requisiti:</b><br>
 * Risponde allo <b>Use Case UC4: Registrazione e Monitoraggio Prestazioni</b> e supporta le logiche post-<b>Login</b>.
 * Fornisce l'interfaccia grafica principale attraverso cui il Cliente accede alle funzionalità di base e premium.
 * Include logica visiva per riflettere i vincoli del dominio, quali la distinzione tra "Utente Base" e "Cliente Premium",
 * preservando il disaccoppiamento: la View mostra lo stato, ma l'autorizzazione all'accesso (Flusso Alternativo 1)
 * sarà garantita dal Controller.
 * </p>
 * * @author Vilucchi
 * @version 1.1
 * @see javax.swing.JFrame
 * @see it.unipv.posfw.domain.Cliente
 * @see it.unipv.posfw.domain.Corso
 */
public class DashboardClienteView extends JFrame {
    
    private Cliente utenteCorrente;

    private JLabel lblBenvenuto;
    private JLabel lblAbbonamento;
    private JPanel panelPrenotazioni;
    
    /* * Risoluzione Violazione Architetturale: Information Hiding.
     * I bottoni sono ora privati. I Controller esterni non possono più invocare direttamente 
     * btnAreaPremium.addActionListener(), ma devono passare attraverso i metodi setter dedicati,
     * riducendo l'accoppiamento strutturale.
     */
    private JButton btnAreaPremium;
    private JButton btnPrenotaCorsi; 
    
    private final Font fontTitolo = new Font("SansSerif", Font.BOLD, 22);
    private final Font fontSottotitolo = new Font("SansSerif", Font.PLAIN, 16);
    private final Font fontBottoni = new Font("SansSerif", Font.BOLD, 14);

    /**
     * Costruttore di default per la classe {@code DashboardClienteView}.
     * <p>
     * Inizializza il <i>Top-level container</i> definendone le proprietà fondamentali (titolo, dimensioni, operazione di chiusura).
     * Imita il comportamento di un'applicazione client standard, configurando un layout di base ({@link BorderLayout})
     * e delegando la costruzione dell'albero dei componenti (<i>Component Tree</i>) a un metodo dedicato.
     * </p>
     */
    public DashboardClienteView() {
        setTitle("ScriptActive - Dashboard Cliente");
        setSize(600, 500); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        inizializzaComponenti();
    }

    /**
     * Metodo di servizio privato per l'inizializzazione dei <i>General-purpose containers</i> (es. {@code JPanel}, {@code JScrollPane}) 
     * e dei componenti atomici <i>lightweight</i> (es. {@code JLabel}, {@code JButton}).
     * <p>
     * Segue il paradigma di composizione UI di Swing. I riferimenti ai componenti interattivi sono 
     * mantenuti privati e le loro azioni sono esposte ai Controller tramite i metodi add...Listener().
     * </p>
     */
    private void inizializzaComponenti() {
        JPanel panelTop = new JPanel(new GridLayout(2, 1, 5, 5));
        
        lblBenvenuto = new JLabel("Benvenuto!");
        lblBenvenuto.setFont(fontTitolo);
        lblBenvenuto.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblAbbonamento = new JLabel("Livello Abbonamento:");
        lblAbbonamento.setFont(fontSottotitolo);
        lblAbbonamento.setForeground(Color.DARK_GRAY);
        lblAbbonamento.setHorizontalAlignment(SwingConstants.CENTER);
        
        panelTop.add(lblBenvenuto);
        panelTop.add(lblAbbonamento);
        add(panelTop, BorderLayout.NORTH);

        panelPrenotazioni = new JPanel();
        panelPrenotazioni.setLayout(new BoxLayout(panelPrenotazioni, BoxLayout.Y_AXIS));
        panelPrenotazioni.setBackground(Color.WHITE);
        
        JScrollPane scrollPrenotazioni = new JScrollPane(panelPrenotazioni);
        scrollPrenotazioni.setBorder(BorderFactory.createTitledBorder(null, "I Tuoi Prossimi Corsi", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, fontBottoni));
        add(scrollPrenotazioni, BorderLayout.CENTER);
        
        JPanel panelBottoni = new JPanel(new GridLayout(1, 2, 15, 0)); 
        
        btnPrenotaCorsi = new JButton("VAI AL PALINSESTO CORSI");
        btnPrenotaCorsi.setFont(fontBottoni);
        btnPrenotaCorsi.setBackground(new Color(70, 130, 180)); // Rendering visivo: Blu
        btnPrenotaCorsi.setForeground(Color.WHITE);
        btnPrenotaCorsi.setFocusPainted(false);
        btnPrenotaCorsi.setOpaque(true);
        btnPrenotaCorsi.setBorderPainted(false);
        btnPrenotaCorsi.setPreferredSize(new Dimension(200, 50));
        
        btnAreaPremium = new JButton("LA MIA AREA PREMIUM");
        btnAreaPremium.setFont(fontBottoni);
        btnAreaPremium.setBackground(new Color(77, 43, 107)); // Rendering visivo: Viola (identità visiva Premium)
        btnAreaPremium.setForeground(Color.WHITE);
        btnAreaPremium.setFocusPainted(false);
        btnAreaPremium.setOpaque(true);
        btnAreaPremium.setBorderPainted(false);
        btnAreaPremium.setPreferredSize(new Dimension(200, 50));

        panelBottoni.add(btnPrenotaCorsi);
        panelBottoni.add(btnAreaPremium);
        
        add(panelBottoni, BorderLayout.SOUTH);
    }

    /**
     * Esegue il data-binding direzionale (Modello &rarr; Vista) aggiornando lo stato visivo in base all'entità di dominio.
     * <p>
     * Modifica testata e metadati della dashboard estraendo le informazioni rilevanti dal modello {@link Cliente}. 
     * Il metodo gestisce dinamicamente la rappresentazione visiva dei privilegi utente (es. differenziazione 
     * cromatica tra utente Base e Premium), come richiesto dalle precondizioni di accesso dell'UC4.
     * </p>
     * * @param cliente L'istanza dell'entità di dominio {@code Cliente} attualmente autenticata nel sistema.
     */
    public void impostaDatiCliente(Cliente cliente) {
        this.utenteCorrente = cliente;
        
        lblBenvenuto.setText("Bentornato, " + cliente.getNome() + " " + cliente.getCognome() + "!");
        
        String tipoAbb = "BASE"; 
        if (cliente.getTipoAbbonamento() != null) {
            tipoAbb = cliente.getTipoAbbonamento().toString(); 
        }
        
        String durataAbb = "N/A";
        if (cliente.getAbbonamentoAttivo() != null && cliente.getAbbonamentoAttivo().getLivello() != null) {
            durataAbb = cliente.getAbbonamentoAttivo().getLivello().toString(); 
        }
        
        lblAbbonamento.setText("Abbonamento: " + tipoAbb.toUpperCase() + " | Durata: " + durataAbb);
        
        /* * Adeguamento visivo dell'interfaccia in base alle policy di dominio.
         * Evidenzia lo stato Premium rispetto al livello Base.
         */
        if ("PREMIUM".equalsIgnoreCase(tipoAbb)) {
            lblAbbonamento.setForeground(new Color(77, 43, 107)); 
        } else {
            lblAbbonamento.setForeground(Color.DARK_GRAY); 
        }
    }
    
    /**
     * Restituisce l'istanza dell'entità di dominio attualmente vincolata a questa Vista.
     * <p>
     * Permette al Controller di recuperare agilmente il contesto dell'utente (Sessione locale) 
     * durante la gestione degli eventi delegati.
     * </p>
     * * @return L'oggetto {@link Cliente} in uso.
     */
    public Cliente getUtenteCorrente() {
        return this.utenteCorrente;
    }
    
    /**
     * Esegue il rendering grafico dinamico di una collezione di oggetti di dominio {@link Corso}.
     * <p>
     * Se la collezione risulta vuota, delega la gestione del feedback visivo a un metodo di fallback.
     * In caso contrario, itera sulla collezione e costruisce dinamicamente sotto-pannelli informativi,
     * aggiornando l'albero dei componenti in fase di runtime (richiedendo {@code revalidate()} e {@code repaint()}).
     * </p>
     * * @param corsi La lista parametrizzata di oggetti {@link Corso} restituiti dal livello architetturale sottostante (es. DAO).
     */
    public void mostraCorsiPrenotati(List<Corso> corsi) {
        panelPrenotazioni.removeAll();

        if (corsi == null || corsi.isEmpty()) {
            mostraMessaggioPrenotazioni("Non hai ancora prenotato nessun corso per i prossimi giorni.");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

            for (Corso corso : corsi) {
                JPanel panelScheda = new JPanel(new BorderLayout(10, 10));
                panelScheda.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 10, 5, 10),
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)
                ));
                panelScheda.setBackground(Color.WHITE);

                StringBuilder sb = new StringBuilder();
                sb.append(" ").append(corso.getNome().toUpperCase()).append("\n");
                sb.append(" ").append(corso.getDataOra().format(formatter)).append("\n");
                
                if (corso.getTrainerAssegnato() != null) {
                    String nomePt = corso.getTrainerAssegnato().getNome();
                    String cognomePt = corso.getTrainerAssegnato().getCognome();
                    
                    /*
                     * Valutazione dello stato dell'entità relazionale Trainer.
                     * Gestisce la casistica in cui il livello di persistenza (DAO) restituisca un'entità stub 
                     * o parzialmente inizializzata prima dell'effettiva assegnazione.
                     */
                    if ("Trainer".equalsIgnoreCase(nomePt) || nomePt == null || nomePt.trim().isEmpty()) {
                        sb.append(" Trainer: Assegnazione in corso...");
                    } else {
                        // Rendering standard dell'entità Trainer consolidata
                        sb.append(" Trainer: ").append(nomePt).append(" ").append(cognomePt != null ? cognomePt : "");
                    }
                }
            
                JTextArea txtDettagli = new JTextArea(sb.toString());
                txtDettagli.setFont(new Font("SansSerif", Font.PLAIN, 14));
                txtDettagli.setEditable(false);
                txtDettagli.setOpaque(false);

                panelScheda.add(txtDettagli, BorderLayout.CENTER);
                
                panelPrenotazioni.add(panelScheda);
                panelPrenotazioni.add(Box.createRigidArea(new Dimension(0, 5))); 
            }
        }

        panelPrenotazioni.revalidate();
        panelPrenotazioni.repaint();
    }

    /**
     * Metodo di utility per la gestione del rendering degli stati vuoti (<i>Empty State</i>).
     * <p>
     * Ripulisce il contenitore principale dedicato alle prenotazioni e inietta un messaggio informativo,
     * garantendo un feedback all'utente e allineando l'interfaccia al variare dello stato applicativo.
     * </p>
     * * @param messaggio Il testo di cortesia da visualizzare nel pannello.
     */
    private void mostraMessaggioPrenotazioni(String messaggio) {
        panelPrenotazioni.removeAll();
        JLabel lblMsg = new JLabel(messaggio);
        lblMsg.setFont(fontSottotitolo);
        lblMsg.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelPrenotazioni.add(lblMsg);
        panelPrenotazioni.revalidate();
        panelPrenotazioni.repaint();
    }
    
    /**
     * Registra un {@link java.awt.event.ActionListener} sul componente grafico delegato all'accesso all'Area Premium.
     * <p>
     * Implementa rigorosamente il principio dell'<b>Information Hiding</b>. La Vista nasconde la propria implementazione interna
     * rendendo privato il bottone, e permette al Controller di registrarsi agli eventi UI tramite questo metodo pubblico.
     * </p>
     * * @param listener L'ascoltatore degli eventi fornito dal Controller per gestire il click sull'Area Premium.
     */
    public void addAreaPremiumListener(ActionListener listener) {
        this.btnAreaPremium.addActionListener(listener);
    }

    /**
     * Registra un {@link java.awt.event.ActionListener} sul componente grafico delegato alla prenotazione dei corsi.
     * <p>
     * Risolve l'accoppiamento strutturale con il Controller applicando il pattern <b>Event Delegation</b>.
     * </p>
     * * @param listener L'ascoltatore degli eventi fornito dal Controller per gestire il click sul Palinsesto.
     */
    public void addPrenotaCorsiListener(ActionListener listener) {
        this.btnPrenotaCorsi.addActionListener(listener);
    }
}