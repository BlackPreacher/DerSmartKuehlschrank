import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static javax.script.ScriptEngine.FILENAME;

/**
 * Created by Hellhero on 05.04.2017.
 */
public class Zentrale {

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String returnvalue = "";

            BufferedReader br = null;
            FileReader fr = null;

            try {
                fr = new FileReader("httpfile.html");
                br = new BufferedReader(fr);
                String sCurrentLine;

                while ((sCurrentLine = br.readLine()) != null) {
                    returnvalue += sCurrentLine;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null)
                        br.close();
                    if (fr != null)
                        fr.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            String response = returnvalue;
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public void startWebserver() throws IOException {
        System.out.println("Starte Webserver " + InetAddress.getLocalHost() + ":" + httpport);
        HttpServer server = HttpServer.create(new InetSocketAddress(httpport), 0);
        server.createContext("/", new MyHandler());
        server.start();
    }

    private int listenerport;
    private int httpport;
    private ZentraleSensorListener listener;

    public Zentrale() throws IOException {
        listenerport = 4711;
        httpport = 4712;
        listener = new ZentraleSensorListener(listenerport);
        (new Thread(listener)).start();
        simpleWebServer ws = new simpleWebServer(8080);
        (new Thread(ws)).start();
        //startWebserver();
    }

    public Zentrale(String listenerport, String httpport) throws IOException{
        this.listenerport = Integer.parseInt(listenerport);
        this.httpport = Integer.parseInt(httpport);
        listener = new ZentraleSensorListener(this.listenerport);
        (new Thread(listener)).start();
        simpleWebServer ws = new simpleWebServer(this.httpport);
        (new Thread(ws)).start();
        //startWebserver();
    }


    public static void main (String args[]) throws IOException {
        Zentrale kuehlschrank;

        try{
            kuehlschrank = new Zentrale(args[0], args[1]);
        }catch(Exception e){
            kuehlschrank = new Zentrale();
        }
    }


}
