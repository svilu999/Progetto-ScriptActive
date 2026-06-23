package it.unipv.poingsfw.controller;

/**
 * La classe {@code SistemaPagamento} funge da <b>Mock Object</b> (oggetto simulato) per 
 * l'integrazione con un Gateway di pagamento esterno (es. Stripe, PayPal, Nexi).
 * <p>
 * In un ambiente di produzione reale, questa classe conterrebbe la logica di rete e le 
 * chiamate API verso i server dell'istituto di credito. Nel contesto attuale 
 * di sviluppo e validazione del prototipo, implementa un comportamento deterministico (Stub), 
 * consentendo di testare i flussi applicativi (sia di successo che di errore) senza 
 * effettuare transazioni monetarie reali.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class SistemaPagamento {

    public SistemaPagamento() {
    }
    
    /**
     * Simula il processo di autorizzazione di una transazione singola (es. pagamento con carta).
     * <p>
     * <b>Logica di Stubbing:</b> Il metodo intercetta la stringa dell'IBAN. Se contiene 
     * la parola chiave {@code "ERRORE"}, innesca artificialmente il flusso alternativo 
     * (Transazione Rifiutata). Altrimenti, simula un'approvazione bancaria.
     * </p>
     * * @param iban    Le coordinate bancarie (o token carta) fornite dal cliente.
     * @param importo La somma economica da addebitare.
     * @return {@code true} se il sistema bancario simulato autorizza l'addebito, {@code false} altrimenti.
     */

    public boolean autorizzaTransazione(String iban, double importo) {
        System.out.println("[SISTEMA ESTERNO BANCA] Ricevuta richiesta di addebito di €" + importo);
        System.out.println("[SISTEMA ESTERNO BANCA] Verifica in corso sull'IBAN: " + iban);
        
        if (iban != null && iban.toUpperCase().contains("ERRORE")) {
            System.out.println("[SISTEMA ESTERNO BANCA] Errore: Transazione RIFIUTATA.");
            return false;
        }
        
        System.out.println("[SISTEMA ESTERNO BANCA] Stato: Transazione AUTORIZZATA con successo.");
        return true;
    }

    /**
     * Simula la registrazione di un mandato per l'addebito automatico e ricorrente.
     * <p>.
     * </p>
     * * @param iban    L'IBAN su cui attivare il mandato di addebito continuo.
     * @param importo L'importo concordato per i futuri addebiti periodici.
     */
    
    public void registraAddebitoPeriodico(String iban, double importo) {
        System.out.println("[SISTEMA ESTERNO BANCA] Attivazione mandato di addebito diretto (SDD).");
        System.out.println("[SISTEMA ESTERNO BANCA] Verranno addebitati €" + importo + " periodicamente sull'IBAN: " + iban);
    }
}