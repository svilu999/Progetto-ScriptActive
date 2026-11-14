package it.unipv.poingsfw.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unipv.poingsfw.controller.GestoreRegistrazione;
import it.unipv.poingsfw.domain.Abbonamento;
import it.unipv.poingsfw.domain.Cliente;
import it.unipv.poingsfw.domain.LivelloAbbonamento;
import it.unipv.poingsfw.domain.Sede;
import it.unipv.poingsfw.domain.TipoAbbonamento;
import it.unipv.poingsfw.exceptions.DatiRegistrazioneNonValidiException;
import it.unipv.poingsfw.exceptions.PagamentoFallitoException;
import it.unipv.poingsfw.exceptions.SalvataggioFallitoException;
import it.unipv.poingsfw.exceptions.UtenteGiaEsistenteException;
import it.unipv.poingsfw.service.GestorePagamenti;
import it.unipv.poingsfw.service.ServizioPagamenti;
import it.unipv.poingsfw.test.fake.ClienteDAOInMemoria;
import it.unipv.poingsfw.test.fake.SedeDAOInMemoria;

/**
 * Test di unità dello <b>Use Case UC1: Registrazione e Creazione Profilo</b>.
 * <p>
 * Il {@link GestoreRegistrazione} riceve DAO in memoria e un sistema di pagamento
 * simulato: ogni test parte da uno stato pulito, non scrive nel database e non
 * lascia dati residui. La verifica contro MySQL reale è nella suite
 * {@code test.integration.RegistrazioneIntegrationTest}.
 * </p>
 * * @author Arianna Padula
 * @version 2.0
 */
class RegistrazioneTest {

    private static final String IBAN_VALIDO = "IT99Z0123456789012345678901";
    private static final String IBAN_RESPINTO = "IT99Z01234ERRORE5678901";
    private static final String CF = "RZZMRA80A01H501Z";

    private ClienteDAOInMemoria clienteDAO;
    private SedeDAOInMemoria sedeDAO;
    private List<Double> addebiti;
    private GestoreRegistrazione gestoreRegistrazione;
    private Sede sedeDiTest;

    @BeforeEach
    void setUp() {
        clienteDAO = new ClienteDAOInMemoria();
        sedeDAO = new SedeDAOInMemoria();
        sedeDAO.aggiungiSede("Sede Centrale Milano");
        sedeDiTest = sedeDAO.getTutteLeSedi().get(0);

        // Banca simulata che registra gli importi addebitati
        addebiti = new ArrayList<>();
        ServizioPagamenti bancaSimulata = new GestorePagamenti();
        ServizioPagamenti bancaCheRegistra = (iban, importo) -> {
            boolean esito = bancaSimulata.elaboraPagamento(iban, importo);
            addebiti.add(importo);
            return esito;
        };

        gestoreRegistrazione = new GestoreRegistrazione(clienteDAO, sedeDAO, bancaCheRegistra);
    }

    private void registra(String password, String codiceFiscale, TipoAbbonamento tipo,
                          LivelloAbbonamento livello, String iban) throws Exception {
        gestoreRegistrazione.registraNuovoCliente("Mario", "Rozzi", "mario.rozzi@email.it", password,
                codiceFiscale, sedeDiTest, tipo, livello, true, iban);
    }

    // --- MAIN SUCCESS SCENARIO ---

    @Test
    void testRegistrazioneConSuccesso() throws Exception {
        registra("PasswordSicura123!", CF, TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE, IBAN_VALIDO);

        Cliente salvato = clienteDAO.getClienteByCF(CF);
        assertNotNull(salvato, "Il nuovo cliente deve essere stato salvato");
        assertEquals("mario.rozzi@email.it", salvato.getEmail());
        assertEquals(sedeDiTest, salvato.getSedePrincipale());

        Abbonamento abbonamento = salvato.getAbbonamentoAttivo();
        assertNotNull(abbonamento, "Al cliente deve essere associato un abbonamento");
        assertTrue(abbonamento.isRinnovoAutomatico());
        assertTrue(abbonamento.isValidoOggi());

        Calendar traDueMesi = Calendar.getInstance();
        traDueMesi.add(Calendar.MONTH, 2);
        assertTrue(abbonamento.getDataScadenza().before(traDueMesi.getTime()),
                "Un abbonamento mensile scade entro un mese");

        assertEquals(List.of(50.0), addebiti, "Deve essere addebitata la tariffa mensile Base");
    }

    @Test
    void testRegistrazionePremiumAnnuale_ApplicaIlSupplemento() throws Exception {
        registra("PasswordSicura123!", CF, TipoAbbonamento.PREMIUM, LivelloAbbonamento.ANNUALE, IBAN_VALIDO);

        assertEquals(List.of(465.0), addebiti);
        assertTrue(clienteDAO.getClienteByCF(CF).isPremium());
    }

    // --- FLUSSI ALTERNATIVI ---

    @Test
    void testRegistrazioneFallitaPerPagamentoRespinto() {
        assertThrows(PagamentoFallitoException.class,
                () -> registra("PasswordSicura123!", CF, TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE, IBAN_RESPINTO));

        assertEquals(0, clienteDAO.getNumeroClienti(),
                "Se il pagamento è respinto il cliente non deve essere salvato");
    }

    @Test
    void testRegistrazioneFallitaPerUtenteDuplicato() throws Exception {
        registra("Password123!", CF, TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE, IBAN_VALIDO);

        assertThrows(UtenteGiaEsistenteException.class,
                () -> registra("Password123!", CF, TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE, IBAN_VALIDO));

        assertEquals(1, addebiti.size(), "Il duplicato non deve generare un secondo addebito");
    }

    @Test
    void testRegistrazioneFallitaPerPasswordTroppoCorta() {
        assertThrows(DatiRegistrazioneNonValidiException.class,
                () -> registra("12345", CF, TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE, IBAN_VALIDO));
        assertTrue(addebiti.isEmpty(), "Con dati non validi non deve partire alcun addebito");
    }

    @Test
    void testRegistrazioneFallitaPerIbanNonValido() {
        assertThrows(DatiRegistrazioneNonValidiException.class,
                () -> registra("PasswordSicura123!", CF, TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE, "DE123"));
        assertTrue(addebiti.isEmpty());
    }

    @Test
    void testRegistrazioneFallitaPerCampiMancanti() {
        assertThrows(DatiRegistrazioneNonValidiException.class,
                () -> registra("PasswordSicura123!", "  ", TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE, IBAN_VALIDO));
    }

    @Test
    void testRegistrazioneFallitaPerSedeMancante() {
        assertThrows(DatiRegistrazioneNonValidiException.class,
                () -> gestoreRegistrazione.registraNuovoCliente("Mario", "Rozzi", "mario.rozzi@email.it",
                        "PasswordSicura123!", CF, null, TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE,
                        true, IBAN_VALIDO));
    }

    @Test
    void testRegistrazioneFallitaPerErroreDiSalvataggio() {
        clienteDAO.setInserimentoFallisce(true);

        assertThrows(SalvataggioFallitoException.class,
                () -> registra("PasswordSicura123!", CF, TipoAbbonamento.BASE, LivelloAbbonamento.MENSILE, IBAN_VALIDO));
    }

    // --- DATI PER LA VISTA ---

    @Test
    void testGetSediDisponibili_RestituisceLeSediDellaPersistenza() {
        sedeDAO.aggiungiSede("Palestra Roma Nord");

        List<Sede> sedi = gestoreRegistrazione.getSediDisponibili();

        assertEquals(2, sedi.size());
        assertEquals("Sede Centrale Milano", sedi.get(0).getNomeSede());
    }
}
