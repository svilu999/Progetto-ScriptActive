package it.unipv.poingsfw.bootstrap;

import javax.swing.SwingUtilities;

import it.unipv.poingsfw.controller.GestorePersonale;
import it.unipv.poingsfw.dao.PersonalTrainerDAO;
import it.unipv.poingsfw.database.PersonalTrainerDAOMySQL;
import it.unipv.poingsfw.service.GestoreContrattiPersonale;
import it.unipv.poingsfw.service.ServizioContrattiPersonale;
import it.unipv.poingsfw.service.ServizioRetribuzioni;
import it.unipv.poingsfw.service.ServizioSwapCorsi;
import it.unipv.poingsfw.dao.SwapCorsiDAO;
import it.unipv.poingsfw.database.SwapCorsiDAOMySQL;
import it.unipv.poingsfw.service.GestoreSwapCorsi;
import it.unipv.poingsfw.view.GestionePersonaleView;
import it.unipv.poingsfw.dao.RetribuzioniDAO;
import it.unipv.poingsfw.service.GestoreRetribuzioni;
import it.unipv.poingsfw.database.RetribuzioniDAOMySQL;
import it.unipv.poingsfw.dao.DirettoreDAO;
import it.unipv.poingsfw.database.DirettoreDAOMySQL;

/**
 * Classe di bootstrap del modulo di gestione del personale.
 *
 * Si occupa solo di creare e collegare View, Controller, Service e componenti
 * concreti necessari all'avvio del modulo.
 */
public final class GestionePersonaleBootstrap {

    /**
     * Costruttore privato per impedire la creazione di oggetti della classe.
     */
    private GestionePersonaleBootstrap() {
    }

    /**
     * Apre il modulo di gestione del personale.
     */
    public static void apriModulo() {
        if (SwingUtilities.isEventDispatchThread()) {
            creaEMostraModulo();
        } else {
            SwingUtilities.invokeLater(GestionePersonaleBootstrap::creaEMostraModulo);
        }
    }

    /**
     * Crea le dipendenze del modulo e mostra la finestra.
     */
    private static void creaEMostraModulo() {
        PersonalTrainerDAO trainerDAO = new PersonalTrainerDAOMySQL();
        
        DirettoreDAO direttoreDAO = new DirettoreDAOMySQL();

        SwapCorsiDAO swapCorsiDAO = new SwapCorsiDAOMySQL();
        ServizioSwapCorsi servizioSwapCorsi = new GestoreSwapCorsi(swapCorsiDAO);

        RetribuzioniDAO retribuzioniDAO = new RetribuzioniDAOMySQL();
        ServizioRetribuzioni servizioRetribuzioni = new GestoreRetribuzioni(retribuzioniDAO);

        ServizioContrattiPersonale servizioContratti = new GestoreContrattiPersonale(
                trainerDAO,
                direttoreDAO,
                servizioSwapCorsi,
                servizioRetribuzioni
        );

        GestionePersonaleView view = new GestionePersonaleView();

        new GestorePersonale(view, servizioContratti);

        view.setVisible(true);
    }

}
