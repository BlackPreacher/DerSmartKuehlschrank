import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;

import java.io.IOException;
import java.net.*;

public class MarktServer {

	public static MarktHandler handler; //Hier sind meine Funktionen aus thrift implementiert
	public static price.Processor meinProzessor;
	private String ipZentrale;
	private int portZentrale;

	public MarktServer() {
		new MarktServer("localhost","4711");
	}

	public MarktServer(String zentraleIp, String zentralePort) {

		ipZentrale = zentraleIp;
		portZentrale = Integer.parseInt(zentralePort);

		try {
			connectToZentrale();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try{
			handler = new MarktHandler();
			meinProzessor = new price.Processor(handler);

			Runnable simple = new Runnable(){
				public void run() {
					simple(meinProzessor);	//Runnable wird mit meinem Prozessor aufgerufen
				}
			};

			new Thread(simple).start();

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private void connectToZentrale() throws IOException {
		String sendstring = "market;6666";

		System.out.println(sendstring);

		InetAddress ia = InetAddress.getByName(ipZentrale);
		byte[] data = sendstring.getBytes();
		DatagramPacket packet = new DatagramPacket( data, data.length, ia, portZentrale );
		DatagramSocket toSocket = new DatagramSocket();
		toSocket.send( packet );
	}

	private static void simple(price.Processor pro) {
		try{
			TServerTransport serverTransport = new TServerSocket(6666);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(pro));

			System.out.println("Server wartet: ...");
			server.serve();
			System.out.println("Message bekommen");
		} catch(Exception e){
			e.printStackTrace();
		}
	}


	public static void main(String[] args) throws Exception{

		MarktServer marktServer;

		try{
			marktServer = new MarktServer (args[0],args[1]);
		}catch(Exception e){
			marktServer = new MarktServer();
		}

	}



}