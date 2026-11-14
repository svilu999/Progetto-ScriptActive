package it.unipv.poingsfw.controller;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import it.unipv.poingsfw.dao.ClienteDAO;
import it.unipv.poingsfw.dao.SedeDAO;
import it.unipv.poingsfw.domain.Abbonamento;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.LivelloAbbonamento;
import it.unipv.poingsfw.domain.Sede;
import it.unipv.poingsfw.domain.Tariffario;
import it.unipv.poingsfw.domain.TipoAbbonamento;
import it.unipv.poingsfw.exceptions.DatiRegistrazioneNonValidiException;
import it.unipv.poingsfw.exceptions.PagamentoFallitoException;
import it.unipv.poingsfw.exceptions.SalvataggioFallitoException;
import it.unipv.poingsfw.exceptions.UtenteGiaEsistenteException;
import it.unipv.poingsfw.service.ServizioPagamenti;

/**
 * La classe {@code GestoreRegistrazione} agisce come <b>Controller</b> nel pattern architetturale <b>MVC (Model-View-Controller) per 
 * la gestione del dominio di iscrizione.
 * <p>
 * Questa classe implementa la logica applicativa dello <b>Use Case UC1: Registrazione e Creazione Profilo</b>.
 * Agisce da intermediario: riceve i dati di input dalla Vista (l'interfaccia grafica), li valida,
 * e coordina il Modello (creando le entità {@link Cliente} e {@link Abbonamento}). 
 * Infine, delega il salvataggio dei dati al livello di persistenza (DAO).
 * </p>
 * <p>
 * <b>Scelte Architetturali:</b><br>
 * Le dipendenze verso la persistenza ({@link ClienteDAO}, {@link SedeDAO}) e verso il
 * sistema di pagamento ({@link ServizioPagamenti}) arrivano dal costruttore
 * (<i>Dependency Injection</i>): il gestore conosce solo interfacce e può essere
 * collaudato con oggetti fittizi, senza database. L'istanza è creata una sola volta
 * nel Composition Root, quindi non serve un Singleton.
 * </p>
 * * @author Arianna Padula
 * @version 2.0
 */

public class GestoreRegistrazione {

    private static final Logger LOG = Logger.getLogger(GestoreRegistrazione.class.getName());

    private static final int LUNGHEZZA_MINIMA_PASSWORD = 6;
    private static final int LUNGHEZZA_MINIMA_IBAN = 15;

    private final ClienteDAO clienteDAO;
    private final SedeDAO sedeDAO;
    private final ServizioPagamenti servizioPagamenti;

    public GestoreRegistrazione(ClienteDAO clienteDAO, SedeDAO sedeDAO, ServizioPagamenti servizioPagamenti) {
        this.clienteDAO = Objects.requireNonNull(clienteDAO, "clienteDAO non può essere null");
        this.sedeDAO = Objects.requireNonNull(sedeDAO, "sedeDAO non può essere null");
        this.servizioPagamenti = Objects.requireNonNull(servizioPagamenti, "servizioPagamenti non può essere null");
    }

    /**
     * Restituisce le sedi tra cui il nuovo cliente può scegliere.
     * La Vista le riceve dal Controller e non interroga mai direttamente la persistenza.
     *
     * @return l'elenco delle sedi disponibili
     */
    public List<Sede> getSediDisponibili() {
        return sedeDAO.getTutteLeSedi();
    }

    /**
     * Gestisce il <i>Main Success Scenario</i> e le deviazioni (Alternative Flows) dello Use Case UC1.
     * <p>
     * * </p>
     * * @param nome                 Il nome anagrafico fornito in fase di input.
     * @param cognome              Il cognome fornito in fase di input.
     * @param email                Il recapito telematico.
     * @param password             La chiave di cifratura per l'autenticazione futura.
     * @param codiceFiscale        La chiave naturale univoca di dominio.
     * @param sedeScelta           L'oggetto {@link Sede} associato geograficamente al cliente.
     * @param tipo                 L'enumerazione indicante il tipo di abbonamento (BASE, PREMIUM).
     * @param livello              L'enumerazione indicante la ricorrenza temporale (MENSILE, SEMESTRALE, ANNUALE).
     * @param rinnovoAutomatico    Flag booleano per l'addebito ricorrente.
     * @param iban                 Le coordinate bancarie per l'espletamento finanziario.
     * @throws UtenteGiaEsistenteException       Se il DAO rintraccia un record duplicato in fase pre-inserimento.
     * @throws DatiRegistrazioneNonValidiException      Se viene violata l'integrità dei dati o le regole di sicurezza (es. password corta).
     * @throws PagamentoFallitoException        (Alternative Flow 1).
     * @throws SalvataggioFallitoException      Se il livello di persistenza non conferma l'inserimento del nuovo profilo.
     */
    
    public void registraNuovoCliente(String nome, String cognome, String email, String password, 
                                     String codiceFiscale, Sede sedeScelta, 
                                     TipoAbbonamento tipo, LivelloAbbonamento livello, 
                                     boolean rinnovoAutomatico, String iban) 
                                     throws UtenteGiaEsistenteException, DatiRegistrazioneNonValidiException,
                                            PagamentoFallitoException, SalvataggioFallitoException {
        
        if (nome == null || nome.trim().isEmpty() ||
            cognome == null || cognome.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            codiceFiscale == null || codiceFiscale.trim().isEmpty() ||
            iban == null || iban.trim().isEmpty()) {
            throw new DatiRegistrazioneNonValidiException("Errore: Tutti i campi testuali sono obbligatori.");
        }
        
        if (sedeScelta == null) {
            throw new DatiRegistrazioneNonValidiException("Errore: Selezionare una sede di riferimento.");
        }
        
        if (password.length() < LUNGHEZZA_MINIMA_PASSWORD) {
            throw new DatiRegistrazioneNonValidiException("Errore: La password deve contenere almeno 6 caratteri.");
        }

        String ibanNormalizzato = iban.trim();
        if (ibanNormalizzato.length() < LUNGHEZZA_MINIMA_IBAN || !ibanNormalizzato.toUpperCase().startsWith("IT")) {
            throw new DatiRegistrazioneNonValidiException(
                    "Errore: L'IBAN inserito non è nel formato valido. Deve iniziare con 'IT' ed avere almeno 15 caratteri.");
        }

        Cliente clienteEsistente = clienteDAO.getClienteByCF(codiceFiscale);
        if (clienteEsistente != null) {
            throw new UtenteGiaEsistenteException("Errore: Esiste già un profilo associato a questo Codice Fiscale.");
        }

        Cliente nuovoCliente = new Cliente(nome, cognome, email, codiceFiscale, sedeScelta, tipo);
        nuovoCliente.setPassword(password);

        double importo = Tariffario.calcolaImporto(tipo, livello);
        servizioPagamenti.elaboraPagamento(ibanNormalizzato, importo);

        Abbonamento nuovoAbbonamento = new Abbonamento(codiceFiscale, livello, tipo, rinnovoAutomatico, ibanNormalizzato);
        
        Calendar cal = Calendar.getInstance();
        if (livello != null) {
            cal.add(Calendar.MONTH, livello.getMesi());
        }
        
        nuovoAbbonamento.setDataScadenza(cal.getTime());
        nuovoCliente.setAbbonamentoAttivo(nuovoAbbonamento);
        
        boolean salvato = clienteDAO.inserisciCliente(nuovoCliente);
        if (!salvato) {
            throw new SalvataggioFallitoException(
                    "Errore: impossibile salvare il profilo. Verificare che l'email non sia già registrata.");
        }

        LOG.info("Registrazione completata: abbonamento attivato e cliente salvato.");
    }
}