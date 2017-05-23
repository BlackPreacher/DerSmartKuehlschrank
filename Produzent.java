/**
 * Created by Hellhero on 21.05.2017.
 */

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;

public class Produzent {

    String artikel;
    int preis;
    String clientId;
    String brokerIP;
    MqttClient mqttClient;
    int qos = 2;

    public static void main(String[] args) throws Exception{
        Produzent prod = new Produzent(args);
    }

    public Produzent(String[] param) {
        clientId = param[1];
        artikel = param[2];
        brokerIP = param[0];
        mqttClient = makeMQTTClient();
        System.out.println("Gestartet für broker " + brokerIP + " als " + clientId + " mit " + artikel);
        subscribe(mqttClient,"Bestellung");
        generatenewPrice();
    }

    private MqttClient makeMQTTClient(){
        try{
            String broker = "tcp://"+brokerIP+":1883";
            MemoryPersistence persistence = new MemoryPersistence();
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
            sampleClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    parseMqttMessage(mqttMessage);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });

            return sampleClient;
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
            return null;
        }
    }

    private void subscribe(MqttClient client, String topic){
        try {
            System.out.println("Subscribe: " + topic);
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publish(MqttClient client, String topic, String message){
        try {
            System.out.println("Publish: " + message);
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(qos);
            client.publish(topic,mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void parseMqttMessage(MqttMessage message) throws Exception{
        final String toParse = new String(message.getPayload());
        System.out.println("Got: " + toParse);

        String[] params = toParse.split(";");
        String typ = params[0];
        String markt = params[1];
        String produkt = params[2];
        String menge = params[3];
        switch (typ){
            case "bestellung":
                if(params[4].equals(clientId)){

                    String content = "bestaetigung;" + markt + ";" + produkt + ";" + preis + ";" +  clientId + ";" + menge;
                    publish(mqttClient,"Bestellung",content);
                    System.out.println("Bestellung von Markt " + markt + " über " + menge + " " + produkt + " wurde bei " + clientId + " abgewickelt.");

                    preis = (int)(Math.random() * 100);
                    content = "angebot;" + clientId + ";" + produkt + ";" + preis;
                    publish(mqttClient,"Angebot",content);

                }
                break;
            default:
                break;
        }
    }

    private void generatenewPrice(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true){
                        preis = (int)(Math.random() * 100);
                        String type = "angebot";
                        if(Math.random() < 0.2){
                            preis = (int)(Math.random() * 10);
                            type = "sonderangebot";
                        } else {
                            if(preis<10){
                                preis = (int)(Math.random() * 100);
                            }
                        }
                        String content = type+";" + clientId + ";" + artikel + ";" + preis;
                        publish(mqttClient,"Angebot",content);
                        Thread.sleep(10000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
