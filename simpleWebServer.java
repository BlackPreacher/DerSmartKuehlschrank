import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created by bro on 24.04.2017.
 */
public class simpleWebServer implements Runnable {

    private int port;

    public simpleWebServer(int port) {

        this.port = port;

        try {
            System.out.println("Starte Webserver " + InetAddress.getLocalHost() + ":" + port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            easyServer(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void easyServer(int port) throws IOException {

        ServerSocket server = null;
        BufferedReader br = null;
        FileReader fr = null;

        server = new ServerSocket(port);

        while (true) {
            try (Socket socket = server.accept()) {

                String header = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n";

                fr = new FileReader("httpfile.html");
                br = new BufferedReader(fr);

                String sCurrentLine;
                String content = null;
                while ((sCurrentLine = br.readLine()) != null) {
                    content += sCurrentLine;
                }

                socket.getOutputStream().write(header.getBytes("UTF-8"));
                socket.getOutputStream().write(content.getBytes("UTF-8"));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

}
