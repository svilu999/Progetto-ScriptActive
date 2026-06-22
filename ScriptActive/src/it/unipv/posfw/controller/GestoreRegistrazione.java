package it.unipv.posfw.controller;

import java.util.Calendar;

// Imports del DAO
import it.unipv.posfw.dao.ClienteDAO;
import it.unipv.posfw.database.ClienteDAOMySQL;
// Imports del Domain
import it.unipv.posfw.domain.Abbonamento;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.LivelloAbbonamento;
import it.unipv.posfw.domain.Sede;
import it.unipv.posfw.domain.TipoAbbonamento;
// Imports delle Eccezioni
import it.unipv.posfw.exceptions.DatiRegistrazioneNonValidiException;
import it.unipv.posfw.exceptions.PagamentoFallitoException;
import it.unipv.posfw.exceptions.UtenteGiaEsistenteException;

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
 * Applica il pattern creazionale <b>Singleton</b> per garantire che esista una sola istanza 
 * globale del gestore, ottimizzando la memoria della Java Virtual Machine (JVM) durante l'utilizzo dell'applicazione.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class GestoreRegistrazione {

    private static GestoreRegistrazione istanza;
    private ClienteDAO clienteDAO;

    private GestoreRegistrazione() {
        this.clienteDAO = new ClienteDAOMySQL();
    }

    public static GestoreRegistrazione getIstanza() {
        if (istanza == null) {
            istanza = new GestoreRegistrazione();
        }
        return istanza;
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
     */
    
    public void registraNuovoCliente(String nome, String cognome, String email, String password, 
                                     String codiceFiscale, Sede sedeScelta, 
                                     TipoAbbonamento tipo, LivelloAbbonamento livello, 
                                     boolean rinnovoAutomatico, String iban) 
                                     throws UtenteGiaEsistenteException, DatiRegistrazioneNonValidiException, PagamentoFallitoException {
        
        System.out.println("\n--- AVVIO PROCESSO DI REGISTRAZIONE ---");

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
        
        if (password.length() < 6) {
            throw new DatiRegistrazioneNonValidiException("Errore: La password deve contenere almeno 6 caratteri.");
        }

        Cliente clienteEsistente = clienteDAO.getClienteByCF(codiceFiscale);
        if (clienteEsistente != null) {
            throw new UtenteGiaEsistenteException("Errore: Esiste già un profilo associato a questo Codice Fiscale.");
        }

        Cliente nuovoCliente = new Cliente(nome, cognome, email, codiceFiscale, sedeScelta, tipo);
        nuovoCliente.setPassword(password);

        double importo = 0.0;
        if (livello == LivelloAbbonamento.MENSILE) importo = 50.0;
        else if (livello == LivelloAbbonamento.SEMESTRALE) importo = 250.0;
        else if (livello == LivelloAbbonamento.ANNUALE) importo = 450.0;
        
        if (tipo == TipoAbbonamento.PREMIUM) {
            importo += 15.0; 
        }

        GestorePagamenti banca = GestorePagamenti.getIstanza();
        banca.elaboraPagamento(iban, importo); 


       
        System.out.println("[DEBUG GESTORE] Il parametro arrivato al gestore vale: " + rinnovoAutomatico);

        Abbonamento nuovoAbbonamento = new Abbonamento(codiceFiscale, livello, tipo, rinnovoAutomatico, iban);
        
        Calendar cal = Calendar.getInstance();
        if (livello == LivelloAbbonamento.MENSILE) cal.add(Calendar.MONTH, 1);
        else if (livello == LivelloAbbonamento.SEMESTRALE) cal.add(Calendar.MONTH, 6);
        else if (livello == LivelloAbbonamento.ANNUALE) cal.add(Calendar.YEAR, 1);
        
        nuovoAbbonamento.setDataScadenza(cal.getTime());
        nuovoCliente.setAbbonamentoAttivo(nuovoAbbonamento);
        
        clienteDAO.inserisciCliente(nuovoCliente);
        
        System.out.println("[SUCCESS] Abbonamento attivato e Utente salvato nel database!");
    }
}