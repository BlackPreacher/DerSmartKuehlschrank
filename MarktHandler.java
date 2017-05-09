import org.apache.thrift.TException;

import java.util.*;
import java.io.*;
import java.net.*;

public class MarktHandler implements price.Iface {


	HashMap<String,Integer> preisMapping = new HashMap<>();
	int preis = -1;


	@Override
	public int bestellung(String artikel, int menge) throws TException {
		try{
			System.out.println("Der Artikel " + artikel + " wurde "+Integer.toString(menge)+"x bestellt! Preis: "
					+ Integer.toString(preisMapping.get(artikel)*menge));
			preis = -1;
			return preisMapping.get(artikel) * menge;
		} catch (Exception e){
			artikelPreis(artikel);
			System.out.println("Der Artikel " + artikel + " wurde "+Integer.toString(menge)+"x bestellt! Preis: "
					+ Integer.toString(preisMapping.get(artikel)*menge));
			preis = -1;
			return preisMapping.get(artikel) * menge;
		}
	}

	@Override
	public int artikelPreis(String artikel) throws TException {

		Random rand;
		if(preis == -1){
			rand = new Random();
			preis = rand.nextInt(100);
		}

        System.out.println("Der Artikel " + artikel + " wurde angefragt! Preis: " + Integer.toString(preis));

        preisMapping.put(artikel,preis);

	    return preis;

	}
}
