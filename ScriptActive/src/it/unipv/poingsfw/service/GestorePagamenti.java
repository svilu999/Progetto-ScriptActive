package it.unipv.poingsfw.service;

import java.util.logging.Logger;

import it.unipv.poingsfw.exceptions.PagamentoFallitoException;

/**
 * La classe {@code GestorePagamenti} funge da <b>Mock Object</b> (oggetto simulato) 
 * per l'integrazione con un sistema bancario esterno (Payment Gateway).
 * <p>
 * Implementa la porta {@link ServizioPagamenti} e astrae la logica di transazione finanziaria. In un ambiente di produzione 
 * reale comunicherebbe tramite API con una banca, ma in questo contesto di sviluppo 
 * e collaudo (Testing) espone un comportamento deterministico e simulato, permettendo 
 * di validare i flussi di successo e di errore senza effettuare transazioni monetarie reali.
 * </p>
 * <p>
 * <b>Scelte Architetturali:</b><br>
 * Non è un Singleton: l'istanza viene creata una sola volta nel <i>Composition Root</i>
 * e passata via costruttore a chi ne ha bisogno. Chi la usa conosce solo l'interfaccia
 * {@link ServizioPagamenti}, quindi nei test può essere sostituita da un oggetto fittizio.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class GestorePagamenti implements ServizioPagamenti {

    private static final Logger LOG = Logger.getLogger(GestorePagamenti.class.getName());

    /**
     * Simula l'elaborazione di una transazione bancaria (Addebito).
     * <p>
     * <b>Meccanismo di Stubbing per il Testing:</b><br>
     * Il metodo è stato ingegnerizzato per facilitare l'esecuzione dei test di unità. 
     * Implementando una logica condizionale (Stub), se l'IBAN passato come parametro 
     * contiene la parola chiave {@code "ERRORE"}, il sistema innesca artificialmente 
     * il Flusso Alternativo (Transazione Rifiutata). In tutti gli altri casi, simula 
     * una transazione autorizzata con successo.
     * </p>
     * * @param ibanCliente Le coordinate bancarie fornite dall'utente.
     * @param importo     La cifra (in Euro) da addebitare sul conto.
     * @return {@code true}   se la transazione viene elaborata e approvata con successo.
     * @throws PagamentoFallitoException   Se l'IBAN contiene la stringa "ERRORE" o il saldo è fittiziamente insufficiente.
     */
    
    @Override
    public boolean elaboraPagamento(String ibanCliente, double importo) throws PagamentoFallitoException {
        LOG.info("[BANCA SIMULATA] Ricevuta richiesta di addebito di €" + importo);
        
     // Controllo fittizio per innescare i Flussi Alternativi nei Test
        
        if (ibanCliente != null && ibanCliente.toUpperCase().contains("ERRORE")) {
            throw new PagamentoFallitoException("Transazione Rifiutata: Credito insufficienti sul conto corrente.");
        }
        
        LOG.info("[BANCA SIMULATA] Transazione autorizzata con successo.");
        return true;
    }
}
