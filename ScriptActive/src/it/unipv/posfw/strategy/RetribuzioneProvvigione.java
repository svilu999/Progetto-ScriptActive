package it.unipv.posfw.strategy;

public class RetribuzioneProvvigione implements StrategiaRetribuzione {
    private double quotaPerLezione;

    public RetribuzioneProvvigione(double quotaPerLezione) {
        this.quotaPerLezione = quotaPerLezione;
    }

    @Override
    public double calcolaStipendio(int numeroLezioni) {
        return quotaPerLezione * numeroLezioni;
    }

    @Override
    public String getTipoContratto() {
        return "Provvigione";
    }
}
