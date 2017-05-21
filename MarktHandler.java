import org.apache.thrift.TException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.*;
import java.io.*;
import java.net.*;

public class MarktHandler implements price.Iface {

	HashMap<String,Artikel> bestand = new HashMap<>();
	HashMap<String,Integer> bestandMenge = new HashMap<>();
	HashMap<String,Angebot> angebote = new HashMap<>();
	private String brokerIP;
	private int marktnummer;


	public MarktHandler(String brokerIP) {
		this.brokerIP = brokerIP;
		marktnummer = (int)(Math.random()*Integer.MAX_VALUE);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(true){
						for (String art : bestand.keySet()) {
							bestelleNach(art, 10);
						}
						Thread.sleep(10000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		updateAngebote();
	}

	@Override
	public int bestellung(String artikel, int menge) throws TException {
		try{

			if(bestandMenge.get(artikel) < menge){
				bestelleNach(artikel,menge);
			}

			//Warten bis die Bestellung vorbei ist
			while(bestandMenge.get(artikel) < menge){
				Thread.sleep(1000);
			}

			int temppreis = bestand.get(artikel).getPreis()*menge;
			System.out.println("Der Artikel " + artikel + " wurde " + Integer.toString(menge) + "x beim Lieferanten bestellt! Preis: "
					+ Integer.toString(temppreis));


			bestandMenge.put(artikel, bestandMenge.get(artikel) - menge);

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
			System.out.println("Der Artikel " + artikel + " wurde vom Kühlschrank angefragt! Preis: " + bestand.get(artikel).getPreis());
			return bestand.get(artikel).getPreis();
		}

	    return -1;

	}

	private void updateAngebote(){
		String clientId     = "markt"+marktnummer;
		String broker       = "tcp://" + brokerIP + ":1883";
		MemoryPersistence persistence = new MemoryPersistence();

		try {
			MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			sampleClient.connect(connOpts);
			sampleClient.subscribe("Angebot");
			sampleClient.setCallback(new MqttCallback() {
				@Override
				public void connectionLost(Throwable throwable) {
					throwable.printStackTrace();
				}

				@Override
				public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
					final String toParse = new String(mqttMessage.getPayload());

					String[] params = toParse.split(";");
					String typ = params[0];
					String erzeuger = params[1];
					String produkt = params[2];
					String preis = params[3];

					switch (typ){
						case "angebot":
							System.out.println("Subscribe: " + toParse);
							angebote.put(produkt, new Angebot(erzeuger, Integer.parseInt(preis)));
							break;
						default:
							break;
					}
				}
				@Override
				public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

				}
			});

			//sampleClient.disconnect();
			//System.out.println("Disconnected");
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



	private void bestelleNach(String artikel, int menge){

		if(angebote.containsKey(artikel)){

			String erzeuger = angebote.get(artikel).erzeuger;

			String clientId     = "markt"+marktnummer;
			String content      = "bestellung;" + clientId + ";" + artikel + ";" + menge + ";" + erzeuger;
			int qos             = 2;
			String broker       = "tcp://" + brokerIP + ":1883";
			MemoryPersistence persistence = new MemoryPersistence();

			try {
				MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
				MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setCleanSession(true);
				sampleClient.connect(connOpts);


				System.out.println("Publish: "+content);
				MqttMessage message = new MqttMessage(content.getBytes());
				message.setQos(qos);
				sampleClient.publish("Bestellung", message);


				sampleClient.subscribe("Bestellung");
				sampleClient.setCallback(new MqttCallback() {
					@Override
					public void connectionLost(Throwable throwable) {
						throwable.printStackTrace();
					}

					@Override
					public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
						final String toParse = new String(mqttMessage.getPayload());

						String[] params = toParse.split(";");
						String typ = params[0];
						String erzeuger = params[1];
						String produkt = params[2];
						String preis = params[3];

						switch (typ){
							case "angebot":
								System.out.println("Subscribe: " + toParse);
								angebote.put(produkt, new Angebot(erzeuger, Integer.parseInt(preis)));

							/*
							bestandMenge.put(produkt, bestandMenge.get(produkt) + menge);
							bestand.get(artikel).setPreis(Integer.parseInt(preis) + 10);
							System.out.println("Bestellung durchgeführt und Bestand aufgefüllt.");*/
								break;
							default:
								break;
						}
					}

					@Override
					public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

					}
				});

				//sampleClient.disconnect();
				//System.out.println("Disconnected");
				//System.exit(0);
			} catch(MqttException me) {
				System.out.println("reason "+me.getReasonCode());
				System.out.println("msg "+me.getMessage());
				System.out.println("loc "+me.getLocalizedMessage());
				System.out.println("cause "+me.getCause());
				System.out.println("excep "+me);
				me.printStackTrace();
			}

		}else{
			System.out.println("Es konnte keine Bestellung durchgeführt werden");
		}


}


}
