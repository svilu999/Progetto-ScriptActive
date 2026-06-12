package it.unipv.posfw.controller;

import java.util.Calendar;

// Imports del DAO
import it.unipv.posfw.dao.ClienteDAO;
import it.unipv.posfw.database.ClienteDAOMySQL;
// Imports del Domain
import it.unipv.posfw.domain.Abbonamento;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.LivelloAbbonamento;
import it.unipv.posfw.domain.Pagamento;
import it.unipv.posfw.domain.Sede;
import it.unipv.posfw.domain.TipoAbbonamento;
// Imports delle Eccezioni
import it.unipv.posfw.exceptions.DatiRegistrazioneNonValidiException;
import it.unipv.posfw.exceptions.PagamentoFallitoException;
import it.unipv.posfw.exceptions.UtenteGiaEsistenteException;

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

    public void registraNuovoCliente(String nome, String cognome, String email, String password, 
                                     String codiceFiscale, Sede sedeScelta, 
                                     TipoAbbonamento tipo, LivelloAbbonamento livello, 
                                     String iban) 
                                     throws UtenteGiaEsistenteException, DatiRegistrazioneNonValidiException, PagamentoFallitoException {
        
        System.out.println("\n--- AVVIO PROCESSO DI REGISTRAZIONE ---");

        // 1. Controllo validità campi testuali
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
        System.out.println("Campi compilati correttamente.");

        // 2. Controllo duplicati nel database
        Cliente clienteEsistente = clienteDAO.getClienteByCF(codiceFiscale);
        if (clienteEsistente != null) {
            throw new UtenteGiaEsistenteException("Errore: Esiste già un profilo associato a questo Codice Fiscale.");
        }
        System.out.println("Il codice fiscale è nuovo. Nessun duplicato.");

        // 3. Creazione Cliente in memoria (CODICE PULITO, VIA IL TRUCCO!)
        Cliente nuovoCliente = new Cliente(nome, cognome, email, codiceFiscale, sedeScelta, tipo);
        nuovoCliente.setPassword(password);
        System.out.println("Istanza Cliente temporanea creata in memoria.");

        // 4. Calcolo dell'importo dell'abbonamento (LOGICA CORRETTA SULLE ENUM)
        double importo = 0.0;
        if (livello == LivelloAbbonamento.MENSILE) importo = 50.0;
        else if (livello == LivelloAbbonamento.SEMESTRALE) importo = 250.0;
        else if (livello == LivelloAbbonamento.ANNUALE) importo = 450.0;
        
        if (tipo == TipoAbbonamento.PREMIUM) {
            importo += 15.0; 
        }

        // 5. Elaborazione del Pagamento
        GestorePagamenti banca = GestorePagamenti.getIstanza();
        banca.elaboraPagamento(iban, importo); 
        System.out.println("[OK] Transazione bancaria completata con successo.");

        // ---> INIZIO CODICE INSERITO: Creazione ricevuta di pagamento <---
        Pagamento ricevuta = new Pagamento(importo, "SUCCESS", iban);
        System.out.println("Ricevuta di pagamento creata per l'importo di €" + importo);
        // ---> FINE CODICE INSERITO <---

        // 6. Creazione e configurazione dell'Abbonamento
        // Allineato al costruttore: (String, LivelloAbbonamento, TipoAbbonamento, boolean, String)
        Abbonamento nuovoAbbonamento = new Abbonamento(codiceFiscale, livello, tipo, true, iban);
        
        Calendar cal = Calendar.getInstance();
        if (livello == LivelloAbbonamento.MENSILE) cal.add(Calendar.MONTH, 1);
        else if (livello == LivelloAbbonamento.SEMESTRALE) cal.add(Calendar.MONTH, 6);
        else if (livello == LivelloAbbonamento.ANNUALE) cal.add(Calendar.YEAR, 1);
        
        nuovoAbbonamento.setDataScadenza(cal.getTime());
        nuovoCliente.setAbbonamentoAttivo(nuovoAbbonamento);
        
        // 7. Salvataggio su Database (eseguito SOLO se il pagamento non ha lanciato eccezioni)
        clienteDAO.inserisciCliente(nuovoCliente);
        
        System.out.println("[SUCCESS] Abbonamento attivato e Utente salvato nel database!");
        System.out.println("Procedura di iscrizione terminata correttamente. Reindirizzamento alla View di Login.");
    }
}