package it.unipv.posfw.domain;

public class DatiForm {
    private String nomeEsercizio;
    private double carichi;
    private int ripetizioni;

    // Costruttore aggiornato (solo 3 parametri)
    public DatiForm(String nomeEsercizio, double carichi, int ripetizioni) {
        this.nomeEsercizio = nomeEsercizio;
        this.carichi = carichi;
        this.ripetizioni = ripetizioni;
    }

    public String getNomeEsercizio() { return nomeEsercizio; }
    public double getCarichi() { return carichi; }
    public int getRipetizioni() { return ripetizioni; }
}
 