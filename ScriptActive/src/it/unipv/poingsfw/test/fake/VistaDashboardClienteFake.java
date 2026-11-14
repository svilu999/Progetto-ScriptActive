package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.controller.vista.VistaDashboardCliente;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Corso;

/**
 * Dashboard cliente fittizia: registra le richieste del controller.
 */
public class VistaDashboardClienteFake implements VistaDashboardCliente {

    private final List<String> errori = new ArrayList<>();
    private Cliente clienteMostrato;
    private List<Corso> corsiMostrati;
    private boolean chiusa;

    public List<String> getErrori() {
        return errori;
    }

    public Cliente getClienteMostrato() {
        return clienteMostrato;
    }

    public List<Corso> getCorsiMostrati() {
        return corsiMostrati;
    }

    public boolean isChiusa() {
        return chiusa;
    }

    @Override
    public void impostaDatiCliente(Cliente cliente) {
        clienteMostrato = cliente;
    }

    @Override
    public void mostraCorsiPrenotati(List<Corso> corsi) {
        corsiMostrati = corsi;
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
