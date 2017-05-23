import org.apache.thrift.TException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.*;
import java.io.*;
import java.net.*;

public class MarktHandler implements price.Iface {


	HashMap<String,Ware> bestand = new HashMap<>();
	HashMap<String,Angebot> angebote = new HashMap<>();
	private String brokerIP;
	private int marktnummer;
	private MqttClient mqttClient;
	int qos = 2;


	public MarktHandler(String brokerIP) {
		this.brokerIP = brokerIP;
		marktnummer = (int)(Math.random()*Integer.MAX_VALUE);
		mqttClient = makeMQTTClient();
		subscribe(mqttClient,"Angebot");
		subscribe(mqttClient,"Bestellung");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(true){
						for (String art : bestand.keySet()) {
							bestelleNach(art, 10);
						}
						Thread.sleep((int)(Math.random() * 10000));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		//updateAngebote();
	}

	@Override
	public int bestellung(String artikel, int menge) throws TException {
		try{
			Artikel derArtikel = bestand.get(artikel).getArtikel();
			if(bestand.get(artikel).getMenge() < menge){
				bestelleNach(artikel,menge);
			}

			//Warten bis die Bestellung vorbei ist
			while(bestand.get(artikel).getMenge() < menge){
				Thread.sleep(1000);
			}

			int temppreis = bestand.get(artikel).getPreis()*menge;
			System.out.println("Der Artikel " + artikel + " wurde " + Integer.toString(menge) + "x beim Lieferanten bestellt! Preis: "
					+ Integer.toString(temppreis));


			//bestand.put(artikel, bestandMenge.get(artikel) - menge);
			int neuerBestand = bestand.get(artikel).getMenge() + menge;
			bestand.put(artikel, new Ware(neuerBestand,derArtikel));

			bestand.get(artikel).generateNewPrice();
			return temppreis;

		} catch (Exception e){
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int artikelPreis(String artikel) throws TException {

		if(!bestand.containsKey(artikel)){
			bestand.put(artikel,new Ware(0,new Artikel(artikel)));
		}

		if(bestand.containsKey(artikel)){
			System.out.println("Der Artikel " + artikel + " wurde vom Kühlschrank angefragt! Preis: " + bestand.get(artikel).getPreis());
			return bestand.get(artikel).getPreis();
		}

	    return -1;

	}

	private MqttClient makeMQTTClient(){
		String broker = "tcp://"+brokerIP+":1883";
		final String clientId  = "markt"+marktnummer;
		MemoryPersistence persistence = new MemoryPersistence();


		try{
			MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			sampleClient.connect(connOpts);
			sampleClient.setCallback(new MqttCallback() {
				@Override
				public void connectionLost(Throwable throwable) {

				}

				@Override
				public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
					final String toParse = new String(mqttMessage.getPayload());
					System.out.println("Got: " + toParse);
					String[] params = toParse.split(";");
					String typ = params[0];
					String erzeuger = params[1];
					String produkt = params[2];
					String preis = params[3];

					switch (typ){
						case "sonderangebot":
						case "angebot":
							angebote.put(produkt, new Angebot(erzeuger, Integer.parseInt(preis)));
							break;
						case "bestaetigung":
							String markt = erzeuger;
							int menge = Integer.parseInt(params[5]);
							if(markt.equals(clientId)){
								//bestandMenge.put(produkt, bestandMenge.get(produkt) + menge);
								int neueMenge = bestand.get(produkt).getMenge() + menge;
								bestand.get(produkt).setMenge(neueMenge);
								bestand.get(produkt).setPreis(Integer.parseInt(preis) + 10);
								System.out.println("Bestellung durchgeführt und Bestand aufgefüllt.");
							}

						default:
							break;
					}

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
			System.out.println("Subsribe: " + topic);
			client.subscribe(topic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	private void publish(MqttClient client, String topic, String message){
		try {
			System.out.println("Publish: " + topic);
			MqttMessage mqttMessage = new MqttMessage(message.getBytes());
			mqttMessage.setQos(qos);
			client.publish(topic,mqttMessage);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/*private void updateAngebote(){
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
	}*/

	private void bestelleNach(String artikel, int menge){

		String erzeuger = angebote.get(artikel).erzeuger;

		String clientId     = "markt"+marktnummer;
		String content      = "bestellung;" + clientId + ";" + artikel + ";" + menge + ";" + erzeuger;

		publish(mqttClient,"Bestellung",content);
	}



	/*private void bestelleNach(String artikel, int menge){

		if(angebote.containsKey(artikel)){

			String erzeuger = angebote.get(artikel).erzeuger;

			String clientId     = "markt"+marktnummer;
			String content      = "bestellung;" + clientId + ";" + artikel + ";" + menge + ";" + erzeuger;

			publish(mqttClient,"Bestellung",content);

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

				//-
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

							*//*
							bestandMenge.put(produkt, bestandMenge.get(produkt) + menge);
							bestand.get(artikel).setPreis(Integer.parseInt(preis) + 10);
							System.out.println("Bestellung durchgeführt und Bestand aufgefüllt.");*//*
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


}*/


}
