package it.unipv.poingsfw.service;

import java.util.Objects;

import it.unipv.poingsfw.dao.SwapCorsiDAO;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;

/**
 * Service applicativo dedicato allo swap dei corsi tra Personal Trainer.
 *
 * La classe contiene le regole applicative dello swap, mentre le operazioni
 * SQL sono delegate al DAO dedicato.
 */
public class GestoreSwapCorsi implements ServizioSwapCorsi {

    private final SwapCorsiDAO swapCorsiDAO;

    /**
     * Crea il service di swap dei corsi.
     *
     * @param swapCorsiDAO DAO usato per accedere ai dati dei corsi
     */
    public GestoreSwapCorsi(SwapCorsiDAO swapCorsiDAO) {
        this.swapCorsiDAO = Objects.requireNonNull(
                swapCorsiDAO,
                "swapCorsiDAO non può essere null"
        );
    }

    /**
     * Verifica se un Personal Trainer ha corsi attivi o futuri.
     *
     * @param idTrainer identificativo testuale del Personal Trainer
     * @return true se esiste almeno un corso attivo o futuro, false altrimenti
     */
    @Override
    public boolean haCorsiAttiviOFuturi(String idTrainer) {
        Integer idTrainerNumerico = estraiIdNumerico(idTrainer);

        if (idTrainerNumerico == null) {
            return false;
        }

        return swapCorsiDAO.esistonoCorsiAttiviOFuturiPerTrainer(idTrainerNumerico);
    }

    /**
     * Verifica se un Personal Trainer ha corsi imminenti.
     *
     * @param idTrainer identificativo testuale del Personal Trainer
     * @return true se esiste almeno un corso imminente, false altrimenti
     */
    @Override
    public boolean haCorsiImminenti(String idTrainer) {
        Integer idTrainerNumerico = estraiIdNumerico(idTrainer);

        if (idTrainerNumerico == null) {
            return false;
        }

        return swapCorsiDAO.esistonoCorsiImminentiPerTrainer(idTrainerNumerico);
    }

    /**
     * Sostituisce un Personal Trainer nei corsi attivi o futuri.
     *
     * Il metodo applica le regole di validazione del sostituto e delega al DAO
     * solo l'aggiornamento effettivo dei corsi nel database.
     *
     * @param idTrainerDaSostituire identificativo del Personal Trainer da sostituire
     * @param idTrainerSostituto identificativo del Personal Trainer sostituto
     * @return numero di corsi aggiornati
     * @throws SostitutoNonValidoException se il sostituto non è valido
     */
    @Override
    public int sostituisciTrainerNeiCorsi(
            String idTrainerDaSostituire,
            String idTrainerSostituto) throws SostitutoNonValidoException {

        Integer idVecchioTrainer = estraiIdNumerico(idTrainerDaSostituire);
        Integer idNuovoTrainer = estraiIdNumerico(idTrainerSostituto);

        if (idVecchioTrainer == null || idNuovoTrainer == null) {
            throw new SostitutoNonValidoException("ID trainer non valido.");
        }

        if (idVecchioTrainer.equals(idNuovoTrainer)) {
            throw new SostitutoNonValidoException(
                    "Il sostituto non può coincidere con il PT da sostituire."
            );
        }

        if (!swapCorsiDAO.esisteTrainerConContrattoAttivo(idNuovoTrainer)) {
            throw new SostitutoNonValidoException(
                    "Il sostituto non esiste o non è attivo."
            );
        }

        if (swapCorsiDAO.esistonoSovrapposizioniTraCorsi(
                idVecchioTrainer,
                idNuovoTrainer)) {

            throw new SostitutoNonValidoException(
                    "OPERAZIONE ANNULLATA: il sostituto ha già un corso assegnato nello stesso orario."
            );
        }

        return swapCorsiDAO.riassegnaCorsiAttiviOFuturi(
                idVecchioTrainer,
                idNuovoTrainer
        );
    }

    /**
     * Estrae la parte numerica da un identificativo testuale.
     *
     * @param id identificativo da convertire
     * @return identificativo numerico, oppure null se il valore non è valido
     */
    private Integer estraiIdNumerico(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        String soloNumeri = id.replaceAll("[^0-9]", "");

        if (soloNumeri.isBlank()) {
            return null;
        }

        return Integer.parseInt(soloNumeri);
    }
}
