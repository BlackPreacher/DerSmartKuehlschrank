import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created by Sem on 05.04.2017.
 */
public class Sensor {
    private Produkt product;

    public Sensor(){

    }

    private void send(String name, String datum, int menge) throws IOException {
        String sendstring = name + ";" + datum + ";" + menge;

        //tollersendeteil

        InetAddress ia = InetAddress.getByName("localhost");
        int port = 4711;
        byte[] data = sendstring.getBytes();
        DatagramPacket packet = new DatagramPacket( data, data.length, ia, port );
        DatagramSocket toSocket = new DatagramSocket();
        toSocket.send( packet );
    }

    public static void main (String args[]) throws IOException {
        Sensor s1 = new Sensor();
        Sensor s2 = new Sensor();
        Sensor s3 = new Sensor();
        Sensor s4 = new Sensor();

        int sec = 20;

        while(sec > 0){
            s1.send("Kekse", new Date().toString(), sec);
            s2.send("Bier", new Date().toString(), sec);
            s3.send("Kaese", new Date().toString(), sec);
            s4.send("Warmer Apfelkuchen", new Date().toString(), sec);

            sec--;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
