package it.unipv.posfw.controller;

import it.unipv.posfw.exceptions.PagamentoFallitoException;

/**
 * La classe {@code GestorePagamenti} funge da <b>Mock Object</b> (oggetto simulato) 
 * per l'integrazione con un sistema bancario esterno (Payment Gateway).
 * <p>
 * Sviluppata all'interno del livello <b>Controller</b> dell'architettura <b>MVC</b>, 
 * questa classe astrae la logica di transazione finanziaria. In un ambiente di produzione 
 * reale comunicherebbe tramite API con una banca, ma in questo contesto di sviluppo 
 * e collaudo (Testing) espone un comportamento deterministico e simulato, permettendo 
 * di validare i flussi di successo e di errore senza effettuare transazioni monetarie reali.
 * </p>
 * <p>
 * <b>Scelte Architetturali:</b><br>
 * Utilizza il pattern <b>Singleton</b> per simulare un unico punto di accesso globale 
 * al servizio di pagamento, ottimizzando le risorse di rete (fittizie) del sistema.
 * </p>
 * * @author Arianna Padula
 * @version 1.0
 */

public class GestorePagamenti {
    
    private static GestorePagamenti istanza;
    
    /**
     * Costruttore privato della classe, necessario per applicare il pattern Singleton.
     * Impedisce l'istanziazione diretta dall'esterno.
     */
    
    private GestorePagamenti() {
    }
    
    /**
     * Punto di accesso globale all'istanza unica della classe (Pattern Singleton).
     * @return L'unica istanza attiva di {@code GestorePagamenti}.
     */
    
    public static GestorePagamenti getIstanza() {
        if (istanza == null) {
            istanza = new GestorePagamenti();
        }
        return istanza;
    }
    
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
    
    public boolean elaboraPagamento(String ibanCliente, double importo) throws PagamentoFallitoException {
        System.out.println("[BANCA SIMULATA] Ricevuta richiesta di addebito di €" + importo + " sull'IBAN: " + ibanCliente);
        
     // Controllo fittizio per innescare i Flussi Alternativi nei Test
        
        if (ibanCliente != null && ibanCliente.toUpperCase().contains("ERRORE")) {
            throw new PagamentoFallitoException("Transazione Rifiutata: Credito insufficienti sul conto corrente.");
        }
        
        System.out.println("[BANCA SIMULATA] Transazione autorizzata con successo!");
        return true;
    }
}
