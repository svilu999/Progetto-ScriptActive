package it.unipv.posfw.test;



import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import it.unipv.posfw.controller.StoricoAllenamentiController;
import it.unipv.posfw.domain.Cliente;
import it.unipv.posfw.domain.DatiFormPojo;
import it.unipv.posfw.domain.TipoAbbonamento;
import it.unipv.posfw.exceptions.*;
import it.unipv.posfw.view.StoricoAllenamentiView;

public class StoricoAllenamentiTest {

    private StoricoAllenamentiController controller;
    private Cliente utenteBase;
    private Cliente utentePremium;
 // FASE DI SETUP
    @Before
    public void initTest() {
                StoricoAllenamentiView viewPerTest = new StoricoAllenamentiView();
        
   it.unipv.posfw.dao.SessioneDAO daoPerTest = new it.unipv.posfw.database.SessioneDAOMySQL();
        
                controller = new StoricoAllenamentiController(viewPerTest, daoPerTest);
        
       //istanzio gli attori
        utenteBase = new Cliente("Mario", "Rossi", "m@m.it", "CF1", TipoAbbonamento.BASE);
        utentePremium = new Cliente("Luigi", "Verdi", "l@l.it", "CF2", TipoAbbonamento.PREMIUM);
    }

    @Test(expected = UtenteNonPremiumException.class)
    public void testUtenteBaseBloccato() throws UtenteNonPremiumException {
        
        controller.gestisciAccessoSezione(utenteBase);
    }

    @Test
    public void testUtentePremiumConsentito() {
        try {
            // proviamo a far accedere l'utente Premium
            controller.gestisciAccessoSezione(utentePremium);
            assertTrue(true); // Se arriva a questa riga senza lanciare eccezioni, il test è superato
        } catch (UtenteNonPremiumException e) {
            fail("Errore UC4: Il sistema ha bloccato un utente Premium!");
        }
    }



    @Test(expected = SchedaVuotaException.class)
    public void testSalvataggioSchedaVuota() throws Exception {
        // SETUP: Creiamo una lista di esercizi vuota
        List<DatiFormPojo> eserciziVuoti = new ArrayList<>();
        Date dataOggi = new Date();

        // L'utente preme "Salva" senza aver aggiunto esercizi
        controller.salvaSessioneCompleta(dataOggi, eserciziVuoti, utentePremium);
    }

   
    @Test
    public void testCasiLimiteDatiAllenamento() {
        // Testiamo l'oggetto DatiForm che modella i parametri quantitativi
        
        // 1. Partizione di equivalenza VALIDA
        try {
            DatiFormPojo valido = new DatiFormPojo("Panca Piana", 50.0, 10);
            assertTrue(valido.getRipetizioni() > 0);
        } catch (Exception e) {
            fail("I dati validi non dovrebbero generare eccezioni.");
        }

        // 2. Partizione NON VALIDA / Caso limite (Ripetizioni negative)
        boolean haLanciatoEccezione = false;
        try {
            // Simuliamo che l'utente inserisca -5 ripetizioni
            DatiFormPojo invalido = new DatiFormPojo("Squat", 100.0, -5); 
          
        } catch (IllegalArgumentException e) {
            haLanciatoEccezione = true;
        }
        
    
    }
}
