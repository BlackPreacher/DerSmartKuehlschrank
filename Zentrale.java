import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Hellhero on 05.04.2017.
 */
public class Zentrale {

    private int listenerport;
    private int httpport;
    private ZentraleSensorListener listener;

    public Zentrale() throws Exception {
        listenerport = 4711;
        httpport = 80;
        listener = new ZentraleSensorListener(listenerport);
        new Thread(listener).start();
        startWebserver();
    }

    public Zentrale(String listenerport, String httpport) throws Exception{
        this.listenerport = Integer.parseInt(listenerport);
        this.httpport = Integer.parseInt(httpport);
        listener = new ZentraleSensorListener(this.listenerport);
        new Thread(listener).start();
        startWebserver();
    }

    public void startWebserver() throws Exception {

        System.out.println("Starte Webserver " + InetAddress.getLocalHost() + ":" + httpport);
        ServerSocket server = new ServerSocket( httpport );
        try {
            while (true){
                Socket client = server.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handleConnection(client);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        catch ( InterruptedIOException e)  {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket client)throws Exception{
        PrintWriter out = new PrintWriter( client.getOutputStream(), true );
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        ArrayList<String> browserHeader = new ArrayList<>();
        String browserHeaderTemp;
        while ((browserHeaderTemp = in.readLine()) != null)
        {
            if (browserHeaderTemp.isEmpty()){
                break;
            }
            browserHeader.add(browserHeaderTemp);
        }
        for (String bh :browserHeader) {
            if (bh.contains("GET")){
                String[] pairs = bh.split(" ");
                String[] param = pairs[1].split("/");
                int paramcount = param.length;
                if (paramcount > 0){
                    String paramstring = param[1];
                    if (!paramstring.equals("favicon.ico")){
                        out.println(listener.getBestandVonProdukt(paramstring));
                        out.println(listener.getActualBestandVonProdukt(paramstring));
                    }
                } else {
                    out.println(listener.getBestand());
                }
            }
        }
        client.close();
    }


    public static void main (String args[]) throws Exception {
        Zentrale kuehlschrank;

        try{
            kuehlschrank = new Zentrale(args[0], args[1]);
        }catch(Exception e){
            kuehlschrank = new Zentrale();
        }
    }
}
