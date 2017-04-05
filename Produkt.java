/**
 * Created by Sem on 05.04.2017.
 */
public class Produkt {

    private String name;
    private DataHistory history;

    public Produkt(String name){
        this.name = name;
        history = new DataHistory();
    }

    public void addData(String datum, int menge){
        history.addData(datum, menge);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Produkt){
            Produkt p = (Produkt) obj;
            if(p.getName().equals(name))
                return true;
        }
        return false;
    }


}
