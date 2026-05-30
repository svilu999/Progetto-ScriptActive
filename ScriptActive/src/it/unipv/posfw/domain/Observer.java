package it.unipv.posfw.domain;

// Il contratto per chiunque debba mettersi in ascolto di notifiche (es. il Cliente)
public interface Observer {
    void update(String messaggio);
}