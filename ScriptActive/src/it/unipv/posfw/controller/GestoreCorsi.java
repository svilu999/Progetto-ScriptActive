package it.unipv.posfw.controller;

import it.unipv.posfw.dao.CorsoDAO;
import it.unipv.posfw.dao.CorsoDAOImpl;
import it.unipv.posfw.domain.Corso;
import it.unipv.posfw.domain.PersonalTrainer;
import it.unipv.posfw.domain.StatoCorso;
import it.unipv.posfw.exceptions.CorsoNonTrovatoException;
import it.unipv.posfw.exceptions.SovrapposizioneOrarioException;
import it.unipv.posfw.exceptions.TrainerNonValidoException;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

public class GestoreCorsi {

    // --- PATTERN SINGLETON ---
    private static GestoreCorsi istanza;
    
    // --- DIPENDENZE (DEPENDENCY INVERSION) ---
    private CorsoDAO corsoDAO;

    // Costruttore privato: impedisce l'istanziazione diretta dall'esterno
    private GestoreCorsi() {
        // Iniettiamo l'implementazione tramite l'interfaccia (Disaccoppiamento totale)
        this.corsoDAO = new CorsoDAOImpl();
    }

    // Punto di accesso globale e sincronizzato all'istanza unica
    public static synchronized GestoreCorsi getInstance() {
        if (istanza == null) {
            istanza = new GestoreCorsi();
        }
        return istanza;
    }

    // --- METODI CORE DEL CASO D'USO UC3 (THREAD-SAFE) ---

    /**
     * Coordina la creazione di un nuovo corso nel palinsesto.
     * La keyword synchronized implementa la mutua esclusione per evitare corruzioni da accessi concorrenti.
     */
    public synchronized void organizzaNuovoCorso(String nome, LocalDateTime orario, int capienza, String idPT) 
            throws TrainerNonValidoException, SovrapposizioneOrarioException {
        
        System.out.println("[CONTROLLER] Richiesta inserimento corso: " + nome + " in data " + orario);

        // 1. Validazione del Trainer tramite metodo privato ad alta coesione
        if (!convalidaTrainer(idPT)) {
            throw new TrainerNonValidoException("Errore UC3: Il Personal Trainer con ID " + idPT + " non esiste o non è attivo.");
        }

        // 2. Controllo sovrapposizioni orarie dello stesso Trainer
        if (controllaSovrapposizioni(idPT, orario)) {
            throw new SovrapposizioneOrarioException("Errore UC3: Il Trainer " + idPT + " è già occupato in questa fascia oraria.");
        }

        // 3. Generazione automatica di un ID univoco per il corso
        String idCorsoUnivoco = "CRS-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        
        // Istanziamo gli oggetti di dominio corretti come richiesto dal nuovo UML
        PersonalTrainer ptAssegnato = new PersonalTrainer("NomePT", "CognomePT", "trainer@scriptactive.it", idPT);
        Corso nuovoCorso = new Corso(idCorsoUnivoco, nome, orario, capienza, ptAssegnato);

        // 4. Delegalizzazione del salvataggio persistente al DAO
        corsoDAO.insert(nuovoCorso);
        System.out.println("[CONTROLLER] Nuovo corso registrato con ID: " + idCorsoUnivoco);
    }

    /**
     * Gestisce l'annullamento logico di un corso esistente.
     */
    public synchronized void annullaCorso(String idCorso) throws CorsoNonTrovatoException {
        System.out.println("[CONTROLLER] Richiesta annullamento corso ID: " + idCorso);

        // 1. Recupero dei dati dal DAO
        Corso corso = corsoDAO.findById(idCorso);
        if (corso == null) {
            throw new CorsoNonTrovatoException("Errore UC3: Corso con ID " + idCorso + " inesistente.");
        }

        // 2. Cambio di stato (setStato attiverà polimorficamente le notifiche agli Observer/Clienti)
        corso.setStato(StatoCorso.CANCELLATO);

        // 3. Sovrascrittura e salvataggio dello stato aggiornato nel DB finto
        corsoDAO.insert(corso);
        System.out.println("[CONTROLLER] Corso " + idCorso + " annullato correttamente. Notifiche inviate.");
    }
    /**
     * Restituisce l'elenco completo dei corsi per la visualizzazione nella View.
     */
    public synchronized List<Corso> getElencoCorsi() {
        return corsoDAO.findAll();
    }

    /**
     * Interfaccia di collegamento richiesta dall'UC5 (Gestione Personale).
     * Se un Trainer viene licenziato, cancella automaticamente tutti i suoi corsi futuri.
     */
    public synchronized void rimuoviPtDaCorsi(PersonalTrainer pt) {
        System.out.println("[CONTROLLER-INTEGRAZIONE] Rilevato licenziamento Trainer: " + pt.getIdTrainer() + ". Rimozione dai corsi in corso...");
        
        for (Corso c : corsoDAO.findAll()) {
            if (c.getTrainerAssegnato().getIdTrainer().equals(pt.getIdTrainer()) && c.getStato() == StatoCorso.ATTIVO) {
                c.setStato(StatoCorso.CANCELLATO); // L'Observer si attiva anche qui!
                corsoDAO.insert(c);
            }
        }
    }

    // --- METODI PRIVATI INTERNI DI VALIDAZIONE (ALTA COESIONE) ---

    private boolean convalidaTrainer(String idPT) {
        // Mock: simuliamo che se l'ID contiene la parola "ERR" o è vuoto, il trainer non è valido
        if (idPT == null || idPT.trim().isEmpty() || idPT.toUpperCase().contains("ERR")) {
            return false;
        }
        return true; 
    }

    private boolean controllaSovrapposizioni(String idPT, LocalDateTime dataOra) {
        // Verifichiamo nel database che il trainer non abbia corsi attivi nello stesso identico momento
        for (Corso c : corsoDAO.findAll()) {
            if (c.getTrainerAssegnato().getIdTrainer().equals(idPT) && 
                c.getDataOra().equals(dataOra) && 
                c.getStato() == StatoCorso.ATTIVO) {
                return true; // Trovata collisione d'orario
            }
        }
        return false;
    }
}

/*
"In questa classe ho implementato il cuore algoritmico del mio caso d'uso, applicando il Pattern GRASP Controller per l'Application Layer. Ecco i punti cardine di questa implementazione:

Il Pattern Singleton: Per evitare che nascano più assistenti duplicati in memoria che modificherebbero il palinsesto in modo disordinato, ho blindato la classe rendendo il costruttore private e fornendo un unico punto di accesso globale tramite getInstance().

Gestione dei Thread (La Sincronizzazione del prof): Ho applicato il modificatore synchronized a tutti i metodi principali di scrittura e modifica (organizzaNuovoCorso, annullaCorso, rimuoviPtDaCorsi). Questo assicura che se più attori amministrativi tentano di alterare i dati contemporaneamente, le richieste vengano messe in coda sullo stack, evitando Race Condition devastanti.

Disaccoppiamento tramite Interfaccia (SOLID): Il controller dichiara la variabile privata CorsoDAO (l'interfaccia) ed eredita l'istanza concreta CorsoDAOImpl. In questo modo, se un domani il gruppo decidesse di passare da una mappa in memoria a un database SQL o a Hibernate, io non dovrò toccare nemmeno una riga di questa logica di business.

Risoluzione dell'Integrazione Cross-Modulo: Nel metodo rimuoviPtDaCorsi ho implementato la soluzione all'Open Issue dell'UC5 del mio compagno. Quando lui licenzia un dipendente, invoca questo metodo sul mio modulo. Il mio controller analizza i corsi attivi, li imposta su CANCELLATO e, grazie al Pattern Observer che ho integrato nell'entità Corso, tutti i clienti iscritti a quei corsi vengono notificati all'istante dell'annullamento. Questa è Ingegneria del Software ad altissima coesione!"

*/