import javax.print.DocFlavor;

/**
 * Created by Hellhero on 06.05.2017.
 */
public class Bestellung {

    private String produkt;
    private int preis;
    private  int menge;

    public int getMenge() {
        return menge;
    }

    public Bestellung(String produkt, int preis, int menge) {
        this.preis = preis;
        this.produkt = produkt;
        this.menge = menge;

    }

    public String getProdukt() {
        return produkt;
    }

    public int getPreis() {
        return preis;
    }
}
