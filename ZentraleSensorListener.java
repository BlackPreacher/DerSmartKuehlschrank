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

            switch(parts[0]){
                case "market":

                    // Anmeldevorgang eines Marktes
                    System.out.println("Neuer Markt unter " + sender.toString().replace("/","") + ":" +parts[1]+ " wurde hinzugefuegt!");
                    markets.add(new Markt(sender.toString().replace("/",""),parts[1]));

                    break;
                case "update":
                    try {
                        String produktName = parts[1];
                        String datum = parts[2];
                        int aktuellerBestand = Integer.parseInt(parts[3]);
                        int zielBestand = Integer.parseInt(parts[4]);

                        //Datumsparser
                        long timeconvert = Long.parseLong(datum);
                        Date time = new Date(timeconvert);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        date = sdf.format(timeconvert);
                        //System.out.println("Inhalt: " + produktName + "\r\n" + "Datum: " + date + "\r\n" + "Menge: " + aktuellerBestand + "\r\n");

                        try {
                            newValue(produktName, datum, aktuellerBestand);

                            //Bestelle nach, wenn der Sensor zu wenig hat
                            if(aktuellerBestand <= 5){
                                int bestellmenge = zielBestand - aktuellerBestand;

                                int preis = bestelle(produktName, bestellmenge, false);

                                if(preis >= 0){
                                    System.out.println("Preis pro Produkt: " + preis);

                                    String returnString = "order;"+Integer.toString(bestellmenge);
                                    byte[] sendData = returnString.getBytes();
                                    DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, packet.getAddress(), packet.getPort());
                                    DatagramSocket toSocket = new DatagramSocket();
                                    toSocket.send( sendPacket);
                                } else {
                                    System.out.println("Bestellung nicht möglich, da keine Märkte vorhanden sind!");
                                    byte[] sendData = "no_order".getBytes();
                                    DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, packet.getAddress(), packet.getPort());
                                    DatagramSocket toSocket = new DatagramSocket();
                                    toSocket.send( sendPacket );
                                }
                            }else{
                                for (Bestellung b : offeneBestellungen) {
                                    if(b.getProdukt().equals(produktName)){
                                        String returnString = "order;"+b.getMenge();
                                        byte[] sendData = returnString.getBytes();
                                        DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, packet.getAddress(), packet.getPort());
                                        DatagramSocket toSocket = new DatagramSocket();
                                        toSocket.send( sendPacket);
                                        offeneBestellungen.remove(b);
                                        break;
                                    }
                                }

                                byte[] sendData = "no_order".getBytes();
                                DatagramPacket sendPacket = new DatagramPacket( sendData, sendData.length, packet.getAddress(), packet.getPort());
                                DatagramSocket toSocket = new DatagramSocket();
                                toSocket.send( sendPacket );
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception e) {
                        System.out.println("Fehlerhaftes Paket empfangen\r\n");
                    }
                    break;
                default:
                    System.out.println("Test");
                    break;
            }
        }
    }


    public int bestelle(String artikel, int menge, boolean fromWeb){

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
            return -1;
        }


        cheapestMarket.bestelle(artikel,menge);

        Bestellung bestellung = new Bestellung(artikel, minPreis, menge);
        bestellungen.add(bestellung);

        System.out.println("Bestellung von " + menge + "x " + artikel + " abgewickelt.");

        if(fromWeb){
            offeneBestellungen.add(bestellung);
        }

        return minPreis;

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
