package it.unipv.poingsfw.service;

import java.util.logging.Logger;

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

    private static final Logger LOG = Logger.getLogger(SistemaPagamento.class.getName());

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
        LOG.info("[SISTEMA ESTERNO BANCA] Ricevuta richiesta di addebito di €" + importo);
        
        if (iban != null && iban.toUpperCase().contains("ERRORE")) {
            LOG.warning("[SISTEMA ESTERNO BANCA] Transazione RIFIUTATA.");
            return false;
        }
        
        LOG.info("[SISTEMA ESTERNO BANCA] Transazione AUTORIZZATA con successo.");
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
        LOG.info("[SISTEMA ESTERNO BANCA] Attivato mandato di addebito diretto (SDD) di €" + importo + ".");
    }
}