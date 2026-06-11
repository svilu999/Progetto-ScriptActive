package it.unipv.posfw.strategy;

public interface StrategiaRetribuzione {
    double calcolaStipendio(int numeroLezioni);
    String getTipoContratto();
}