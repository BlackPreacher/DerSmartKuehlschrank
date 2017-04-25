import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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

    public String getBestand() {
        String returnvalue = "HTTP/1.1 200 Ok \r\n Content-type: text/html\r\n \r\n\r\n <html>";
        for (Produkt p :alleprodukte) {
            returnvalue += p.getBestand();
            returnvalue += "<p>Aktuelle Menge: " + p.getActualBestand() + " </p>";
            returnvalue += "<a href=/" + p.getName()+"> " + p.getName() + "</a>";
        }
        return returnvalue + "</html>";
    }

    public String getBestandVonProdukt(String name){
        String returnvalue = "HTTP/1.1 200 Ok \r\n Content-type: text/html; charset=utf-8\r\n \r\n\r\n <html>";
        for (Produkt p :alleprodukte) {
            if (p.getName().equals(name)){
                returnvalue += p.getBestand();
            }
        }
        return returnvalue + "</html>";
    }

    public String getActualBestandVonProdukt(String name){
        String returnvalue = "<html><p>Aktuelle Menge: ";
        for (Produkt p :alleprodukte) {
            if (p.getName().equals(name)){
                returnvalue += p.getActualBestand();
            }
        }
        return returnvalue + "</p></html>";
    }

    private ArrayList<Produkt> alleprodukte;


    public ZentraleSensorListener(int port) throws SocketException {
        usedport = port;
        socket = new DatagramSocket(usedport);
        alleprodukte = new ArrayList<>();
    }


    private void receive() throws IOException {
        System.out.println("Starte Sensor Listener auf " + InetAddress.getLocalHost() + ":" + usedport);

        while (true)
        {
            // Auf Anfrage warten
            DatagramPacket packet = new DatagramPacket( new byte[1024], 1024 );
            socket.receive( packet );

            // Empfänger auslesen
            byte[] data = packet.getData();

            String message = new String( data,0, packet.getLength());

            String[] parts = message.split(";");

            String date = "";

            try{
                //Datumsparser
                long timeconvert = Long.parseLong(parts[1]);
                Date time = new Date(timeconvert);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = sdf.format(timeconvert);
                System.out.println("Inhalt: " + parts[0] + "\r\n" + "Datum: " + date + "\r\n" + "Menge: " + parts[2] + "\r\n");

                try {
                    newValue(parts[0], parts[1], Integer.parseInt(parts[2]));
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }catch(Exception e){
                System.out.println("Fehlerhaftes Paket empfangen\r\n");
            }
        }
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
