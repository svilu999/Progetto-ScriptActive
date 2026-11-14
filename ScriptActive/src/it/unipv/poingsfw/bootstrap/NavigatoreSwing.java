package it.unipv.poingsfw.bootstrap;

import java.util.Objects;

import it.unipv.poingsfw.controller.DashboardClienteController;
import it.unipv.poingsfw.controller.GestoreCorsi;
import it.unipv.poingsfw.controller.GestorePrenotazioni;
import it.unipv.poingsfw.controller.GestoreRegistrazione;
import it.unipv.poingsfw.controller.LoginController;
import it.unipv.poingsfw.controller.Navigatore;
import it.unipv.poingsfw.controller.RinnovoController;
import it.unipv.poingsfw.controller.StoricoAllenamentiController;
import it.unipv.poingsfw.dao.SessioneDAO;
import it.unipv.poingsfw.dao.UtenteDAO;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Direttore;
import it.unipv.poingsfw.view.DashboardClienteView;
import it.unipv.poingsfw.view.DashboardDirettoreView;
import it.unipv.poingsfw.view.LoginView;
import it.unipv.poingsfw.view.PalinsestoCorsiView;
import it.unipv.poingsfw.view.RegistrazioneView;
import it.unipv.poingsfw.view.RinnovoView;
import it.unipv.poingsfw.view.StoricoAllenamentiView;

/**
 * Implementazione Swing del {@link Navigatore}.
 * <p>
 * Per ogni schermata assembla la triade <b>MVC</b>: istanzia la finestra, crea il
 * controller passandogli le dipendenze di cui ha bisogno, collega i due e mostra la
 * finestra. È l'unica classe che conosce contemporaneamente le View concrete e i
 * Controller; né le une né gli altri si istanziano a vicenda.
 * </p>
 * <p>
 * Deve essere usato dal thread degli eventi di Swing (<i>Event Dispatch Thread</i>).
 * </p>
 */
public final class NavigatoreSwing implements Navigatore {

    private final UtenteDAO utenteDAO;
    private final SessioneDAO sessioneDAO;
    private final GestoreCorsi gestoreCorsi;
    private final GestorePrenotazioni gestorePrenotazioni;
    private final GestoreRegistrazione gestoreRegistrazione;

    public NavigatoreSwing(UtenteDAO utenteDAO, SessioneDAO sessioneDAO, GestoreCorsi gestoreCorsi,
                           GestorePrenotazioni gestorePrenotazioni, GestoreRegistrazione gestoreRegistrazione) {
        this.utenteDAO = Objects.requireNonNull(utenteDAO);
        this.sessioneDAO = Objects.requireNonNull(sessioneDAO);
        this.gestoreCorsi = Objects.requireNonNull(gestoreCorsi);
        this.gestorePrenotazioni = Objects.requireNonNull(gestorePrenotazioni);
        this.gestoreRegistrazione = Objects.requireNonNull(gestoreRegistrazione);
    }

    @Override
    public void apriLogin() {
        LoginView view = new LoginView();
        LoginController controller = new LoginController(view, utenteDAO, this);
        view.setController(controller);
        view.setVisible(true);
    }

    @Override
    public void apriRegistrazione() {
        RegistrazioneView view = new RegistrazioneView(gestoreRegistrazione);
        view.onChiusura(this::apriLogin);
        view.setVisible(true);
    }

    @Override
    public void apriRinnovo(String email) {
        RinnovoView view = new RinnovoView();
        RinnovoController controller = new RinnovoController(view, utenteDAO, this, email);
        view.setController(controller);
        view.setVisible(true);
    }

    @Override
    public void apriDashboardCliente(Cliente cliente) {
        DashboardClienteView view = new DashboardClienteView();
        DashboardClienteController controller =
                new DashboardClienteController(view, gestorePrenotazioni, this, cliente);
        view.setController(controller);
        controller.inizializza();
        view.setVisible(true);
    }

    @Override
    public void apriDashboardDirettore(Direttore direttore) {
        DashboardDirettoreView view = new DashboardDirettoreView(gestoreCorsi);
        view.onVisualizzaPalinsesto(this::apriPalinsestoConsultazione);
        view.onGestionePersonale(this::apriGestionePersonale);
        view.setVisible(true);
    }

    @Override
    public void apriPalinsestoCliente(Cliente cliente) {
        PalinsestoCorsiView view = new PalinsestoCorsiView(gestoreCorsi, gestorePrenotazioni, cliente);
        view.onIndietro(() -> apriDashboardCliente(cliente));
        view.setVisible(true);
    }

    @Override
    public void apriPalinsestoConsultazione() {
        /* Finestra aggiuntiva: alla chiusura la schermata di provenienza è ancora aperta */
        new PalinsestoCorsiView(gestoreCorsi, gestorePrenotazioni, null).setVisible(true);
    }

    @Override
    public void apriAreaTrainer() {
        PalinsestoCorsiView view = new PalinsestoCorsiView(gestoreCorsi, gestorePrenotazioni, null);
        view.onIndietro(this::apriLogin);
        view.setVisible(true);
    }

    @Override
    public void apriStoricoAllenamenti(Cliente cliente) {
        StoricoAllenamentiView view = new StoricoAllenamentiView();
        StoricoAllenamentiController controller = new StoricoAllenamentiController(view, sessioneDAO, this);
        view.setController(controller);
        view.setUtenteCorrente(cliente);
        view.setVisible(true);
        view.clickAccediStorico(cliente);
    }

    @Override
    public void apriGestionePersonale() {
        GestionePersonaleBootstrap.apriModulo();
    }
}
