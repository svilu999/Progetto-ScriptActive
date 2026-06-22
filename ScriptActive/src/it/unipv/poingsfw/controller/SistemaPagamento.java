package it.unipv.poingsfw.controller;

/**
 * Questa classe rappresenta il sistema bancario esterno (es. Stripe, PayPal, Nexi).
 * In un progetto reale conterrebbe le API di comunicazione, qui viene svolta una simulazione.
 */
public class SistemaPagamento {

    public SistemaPagamento() {
    }

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


    public void registraAddebitoPeriodico(String iban, double importo) {
        System.out.println("[SISTEMA ESTERNO BANCA] Attivazione mandato di addebito diretto (SDD).");
        System.out.println("[SISTEMA ESTERNO BANCA] Verranno addebitati €" + importo + " periodicamente sull'IBAN: " + iban);
    }
}