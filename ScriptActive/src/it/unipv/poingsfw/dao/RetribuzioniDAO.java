package it.unipv.poingsfw.dao;

import java.util.List;

import it.unipv.poingsfw.dto.DatiRetribuzioneTrainer;

/**
 * DAO dedicato al recupero dei dati necessari al calcolo delle retribuzioni.
 *
 * L'interfaccia espone solo operazioni di lettura dalla persistenza. Il calcolo
 * della retribuzione resta nel Service.
 */
public interface RetribuzioniDAO {

    /**
     * Recupera i dati retributivi dei Personal Trainer attivi nel mese corrente.
     *
     * Il metodo non calcola il totale delle retribuzioni: recupera solo i dati
     * necessari affinché il Service possa applicare la logica di calcolo.
     *
     * @return lista dei dati retributivi dei Personal Trainer attivi
     */
    List<DatiRetribuzioneTrainer> recuperaDatiRetribuzioniMensili();
}
