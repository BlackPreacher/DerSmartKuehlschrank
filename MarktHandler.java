import org.apache.thrift.TException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.*;
import java.io.*;
import java.net.*;

public class MarktHandler implements price.Iface {

	HashMap<String,Artikel> bestand = new HashMap<>();
	HashMap<String,Integer> bestandMenge = new HashMap<>();
	String brokerIP;
	int marktnummer;

	public MarktHandler(String brokerIP) {
		this.brokerIP = brokerIP;
		Random rand = new Random();
		marktnummer = rand.nextInt();
	}

	@Override
	public int bestellung(String artikel, int menge) throws TException {
		try{

			if(bestandMenge.get(artikel) < menge){
				bestelleNach(artikel,menge);
			}

			int temppreis = bestand.get(artikel).getPreis()*menge;
			System.out.println("Der Artikel " + artikel + " wurde "+Integer.toString(menge)+"x bestellt! Preis: "
					+ Integer.toString(temppreis));

			bestand.get(artikel).generateNewPrice();
			return temppreis;

		} catch (Exception e){
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int artikelPreis(String artikel) throws TException {

		if(!bestandMenge.containsKey(artikel)){
			bestand.put(artikel,new Artikel(artikel));
			bestandMenge.put(artikel,0);
		}

		if(bestand.containsKey(artikel)){
			System.out.println("Der Artikel " + artikel + " wurde angefragt! Preis: " + bestand.get(artikel).getPreis());
			return bestand.get(artikel).getPreis();
		}

	    return -1;

	}

	private void bestelleNach(String artikel, int menge){

		String topic        = "LadenBraucht";
		String clientId     = "markt"+marktnummer;
		String content      = "nachfrage;" + clientId + ";" + artikel + ";" + menge;
		int qos             = 2;
		String broker       = "tcp://"+brokerIP+":1883";
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
			sampleClient.publish(topic, message);
			System.out.println("Message published");
			sampleClient.subscribe("LadenBraucht");
			sampleClient.setCallback(new MqttCallback() {
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
					String preis = params[3];
					System.out.println(typ);
					switch (typ){
						case "angebot":
								if(markt.equals(clientId)){
									String content = "bestellung;" + clientId + ";" + artikel + ";" + menge;
									MqttMessage message = new MqttMessage(content.getBytes());
									message.setQos(qos);
									sampleClient.publish(topic, message);
								}
							break;
						default:
							break;
					}
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

				}
			});

			sampleClient.disconnect();
			System.out.println("Disconnected");
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
