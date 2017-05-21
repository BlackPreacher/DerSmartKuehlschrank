import org.apache.thrift.TException;

import java.util.*;
import java.io.*;
import java.net.*;

public class MarktHandler implements price.Iface {


	HashMap<String,Integer> preisMapping = new HashMap<>();


	@Override
	public int bestellung(String artikel, int menge) throws TException {
		try{
			System.out.println("Der Artikel " + artikel + " wurde "+Integer.toString(menge)+"x bestellt! Preis: "
					+ Integer.toString(preisMapping.get(artikel)*menge));

			preisMapping.remove(artikel);
			preisMapping.put(artikel, generateNewPrice());
			return preisMapping.get(artikel) * menge;

		} catch (Exception e){
			artikelPreis(artikel);
			System.out.println("Der Artikel " + artikel + " wurde "+Integer.toString(menge)+"x bestellt! Preis: "
					+ Integer.toString(preisMapping.get(artikel)*menge));
            preisMapping.remove(artikel);
            preisMapping.put(artikel, generateNewPrice());
			return preisMapping.get(artikel) * menge;

		}
	}

	@Override
	public int artikelPreis(String artikel) throws TException {

		if(!preisMapping.containsKey(artikel)){
            preisMapping.put(artikel, generateNewPrice());
        }

        int preis = preisMapping.get(artikel);
        System.out.println("Der Artikel " + artikel + " wurde angefragt! Preis: " + preis);

	    return preis;

	}

	private int generateNewPrice(){
	    Random rand = new Random();
	    return rand.nextInt(100);
    }
}
