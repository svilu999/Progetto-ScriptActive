package it.unipv.posfw.dao;

import it.unipv.posfw.domain.PersonalTrainer;
import java.util.List;

public interface PersonalTrainerDAO {
    void salva(PersonalTrainer pt);
    PersonalTrainer trovaPerId(String idPT);
    void elimina(String idPT);
    void aggiorna(PersonalTrainer pt);
    List<PersonalTrainer> trovaTutti();
}
