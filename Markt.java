/**
 * Created by Hellhero on 06.05.2017.
 */

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TBinaryProtocol;

import java.util.ArrayList;

public class Markt {

    String ip;
    String port;
    ArrayList<Bestellung> bestellungen = new ArrayList<>();

    public Markt(String ip, String port) {

        this.ip = ip;
        this.port = port;
    }

    public String getPort() { return port; }

    public String getIp() { return ip; }

    public int getArtikelPreis(String artikel) {
        // Apache Thrift RPC zur Preisabfrage
        try {
            TTransport transport;

            transport = new TSocket(ip, Integer.parseInt(port));
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport); //Protocol fuer den Client festlegen
            price.Client marktClient = new price.Client(protocol); //Protocol dem Client zuweisen

            int preis = marktClient.artikelPreis(artikel);

            transport.close();

            return preis;

        } catch (TException x) {
            System.out.println(x);
            return -1;
        }
    }

    public String getBestellungen() {

        //String returnvalue = "HTTP/1.1 200 Ok \r\n Content-type: text/html; charset=utf-8\r\n \r\n\r\n <html>";
        String returnvalue = "<html>";
        //returnvalue += "<a href=\"/\">Home</a><br>";
        returnvalue += "Bestellungen: <br>";
        returnvalue += "<table border=1 ><th>Produkt</th><th>Menge</th><th>Preis</th>";
        for (Bestellung b : bestellungen) {
            returnvalue = returnvalue + "<tr><td>" + b.getProdukt() + "</td><td>" +b.getMenge()+ "</td><td>" + b.getPreis() + "</td></tr>";
        }
        returnvalue += "</table>";
        return returnvalue + "</html>";
    }

    public int bestelle(String artikel, int menge) {
        // Apache Thrift RPC zur Bestellung
        try {
            TTransport transport;

            transport = new TSocket(ip, Integer.parseInt(port));
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport); //Protocol fuer den Client festlegen
            price.Client marktClient = new price.Client(protocol); //Protocol dem Client zuweisen

            int preis = marktClient.bestellung(artikel, menge);

            bestellungen.add(new Bestellung(artikel,preis,menge));


            transport.close();

            return preis;

        } catch (TException x) {
            System.out.println(x);
            return -1;
        }
    }
}
