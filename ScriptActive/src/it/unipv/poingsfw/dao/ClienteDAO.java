package it.unipv.poingsfw.dao;

import it.unipv.poingsfw.domain.Cliente;

public interface ClienteDAO {

    public boolean inserisciCliente(Cliente c);

    public Cliente getClienteByCF(String codiceFiscale);

    public void updateCliente(Cliente c);

    public void deleteCliente(String codiceFiscale);
}
