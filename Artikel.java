import java.util.Random;

/**
 * Created by Hellhero on 21.05.2017.
 */
public class Artikel {

    String name;
    int preis;

    public Artikel(String name) {
        generateNewPrice();
        this.name = name;
    }

    public void generateNewPrice(){
        Random rand = new Random();
        preis = rand.nextInt(100);
    }

    public void setPreis(int preis){
        this.preis = preis;
    }

    public int getPreis() {
        return preis;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.toString().equals(getName());
    }
}
