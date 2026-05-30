package it.unipv.posfw.domain;

// Il contratto per l'entità che viene osservata (il nostro Corso)
public interface Subject {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers();
}