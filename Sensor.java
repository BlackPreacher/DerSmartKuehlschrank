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
    private int usedport;

    public Sensor(String ip, String port){
        this.ip = ip;
        usedport = Integer.parseInt(port);
    }

    public Sensor(){
        ip = "localhost";
        usedport = 4711;
    }

    private void send(String name, String datum, int menge) throws IOException {
        String sendstring = name + ";" + datum + ";" + menge;

        InetAddress ia = InetAddress.getByName(ip);
        byte[] data = sendstring.getBytes();
        DatagramPacket packet = new DatagramPacket( data, data.length, ia, usedport );
        DatagramSocket toSocket = new DatagramSocket();
        toSocket.send( packet );
    }

    public static void main (String args[]) throws IOException {
        Sensor s1, s2, s3, s4;

        try{
            s1 = new Sensor(args[0], args[1]);
            s2 = new Sensor(args[0], args[1]);
            s3 = new Sensor(args[0], args[1]);
            s4 = new Sensor(args[0], args[1]);
        }catch(Exception e){
            s1 = new Sensor();
            s2 = new Sensor();
            s3 = new Sensor();
            s4 = new Sensor();
        }

        int sec = 20;

        while(sec > 0){
            s1.send("Kekse", String.valueOf(System.currentTimeMillis()), sec);
            s2.send("Bier", String.valueOf(System.currentTimeMillis()), sec);
            s3.send("Kaese", String.valueOf(System.currentTimeMillis()), sec);
            s4.send("Kuchen", String.valueOf(System.currentTimeMillis()), sec);

            sec--;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
