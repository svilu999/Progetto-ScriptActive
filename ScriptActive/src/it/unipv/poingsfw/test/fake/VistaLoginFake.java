package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.controller.vista.VistaLogin;

/**
 * Vista di login fittizia: nessuna finestra e nessun dialog modale.
 * Registra ciò che il controller le chiede di mostrare, così i test possono verificarlo.
 */
public class VistaLoginFake implements VistaLogin {

    private final List<String> errori = new ArrayList<>();
    private boolean rispostaRinnovo;
    private String messaggioRinnovo;
    private boolean chiusa;

    /** Imposta la risposta che l'utente simulato darà alla proposta di rinnovo. */
    public void setRispostaRinnovo(boolean rispostaRinnovo) {
        this.rispostaRinnovo = rispostaRinnovo;
    }

    public List<String> getErrori() {
        return errori;
    }

    public String getMessaggioRinnovo() {
        return messaggioRinnovo;
    }

    public boolean isChiusa() {
        return chiusa;
    }

    @Override
    public void mostraErrore(String messaggio) {
        errori.add(messaggio);
    }

    @Override
    public boolean chiediConfermaRinnovo(String messaggio) {
        messaggioRinnovo = messaggio;
        return rispostaRinnovo;
    }

    @Override
    public void chiudi() {
        chiusa = true;
    }
}
