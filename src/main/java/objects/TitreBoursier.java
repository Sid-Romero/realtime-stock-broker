package objects;

public class TitreBoursier {
   private String code;       // stock code
    private String nom;        // society name
    private double valeur;
    private String unite;

    // for after
    private double variation;
    private double variationPct;

    public TitreBoursier(String code, String nom, double valeur, String unite) {
        this.code = code;
        this.nom = nom;
        this.valeur = valeur;
        this.unite = unite;
        this.variation = 0.0;
        this.variationPct = 0.0;
    }

    @Override
    public String toString() {
        return code + " (" + nom + ") @ " + valeur + " " + unite +
            " (Δ " + variation + ", " + variationPct + "%)";
    }



    // getters/setters
    public String getCode() { return code; }
    public String getNom() { return nom; }
    public double getValeur() { return valeur; }
    public String getUnite() { return unite; }
    public double getVariation() { return variation; }
    public double getVariationPct() { return variationPct; }

}
