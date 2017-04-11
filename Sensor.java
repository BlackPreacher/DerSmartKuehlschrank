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
    private String produkt;

    public Sensor(String produkt, String ip, String port){
        this.ip = ip;
        usedport = Integer.parseInt(port);
        this.produkt = produkt;
    }

    public Sensor(){
        ip = "localhost";
        usedport = 4711;
        produkt = "NULL";
    }

    private void send(int menge) throws IOException {
        String sendstring = produkt + ";" + String.valueOf(System.currentTimeMillis()) + ";" + menge;

        System.out.println(sendstring);

        InetAddress ia = InetAddress.getByName(ip);
        byte[] data = sendstring.getBytes();
        DatagramPacket packet = new DatagramPacket( data, data.length, ia, usedport );
        DatagramSocket toSocket = new DatagramSocket();
        toSocket.send( packet );
    }

    public static void main (String args[]) throws IOException {
        Sensor s1, s2, s3, s4;

        try{
            s1 = new Sensor(args[0], args[1], args[2]);
        }catch(Exception e){
            s1 = new Sensor();
        }

        int sec = 20;

        while(sec > 0){
            s1.send(sec);
            sec--;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
