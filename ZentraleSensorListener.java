import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Sem2 on 23.04.2017.
 */
public class ZentraleSensorListener implements Runnable {

    private DatagramSocket socket;
    private int usedport;
    private ArrayList<Markt> markets = new ArrayList<>();
    private ArrayList<Bestellung> bestellungen = new ArrayList<>();
    private ArrayList<Bestellung> offeneBestellungen = new ArrayList<>();
    private ArrayList<Produkt> alleprodukte;

    public String getBestand() {
        String returnvalue = "HTTP/1.1 200 Ok \r\n Content-type: text/html\r\n \r\n\r\n <html>";
        for (Produkt p : alleprodukte) {
            //returnvalue += p.getBestand();
            returnvalue += "<a href=/" + p.getName() + "> " + p.getName() + "</a>";
            returnvalue += "<p>Aktuelle Menge: " + p.getActualBestand() + " </p>";
        }
        return returnvalue + "</html>";
    }

    public String getBestandVonProdukt(String name) {
        String returnvalue = "HTTP/1.1 200 Ok \r\n Content-type: text/html\r\n \r\n\r\n <html>";
        returnvalue += "<a href=\"/\">Home</a>";
        for (Produkt p : alleprodukte) {
            if (p.getName().equals(name)) {
                returnvalue += p.getBestand();
            }
        }
        return returnvalue + "</html>";
    }

    public String getActualBestandVonProdukt(String name) {
        String returnvalue = "<html><p>Aktuelle Menge: ";
        for (Produkt p : alleprodukte) {
            if (p.getName().equals(name)) {
                returnvalue += p.getActualBestand();
            }
        }
        return returnvalue + "</p></html>";
    }

    public String getBestellungen() {
        String returnvalue = "";
        for(Markt ma: markets){
            returnvalue += "Markt<br>";
            returnvalue += ma.getBestellungen();
            returnvalue += "<br>";
        }

        return returnvalue;
    }

    public ZentraleSensorListener(int port) throws SocketException {
        usedport = port;
        socket = new DatagramSocket(usedport);
        alleprodukte = new ArrayList<>();
    }


    private void receive() throws IOException {
        System.out.println("Starte Sensor Listener auf " + InetAddress.getLocalHost() + ":" + usedport);

        while (true) {
            // Auf Anfrage warten
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);

            // Empfänger auslesen
            byte[] data = packet.getData();

            InetAddress sender = packet.getAddress();

            String message = new String(data, 0, packet.getLength());

            String[] parts = message.split(";");

            String date = "";

            if (!parts[0].equals("order") && !parts[0].equals("market")) {
                try {
                    //Datumsparser
                    long timeconvert = Long.parseLong(parts[1]);
                    Date time = new Date(timeconvert);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    date = sdf.format(timeconvert);
                    System.out.println("Inhalt: " + parts[0] + "\r\n" + "Datum: " + date + "\r\n" + "Menge: " + parts[2] + "\r\n");

                    try {
                        if (!parts[0].equals("order") && !parts[0].equals("market")) {
                            newValue(parts[0], parts[1], Integer.parseInt(parts[2]));

                            // Keine Bestellungen offen - Sende OK
                            if(offeneBestellungen.size() == 0){
                                byte[] sendData = "OK".getBytes();
                                DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, packet.getAddress(), packet.getPort());
                                DatagramSocket toSocket = new DatagramSocket();
                                toSocket.send( sendPacket );
                            } else {
                                // Wenn noch Bestellungen offen sind, prüfe ob der Sensor zu dem Produkt passt und bestelle
                                ArrayList<Bestellung> done = new ArrayList<>();
                                for(Bestellung b: offeneBestellungen){
                                    System.out.println("Artikel: " + parts[0] + " Bestellung: " + b.getProdukt());
                                    if(b.getProdukt().equals(parts[0])){
                                        String returnString = "order;"+Integer.toString(b.getMenge());
                                        byte[] sendData = returnString.getBytes();
                                        DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, packet.getAddress(), packet.getPort());
                                        DatagramSocket toSocket = new DatagramSocket();
                                        toSocket.send( sendPacket );
                                        done.add(b);
                                    }
                                }
                                // Erledigte Bestellung löschen
                                for(Bestellung b: done){
                                    offeneBestellungen.remove(b);
                                }
                            }

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception e) {
                    System.out.println("Fehlerhaftes Paket empfangen\r\n");
                }
            } else if (parts[0].equals("market")) {

                // Anmeldevorgang eines Marktes
                System.out.println("Neuer Markt unter " + sender.toString().replace("/","") + ":" +parts[1]+ " wurde hinzugefuegt!");
                markets.add(new Markt(sender.toString().replace("/",""),parts[1]));

            } else if (parts[0].equals("order")) {

                // Bestellabwicklung
                String artikel = parts[1];
                int menge = Integer.parseInt(parts[2]);
                System.out.println("Bestellung über " + Integer.toString(menge) + " von " + artikel + " erhalten!");
                Bestellung bestellung = bestelle(artikel, menge);

                if(bestellung != null){
                    System.out.println("Preis: " + Integer.toString(bestellung.getPreis()));

                    String returnString = "order;"+Integer.toString(menge);
                    byte[] sendData = returnString.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, packet.getAddress(), packet.getPort());
                    DatagramSocket toSocket = new DatagramSocket();
                    toSocket.send( sendPacket );

                    bestellungen.add(bestellung);

                    offeneBestellungen.remove(bestellung);
                } else {
                    System.out.println("Bestellung nicht möglich, da keine Märkte vorhanden sind!");

                    String returnString = "order;"+Integer.toString(0);
                    byte[] sendData = returnString.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, packet.getAddress(), packet.getPort());
                    DatagramSocket toSocket = new DatagramSocket();
                    toSocket.send( sendPacket );
                }

            } else {
                System.out.println("Fehlerhaftes Paket!");
            }
        }
    }

    public Bestellung bestelle(String artikel, Integer menge){

        int minPreis = 9999;
        Markt cheapestMarket = null;

        if(markets.size() != 0){
            for(Markt ma: markets){
                int preis = ma.getArtikelPreis(artikel);
                if ( preis < minPreis){
                    cheapestMarket = ma;
                    minPreis = preis;
                }
            }
        } else {
            return null;
        }


        cheapestMarket.bestelle(artikel,menge);

        System.out.println("Bestellung " + artikel +" über" + Integer.toString(menge) + " abgewickelt.");

        Bestellung bestellung = new Bestellung(artikel,minPreis,menge);
        offeneBestellungen.add(bestellung);

        return bestellung;

    }




    private void newValue(String produktname, String datum, int menge){
        int habsgefunden = -1;
        for (int i = 0; i < alleprodukte.size(); i++){
            Produkt p = alleprodukte.get(i);

            if(p.getName().equals(produktname)){
                habsgefunden = i;
            }
        }

        if(habsgefunden == -1){
            alleprodukte.add(new Produkt(produktname));
            habsgefunden = alleprodukte.size() - 1;
        }

        alleprodukte.get(habsgefunden).addData(datum, menge);
    }

    public void run(){
        try {
            receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
