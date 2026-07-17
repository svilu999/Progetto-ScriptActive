package it.unipv.poingsfw.service;

import java.util.Objects;

import it.unipv.poingsfw.dao.SwapCorsiDAO;
import it.unipv.poingsfw.exceptions.SostitutoNonValidoException;

/**
 * Service applicativo dedicato allo swap dei corsi tra Personal Trainer.
 *
 * La classe contiene le regole applicative specifiche dello swap, mentre
 * l'accesso al database e la transazione sono delegati al DAO.
 */
public class GestoreSwapCorsi implements ServizioSwapCorsi {

    private final SwapCorsiDAO swapCorsiDAO;

    /**
     * Crea il Service di swap dei corsi.
     *
     * @param swapCorsiDAO DAO utilizzato per la persistenza dello swap
     */
    public GestoreSwapCorsi(SwapCorsiDAO swapCorsiDAO) {
        this.swapCorsiDAO = Objects.requireNonNull(
                swapCorsiDAO,
                "swapCorsiDAO non può essere null"
        );
    }

    /**
     * Verifica se un Personal Trainer possiede corsi attivi o futuri.
     *
     * @param idTrainer identificativo testuale del Personal Trainer
     * @return true se esiste almeno un corso attivo o futuro
     */
    @Override
    public boolean haCorsiAttiviOFuturi(String idTrainer) {
        Integer idTrainerNumerico = convertiIdNumerico(idTrainer);

        if (idTrainerNumerico == null) {
            return false;
        }

        return swapCorsiDAO.esistonoCorsiAttiviOFuturiPerTrainer(
                idTrainerNumerico
        );
    }

    /**
     * Verifica se un Personal Trainer possiede corsi imminenti.
     *
     * @param idTrainer identificativo testuale del Personal Trainer
     * @return true se esiste almeno un corso imminente
     */
    @Override
    public boolean haCorsiImminenti(String idTrainer) {
        Integer idTrainerNumerico = convertiIdNumerico(idTrainer);

        if (idTrainerNumerico == null) {
            return false;
        }

        return swapCorsiDAO.esistonoCorsiImminentiPerTrainer(
                idTrainerNumerico
        );
    }

    /**
     * Sostituisce un Personal Trainer nei corsi e ne completa
     * la disattivazione.
     *
     * Il Service verifica gli identificativi, l'attivazione del sostituto
     * e gli eventuali conflitti orari. Il DAO esegue atomicamente la
     * riassegnazione dei corsi e la disattivazione del vecchio trainer.
     *
     * @param idTrainerDaSostituire identificativo del trainer da sostituire
     * @param idTrainerSostituto identificativo del trainer sostituto
     * @return numero di corsi riassegnati
     * @throws SostitutoNonValidoException se il sostituto non è valido
     */
    @Override
    public int sostituisciTrainerNeiCorsi(
            String idTrainerDaSostituire,
            String idTrainerSostituto)
            throws SostitutoNonValidoException {

        int idVecchioTrainer = convertiIdObbligatorio(
                idTrainerDaSostituire,
                "Identificativo del trainer da sostituire"
        );

        int idNuovoTrainer = convertiIdObbligatorio(
                idTrainerSostituto,
                "Identificativo del trainer sostituto"
        );

        if (idVecchioTrainer == idNuovoTrainer) {
            throw new SostitutoNonValidoException(
                    "Il sostituto non può coincidere "
                    + "con il trainer da sostituire."
            );
        }

        boolean sostitutoAttivo =
                swapCorsiDAO.esisteTrainerConContrattoAttivo(
                        idNuovoTrainer
                );

        if (!sostitutoAttivo) {
            throw new SostitutoNonValidoException(
                    "Il sostituto non esiste "
                    + "oppure non possiede un contratto attivo."
            );
        }

        boolean sovrapposizione =
                swapCorsiDAO.esistonoSovrapposizioniTraCorsi(
                        idVecchioTrainer,
                        idNuovoTrainer
                );

        if (sovrapposizione) {
            throw new SostitutoNonValidoException(
                    "Il sostituto possiede già un corso "
                    + "assegnato nello stesso orario."
            );
        }

        return swapCorsiDAO.riassegnaCorsiEDisattivaTrainer(
                idVecchioTrainer,
                idNuovoTrainer
        );
    }

    /**
     * Converte un identificativo testuale in un numero positivo.
     *
     * @param id identificativo da convertire
     * @return identificativo numerico oppure null se non valido
     */
    private Integer convertiIdNumerico(String id) {
        if (id == null) {
            return null;
        }

        String valoreNormalizzato = id.trim();

        if (!valoreNormalizzato.matches("\\d+")) {
            return null;
        }

        try {
            int idNumerico = Integer.parseInt(valoreNormalizzato);
            return idNumerico > 0 ? idNumerico : null;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converte e valida un identificativo obbligatorio.
     *
     * @param id identificativo da convertire
     * @param nomeCampo nome del campo
     * @return identificativo numerico positivo
     * @throws SostitutoNonValidoException se l'identificativo non è valido
     */
    private int convertiIdObbligatorio(
            String id,
            String nomeCampo)
            throws SostitutoNonValidoException {

        Integer idNumerico = convertiIdNumerico(id);

        if (idNumerico == null) {
            throw new SostitutoNonValidoException(
                    nomeCampo + " non valido."
            );
        }

        return idNumerico;
    }
}