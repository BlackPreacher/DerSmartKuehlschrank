/**
 * Created by Hellhero on 23.05.2017.
 */
public class Ware {
    private int menge;
    private Artikel artikel;

    public Ware(int menge, Artikel artikel) {
        this.menge = menge;
        this.artikel = artikel;
    }

    public int getMenge() {
        return menge;
    }

    public int getPreis(){
        return artikel.getPreis();
    }
    public Artikel getArtikel(){
        return artikel;
    }
    public void generateNewPrice(){
        artikel.generateNewPrice();
    }
    public void setPreis(int preis){
        artikel.setPreis(preis);
    }

    public void setMenge(int menge) {
        this.menge = menge;
    }
}
