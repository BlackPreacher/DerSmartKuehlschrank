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
	private int thriftPort;
	private String brokerIP;

	public MarktServer() {
		new MarktServer("localhost","4711","6666","localhost");
	}

	public MarktServer(String zentraleIp, String zentralePort, String thriftPort, String brokerIP) {
		this.brokerIP = brokerIP;

		ipZentrale = zentraleIp;
		portZentrale = Integer.parseInt(zentralePort);
		this.thriftPort = Integer.parseInt(thriftPort);

		try {
			connectToZentrale();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try{
			handler = new MarktHandler(brokerIP);
			meinProzessor = new price.Processor(handler);
			//final int, weil die simple Funktion eine final Variable für den Port erwartet
			final int thriftPortInt = Integer.parseInt(thriftPort);

			Runnable simple = new Runnable(){
				public void run() {
					simple(meinProzessor, thriftPortInt);	//Runnable wird mit meinem Prozessor aufgerufen
				}
			};

			new Thread(simple).start();

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private void connectToZentrale() throws IOException {
		String sendstring = "market;"+Integer.toString(thriftPort);

		System.out.println(sendstring);

		InetAddress ia = InetAddress.getByName(ipZentrale);
		byte[] data = sendstring.getBytes();
		DatagramPacket packet = new DatagramPacket( data, data.length, ia, portZentrale );
		DatagramSocket toSocket = new DatagramSocket();
		toSocket.send( packet );
	}

	private static void simple(price.Processor pro, int thriftPort) {
		try{
			TServerTransport serverTransport = new TServerSocket(thriftPort);
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
			marktServer = new MarktServer (args[0],args[1],args[2],args[3]);
		}catch(Exception e){
			marktServer = new MarktServer();
		}

	}



}
