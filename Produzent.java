/**
 * Created by Hellhero on 21.05.2017.
 */

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class Produzent {

    String artikel;
    int preis;
    String clientId;
    String brokerIP;

    public static void main(String[] args) throws Exception{
        Produzent prod = new Produzent(args);
    }

    public Produzent(String[] param) {
        clientId = param[1];
        artikel = param[2];
        brokerIP = param[0];
        starteProduzent();
        generatenewPrice();
    }

    private void generatenewPrice(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true){
                        preis = (int)(Math.random() * 100);

                        int qos             = 2;
                        String broker       = "tcp://" + brokerIP + ":1883";
                        MemoryPersistence persistence = new MemoryPersistence();

                        try {
                            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
                            MqttConnectOptions connOpts = new MqttConnectOptions();
                            connOpts.setCleanSession(true);
                            sampleClient.connect(connOpts);
                            String content = "angebot;" + clientId + ";" + artikel + ";" + preis;
                            System.out.println("Publish: " + content);
                            MqttMessage message = new MqttMessage(content.getBytes());
                            message.setQos(qos);
                            sampleClient.publish("Angebot", message);
                        } catch(MqttException me) {
                            System.out.println("reason "+me.getReasonCode());
                            System.out.println("msg "+me.getMessage());
                            System.out.println("loc "+me.getLocalizedMessage());
                            System.out.println("cause "+me.getCause());
                            System.out.println("excep "+me);
                            me.printStackTrace();
                        }

                        Thread.sleep((int)(Math.random() * 10000));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void starteProduzent(){
        String topic        = "Sonderangebot";
        String content      = "Ich habe sehr viel billiger";
        int qos             = 2;
        String broker       = "tcp://" + brokerIP + ":1883";

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
            sampleClient.subscribe("Bestellung");
            MqttCallback callback = new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    final String toParse = new String(mqttMessage.getPayload());
                    System.out.println("Subscribe: " + toParse);

                    String[] params = toParse.split(";");
                    String typ = params[0];
                    String markt = params[1];
                    String produkt = params[2];
                    String menge = params[3];
                    switch (typ){
                        case "bestellung":
                            if(params[4].equals(clientId)){
                                System.out.println("Subscribe: " + toParse);
                                String content = "bestaetigung;" + markt + ";" + produkt + ";" + preis + ";" +  clientId;
                                MqttMessage message = new MqttMessage(content.getBytes());
                                message.setQos(qos);
                                sampleClient.publish("Bestellung", message);
                                System.out.println("Bestellung von Markt " + markt + " über " + menge + " " + produkt + " wurde bei " + clientId + " abgewickelt.");

                                preis = (int)(Math.random() * 100);
                                content = "angebot;" + clientId + ";" + produkt + ";" + preis;
                                System.out.println("Publish: " + content);
                                message = new MqttMessage(content.getBytes());
                                message.setQos(qos);
                                sampleClient.publish("Angebot", message);
                            }
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            };
            sampleClient.setCallback(callback);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
