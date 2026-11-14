package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.controller.vista.VistaStoricoAllenamenti;
import it.unipv.poingsfw.domain.SessioneAllenamento;

/**
 * Vista fittizia dell'area allenamenti: registra le richieste del controller.
 */
public class VistaStoricoAllenamentiFake implements VistaStoricoAllenamenti {

    private final List<String> avvisi = new ArrayList<>();
    private List<SessioneAllenamento> storicoMostrato;
    private int aggiornamentiStorico;
    private boolean moduloInserimentoMostrato;
    private boolean chiusa;

    public List<String> getAvvisi() {
        return avvisi;
    }

    public List<SessioneAllenamento> getStoricoMostrato() {
        return storicoMostrato;
    }

    /** @return quante volte il controller ha chiesto di ridisegnare lo storico */
    public int getAggiornamentiStorico() {
        return aggiornamentiStorico;
    }

    public boolean isModuloInserimentoMostrato() {
        return moduloInserimentoMostrato;
    }

    public boolean isChiusa() {
        return chiusa;
    }

    @Override
    public void mostraModuloInserimento() {
        moduloInserimentoMostrato = true;
    }

    @Override
    public void mostraStorico(List<SessioneAllenamento> storico) {
        storicoMostrato = storico;
        aggiornamentiStorico++;
    }

    @Override
    public void mostraAvviso(String messaggio) {
        avvisi.add(messaggio);
    }

    @Override
    public void chiudi() {
        chiusa = true;
    }
}
