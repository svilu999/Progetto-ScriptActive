package it.unipv.posfw.strategy;

public class RetribuzioneFissa implements StrategiaRetribuzione {
    private double stipendioBase;

    public RetribuzioneFissa(double stipendioBase) {
        this.stipendioBase = stipendioBase;
    }

    @Override
    public double calcolaStipendio(int numeroLezioni) {
        return stipendioBase; 
    }

    @Override
    public String getTipoContratto() {
        return "Fisso";
    }
}