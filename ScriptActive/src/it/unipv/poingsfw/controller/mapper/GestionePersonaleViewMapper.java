package it.unipv.poingsfw.controller.mapper;

import java.util.List;

import it.unipv.poingsfw.dto.DatiVisualizzazioneTrainer;

/**
 * Converte i DTO della gestione del personale in dati elementari destinati
 * alla rappresentazione grafica.
 *
 * La classe non contiene regole applicative e non utilizza componenti Swing.
 */
public final class GestionePersonaleViewMapper {

    private GestionePersonaleViewMapper() {
    }

    /**
     * Converte l'elenco dei trainer nelle righe da mostrare nella tabella.
     *
     * @param trainer dati dei trainer
     * @return righe grafiche della tabella
     */
    public static Object[][] creaRigheTrainer(
            List<DatiVisualizzazioneTrainer> trainer) {

        if (trainer == null || trainer.isEmpty()) {
            return new Object[0][0];
        }

        Object[][] righe =
                new Object[trainer.size()][6];

        for (int indice = 0;
                indice < trainer.size();
                indice++) {

            DatiVisualizzazioneTrainer dato =
                    trainer.get(indice);

            righe[indice][0] = dato.getIdTrainer();
            righe[indice][1] = dato.getNomeCompleto();
            righe[indice][2] = dato.getEmail();
            righe[indice][3] = dato.getSpecializzazione();
            righe[indice][4] = dato.getStatoContratto();
            righe[indice][5] =
                    dato.isAttivo() ? "Sì" : "No";
        }

        return righe;
    }

    /**
     * Converte i sostituti nelle descrizioni da mostrare nella combo box.
     *
     * @param sostituti dati dei sostituti
     * @return descrizioni grafiche
     */
    public static String[] creaDescrizioniSostituti(
            List<DatiVisualizzazioneTrainer> sostituti) {

        if (sostituti == null || sostituti.isEmpty()) {
            return new String[0];
        }

        String[] descrizioni =
                new String[sostituti.size()];

        for (int indice = 0;
                indice < sostituti.size();
                indice++) {

            DatiVisualizzazioneTrainer sostituto =
                    sostituti.get(indice);

            descrizioni[indice] =
                    sostituto.getIdTrainer()
                    + " - "
                    + sostituto.getNomeCompleto()
                    + " - "
                    + sostituto.getSpecializzazione();
        }

        return descrizioni;
    }

    /**
     * Recupera l'identificativo associato a un indice grafico.
     *
     * @param trainer elenco visualizzato
     * @param indice indice selezionato
     * @return identificativo del trainer, oppure null
     */
    public static String trovaIdTrainer(
            List<DatiVisualizzazioneTrainer> trainer,
            int indice) {

        if (trainer == null
                || indice < 0
                || indice >= trainer.size()) {

            return null;
        }

        DatiVisualizzazioneTrainer dato =
                trainer.get(indice);

        return dato == null
                ? null
                : dato.getIdTrainer();
    }

    /**
     * Converte il testo dell'importo nel corrispondente valore numerico.
     *
     * @param testoImporto testo inserito nella View
     * @return importo convertito
     * @throws NumberFormatException se il testo non rappresenta un numero
     */
    public static double convertiImporto(
            String testoImporto) {

        if (testoImporto == null) {
            throw new NumberFormatException(
                    "Importo assente."
            );
        }

        String valoreNormalizzato =
                testoImporto.trim().replace(',', '.');

        if (valoreNormalizzato.isBlank()) {
            throw new NumberFormatException(
                    "Importo assente."
            );
        }

        return Double.parseDouble(
                valoreNormalizzato
        );
    }
}