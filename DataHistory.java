import java.util.ArrayList;

/**
 * Created by Sem on 05.04.2017.
 */
public class DataHistory {

    private ArrayList<Eintrag> history;

    public DataHistory(){
        history = new ArrayList<>();
    }

    public void addData(String datum, int menge){
        history.add(new Eintrag(datum, menge));
    }

    public class Eintrag{

        private String datum;
        private int menge;

        public Eintrag(String datum, int menge){
            this.datum = datum;
            this.menge = menge;
        }

        public String getDatum() {
            return datum;
        }

        public int getMenge() {
            return menge;
        }
    }
}