import java.util.HashMap;
import java.util.Set;

/**
 * Created by Sem2 on 21.05.2017.
 */
public class Angebot {

    HashMap<String,Integer> anbieter = new HashMap<>();

    public Angebot() {
    }

    public void addAngebot(String erzeuger, int preis){
        anbieter.put(erzeuger,preis);
    }

    public String getBillig(){
        Set<String> keys = anbieter.keySet();
        int minpreis = 101;
        String billigsterAnbieter = "";
        for (String key: keys) {
               if(anbieter.get(key) < minpreis){
                   billigsterAnbieter= key;
                   minpreis = anbieter.get(key);
               }
        }
        return billigsterAnbieter;
    }
}
