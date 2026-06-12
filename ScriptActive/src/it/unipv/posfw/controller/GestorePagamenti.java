package it.unipv.posfw.controller;

import it.unipv.posfw.exceptions.PagamentoFallitoException;

public class GestorePagamenti {
    
    private static GestorePagamenti istanza;
    
    private GestorePagamenti() {
    }
    
    public static GestorePagamenti getIstanza() {
        if (istanza == null) {
            istanza = new GestorePagamenti();
        }
        return istanza;
    }
    
    public boolean elaboraPagamento(String ibanCliente, double importo) throws PagamentoFallitoException {
        System.out.println("[BANCA MOCK] Ricevuta richiesta di addebito di €" + importo + " sull'IBAN: " + ibanCliente);
        

        if (ibanCliente != null && ibanCliente.toUpperCase().contains("ERRORE")) {
            throw new PagamentoFallitoException("Transazione Rifiutata: Credito insufficienti sul conto corrente.");
        }
        
        System.out.println("[BANCA MOCK] Transazione autorizzata con successo!");
        return true;
    }
}
