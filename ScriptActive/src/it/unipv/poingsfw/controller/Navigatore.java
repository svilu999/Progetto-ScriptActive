package it.unipv.poingsfw.controller;

import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.Direttore;

/**
 * Astrazione della navigazione tra le schermate dell'applicazione.
 * <p>
 * I controller decidono <b>quando</b> cambiare schermata (è logica applicativa), ma non
 * sanno <b>come</b> una schermata viene costruita: non istanziano finestre, DAO o altri
 * controller. L'implementazione concreta vive nel <i>Composition Root</i>
 * (package {@code bootstrap}), l'unico punto del sistema che conosce le classi Swing e
 * le implementazioni MySQL.
 * </p>
 * <p>
 * Nei test l'interfaccia viene sostituita da un oggetto fittizio che registra la
 * destinazione richiesta: la navigazione si verifica così senza aprire alcuna finestra.
 * </p>
 */
public interface Navigatore {

    /** Mostra la schermata di autenticazione. */
    void apriLogin();

    /** Mostra il modulo di registrazione di un nuovo cliente (UC1). */
    void apriRegistrazione();

    /** Mostra la schermata di rinnovo per il cliente con l'email indicata. */
    void apriRinnovo(String email);

    /** Mostra la dashboard del cliente autenticato. */
    void apriDashboardCliente(Cliente cliente);

    /** Mostra la dashboard del direttore autenticato. */
    void apriDashboardDirettore(Direttore direttore);

    /** Mostra il palinsesto dal quale il cliente può prenotare o annullare i corsi. */
    void apriPalinsestoCliente(Cliente cliente);

    /** Mostra il palinsesto in sola consultazione, in una finestra aggiuntiva. */
    void apriPalinsestoConsultazione();

    /** Mostra il palinsesto in sola consultazione come area riservata del Personal Trainer. */
    void apriAreaTrainer();

    /** Mostra l'area Premium con lo storico degli allenamenti del cliente (UC4). */
    void apriStoricoAllenamenti(Cliente cliente);

    /** Mostra il modulo di gestione dei contratti del personale. */
    void apriGestionePersonale();
}
