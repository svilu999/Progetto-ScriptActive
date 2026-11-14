package it.unipv.poingsfw.test.fake;

import java.util.ArrayList;
import java.util.List;

import it.unipv.poingsfw.dao.UtenteDAO;
import it.unipv.poingsfw.domain.Utente;

/**
 * DAO utenti fittizio: restituisce l'utente preconfigurato dal test e registra
 * i rinnovi richiesti, senza alcun accesso al database.
 */
public class UtenteDAOFake implements UtenteDAO {

    /** Rinnovo richiesto tramite {@link #eseguiRinnovo(String, int, String)}. */
    public record Rinnovo(String email, int mesi, String piano) {
    }

    private final List<Rinnovo> rinnovi = new ArrayList<>();
    private Utente utenteDaRestituire;
    private boolean esitoRinnovoSilenzioso;
    private boolean erroreInRinnovo;

    /** Imposta l'utente restituito dal login ({@code null} = credenziali errate). */
    public void setUtenteDaRestituire(Utente utenteDaRestituire) {
        this.utenteDaRestituire = utenteDaRestituire;
    }

    public void setEsitoRinnovoSilenzioso(boolean esitoRinnovoSilenzioso) {
        this.esitoRinnovoSilenzioso = esitoRinnovoSilenzioso;
    }

    /** Se {@code true}, {@link #eseguiRinnovo(String, int, String)} simula un guasto della persistenza. */
    public void setErroreInRinnovo(boolean erroreInRinnovo) {
        this.erroreInRinnovo = erroreInRinnovo;
    }

    public List<Rinnovo> getRinnovi() {
        return rinnovi;
    }

    @Override
    public Utente effettuaLogin(String email, String password) {
        return utenteDaRestituire;
    }

    @Override
    public void registraCliente(String cf, String nome, String cognome, String email, String passwordHash) {
        // Non utilizzato dai test.
    }

    @Override
    public void eseguiRinnovo(String email, int mesiAggiuntivi, String nuovoPiano) {
        if (erroreInRinnovo) {
            throw new RuntimeException("Persistenza non disponibile (simulato).");
        }
        rinnovi.add(new Rinnovo(email, mesiAggiuntivi, nuovoPiano));
    }

    @Override
    public boolean tentaRinnovoSilenzioso(String email) {
        return esitoRinnovoSilenzioso;
    }
}
