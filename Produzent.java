/**
 * Created by Hellhero on 21.05.2017.
 */

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class Produzent {
    ArrayList<Artikel> artikel = new ArrayList<>();


    public static void main(String[] args) throws Exception{
        Produzent prod = new Produzent(args);
    }

    public Produzent(String[] param) {

        for(int i = 1; i < param.length; i++){
            artikel.add(new Artikel(param[i]));
        }

        String topic        = "Sonderangebot";
        String content      = "Ich habe sehr viel billiger";
        int qos             = 2;
        String broker       = "tcp://"+param[0]+":1883";
        String clientId     = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            //sampleClient.publish(topic, message);
            System.out.println("Message published");
            //sampleClient.disconnect();
            System.out.println("Disconnected");

            sampleClient.subscribe("LadenBraucht");
            MqttCallback callback = new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    final String toParse = new String(mqttMessage.getPayload());
                    System.out.println(toParse);

                    String[] params = toParse.split(";");
                    String typ = params[0];
                    String markt = params[1];
                    String produkt = params[2];
                    String menge = params[3];
                    System.out.println(typ);
                    switch (typ){
                        case "nachfrage":
                            for (Artikel art: artikel) {
                                if(art.getName().equals(produkt)){
                                    String content = "angebot;" + markt + ";" + produkt + ";" + art.getPreis() ;
                                    MqttMessage message = new MqttMessage(content.getBytes());
                                    message.setQos(qos);
                                    sampleClient.publish("LadenBraucht", message);
                                }
                            }
                            break;
                        case "bestellung":
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
            //System.out.println("HIER BIN ICH");
            //System.exit(0);
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
