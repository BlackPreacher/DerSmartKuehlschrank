import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Sem on 05.04.2017.
 */
public class DataHistory {

    private ArrayList<Eintrag> history;
    private Eintrag latest = null;

    public DataHistory(){
        history = new ArrayList<>();
    }

    public void addData(String datum, int menge){
        Eintrag neu = new Eintrag(datum, menge);
        history.add(neu);
        if(latest == null || Long.parseLong(datum) > latest.getRawDatum()){
            latest = neu;
        }
    }

    public ArrayList<Eintrag> getHistory(){
        return history;
    }

    public int getNewestMenge(){
        return latest.getMenge();
    }

    public class Eintrag{

        private String datum;
        private int menge;

        public Eintrag(String datum, int menge){
            this.datum = datum;
            this.menge = menge;
        }

        public String getDatum() {
            //Datumsparser
            long timeconvert = Long.parseLong(datum);
            Date time = new Date(timeconvert);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(timeconvert);
        }

        public long getRawDatum(){
            return Long.parseLong(datum);
        }

        public int getMenge() {
            return menge;
        }
    }
}