package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.controller.Navigatore;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Direttore;

/**
 * Navigatore fittizio per i test: non apre finestre, registra soltanto le
 * destinazioni richieste dai controller, nell'ordine in cui sono state chieste.
 */
public class NavigatoreFake implements Navigatore {

    public static final String LOGIN = "LOGIN";
    public static final String REGISTRAZIONE = "REGISTRAZIONE";
    public static final String RINNOVO = "RINNOVO";
    public static final String DASHBOARD_CLIENTE = "DASHBOARD_CLIENTE";
    public static final String DASHBOARD_DIRETTORE = "DASHBOARD_DIRETTORE";
    public static final String PALINSESTO_CLIENTE = "PALINSESTO_CLIENTE";
    public static final String PALINSESTO_CONSULTAZIONE = "PALINSESTO_CONSULTAZIONE";
    public static final String AREA_TRAINER = "AREA_TRAINER";
    public static final String STORICO_ALLENAMENTI = "STORICO_ALLENAMENTI";
    public static final String GESTIONE_PERSONALE = "GESTIONE_PERSONALE";

    private final List<String> destinazioni = new ArrayList<>();
    private String emailRinnovo;
    private Cliente ultimoCliente;

    /** @return tutte le destinazioni richieste, in ordine */
    public List<String> getDestinazioni() {
        return destinazioni;
    }

    /** @return l'ultima destinazione richiesta, oppure {@code null} se non c'è stata navigazione */
    public String getUltimaDestinazione() {
        return destinazioni.isEmpty() ? null : destinazioni.get(destinazioni.size() - 1);
    }

    public String getEmailRinnovo() {
        return emailRinnovo;
    }

    public Cliente getUltimoCliente() {
        return ultimoCliente;
    }

    @Override
    public void apriLogin() {
        destinazioni.add(LOGIN);
    }

    @Override
    public void apriRegistrazione() {
        destinazioni.add(REGISTRAZIONE);
    }

    @Override
    public void apriRinnovo(String email) {
        emailRinnovo = email;
        destinazioni.add(RINNOVO);
    }

    @Override
    public void apriDashboardCliente(Cliente cliente) {
        ultimoCliente = cliente;
        destinazioni.add(DASHBOARD_CLIENTE);
    }

    @Override
    public void apriDashboardDirettore(Direttore direttore) {
        destinazioni.add(DASHBOARD_DIRETTORE);
    }

    @Override
    public void apriPalinsestoCliente(Cliente cliente) {
        ultimoCliente = cliente;
        destinazioni.add(PALINSESTO_CLIENTE);
    }

    @Override
    public void apriPalinsestoConsultazione() {
        destinazioni.add(PALINSESTO_CONSULTAZIONE);
    }

    @Override
    public void apriAreaTrainer() {
        destinazioni.add(AREA_TRAINER);
    }

    @Override
    public void apriStoricoAllenamenti(Cliente cliente) {
        ultimoCliente = cliente;
        destinazioni.add(STORICO_ALLENAMENTI);
    }

    @Override
    public void apriGestionePersonale() {
        destinazioni.add(GESTIONE_PERSONALE);
    }
}
