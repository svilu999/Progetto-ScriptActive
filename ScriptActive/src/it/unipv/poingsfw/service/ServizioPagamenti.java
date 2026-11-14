package it.unipv.poingsfw.service;

import it.unipv.poingsfw.exceptions.PagamentoFallitoException;

/**
 * Porta verso il sistema di pagamento esterno (Payment Gateway).
 * <p>
 * I controller dipendono da questa interfaccia e non da un'implementazione concreta:
 * nei test viene sostituita da un oggetto fittizio, in produzione da un vero gateway
 * bancario, senza modificare il codice che la usa (Dependency Inversion).
 * </p>
 */
public interface ServizioPagamenti {

    /**
     * Richiede l'addebito di un importo sull'IBAN indicato.
     *
     * @param ibanCliente coordinate bancarie del cliente
     * @param importo     cifra in Euro da addebitare
     * @return {@code true} se la transazione è stata autorizzata
     * @throws PagamentoFallitoException se la banca rifiuta la transazione
     */
    boolean elaboraPagamento(String ibanCliente, double importo) throws PagamentoFallitoException;
}
