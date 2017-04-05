import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Hellhero on 05.04.2017.
 */
public class Zentrale {

    private ArrayList<Produkt> alleprodukte;
    private DatagramSocket socket;


    public Zentrale() throws IOException {
        socket = new DatagramSocket(4711);
        alleprodukte = new ArrayList<>();
    }

    public void receive() throws IOException {
        while (true)
        {
            // Auf Anfrage warten
            DatagramPacket packet = new DatagramPacket( new byte[1024], 1024 );
            socket.receive( packet );

            // Empfänger auslesen
            byte[] data = packet.getData();

            String message = new String( data,0, packet.getLength());
            System.out.println(message);
            String[] parts = message.split(";");
            try {
                newValue(parts[0], parts[1], Integer.parseInt(parts[2]));
            }catch(Exception ex){
                ex.printStackTrace();
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


    public static void main (String args[]) throws IOException {

        Zentrale keks = new Zentrale();
        keks.receive();

    }


}
