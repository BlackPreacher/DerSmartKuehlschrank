import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

/**
 * Created by Sem on 05.04.2017.
 */
public class Sensor {

    private String ip;

    public Sensor(String ip){
        this.ip = ip;
    }

    public Sensor(){
        ip = "localhost";
    }

    private void send(String name, String datum, int menge) throws IOException {
        String sendstring = name + ";" + datum + ";" + menge;

        InetAddress ia = InetAddress.getByName(ip);
        int port = 4711;
        byte[] data = sendstring.getBytes();
        DatagramPacket packet = new DatagramPacket( data, data.length, ia, port );
        DatagramSocket toSocket = new DatagramSocket();
        toSocket.send( packet );
    }

    public static void main (String args[]) throws IOException {
        Sensor s1, s2, s3, s4;

        try{
            s1 = new Sensor(args[0]);
            s2 = new Sensor(args[0]);
            s3 = new Sensor(args[0]);
            s4 = new Sensor(args[0]);
        }catch(Exception e){
            s1 = new Sensor();
            s2 = new Sensor();
            s3 = new Sensor();
            s4 = new Sensor();
        }



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
