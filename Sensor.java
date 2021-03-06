import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Sem on 05.04.2017.
 */
public class Sensor {

    private String ip;
    private int usedport;
    private String produkt;
    private int bestand;
    private int zielBestand;

    public Sensor(String produkt, String ip, String port){
        this.ip = ip;
        usedport = Integer.parseInt(port);
        this.produkt = produkt;
        bestand =  20;
        zielBestand = 20;
    }

    public Sensor(){
        ip = "localhost";
        usedport = 4711;
        produkt = "NULL";
        bestand =  20;
        zielBestand = 20;
    }

    private void send(int menge) throws IOException {
        String sendstring = "update;" + produkt + ";" + String.valueOf(System.currentTimeMillis()) + ";" + menge + ";" + zielBestand;

        System.out.println(sendstring);

        InetAddress ia = InetAddress.getByName(ip);
        byte[] data = sendstring.getBytes();
        DatagramPacket packet = new DatagramPacket( data, data.length, ia, usedport );
        DatagramSocket toSocket = new DatagramSocket();
        toSocket.send( packet );

        //NEW
        // Auf Antwort warten
        DatagramPacket rcvPacket = new DatagramPacket(new byte[1024], 1024);
        toSocket.setSoTimeout(6000);
        toSocket.receive(rcvPacket);

        // Empfänger auslesen
        byte[] rcvData = rcvPacket.getData();
        String cont = new String(rcvData, 0, rcvPacket.getLength());

        if(cont.equals("no_order")) {
            System.out.println("Nichts neues...");
        } else if(cont.contains("order")){
            String orderMenge = cont.split(";")[1];
            receiveOrder(Integer.parseInt(orderMenge));
            System.out.println("Bestellung von " + orderMenge + " " + produkt + " eingetroffen");
        }

        toSocket.close();

    }

    private int getBestand(){
        return bestand;
    }

    private void receiveOrder(int menge){
        bestand+=menge;
    }

    private void consume() throws IOException {
        bestand--;
    }

    public static void main (String args[]) throws IOException {
        Sensor s1;

        try{
            s1 = new Sensor(args[0], args[1], args[2]);
        }catch(Exception e){
            s1 = new Sensor();
        }

        while(s1.getBestand() >= 0){
            s1.send(s1.getBestand());
            s1.consume();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("LEER!");
    }

}
