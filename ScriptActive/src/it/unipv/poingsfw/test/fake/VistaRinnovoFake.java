package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.controller.vista.VistaRinnovo;
import it.unipv.poingsfw.domain.LivelloAbbonamento;

/**
 * Vista di rinnovo fittizia: registra le richieste del controller.
 */
public class VistaRinnovoFake implements VistaRinnovo {

    private final List<String> errori = new ArrayList<>();
    private LivelloAbbonamento livelloConfermato;
    private boolean chiusa;

    public List<String> getErrori() {
        return errori;
    }

    public LivelloAbbonamento getLivelloConfermato() {
        return livelloConfermato;
    }

    public boolean isChiusa() {
        return chiusa;
    }

    @Override
    public void mostraRinnovoCompletato(LivelloAbbonamento livello) {
        livelloConfermato = livello;
    }

    @Override
    public void mostraErrore(String messaggio) {
        errori.add(messaggio);
    }

    @Override
    public void chiudi() {
        chiusa = true;
    }
}
