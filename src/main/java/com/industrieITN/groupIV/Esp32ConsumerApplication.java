package com.industrieITN.groupIV;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.industrieITN.groupIV.service.IDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;
import java.text.ParseException;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

@SpringBootApplication
public class Esp32ConsumerApplication implements CommandLineRunner {

	@Autowired
	private IDataService dataService;


	public static void main(String[] args) {
		SpringApplication.run(Esp32ConsumerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		final String host = ""; //Your HiveMq MQTT link
		final String username = ""; //username of your MQTT
		final String password = ""; //your MQTT password

		final Mqtt5BlockingClient client = MqttClient.builder()
				.useMqttVersion5()
				.serverHost(host)
				.serverPort(8883)
				.sslWithDefaultConfig()
				.buildBlocking();

		client.connectWith()
				.simpleAuth()
				.username(username)
				.password(UTF_8.encode(password))
				.applySimpleAuth()
				.send();

		System.out.println("Connected successfully");

		client.subscribeWith()
				.topicFilter("the topic in your HiveMQ MQTT")
				.send();

		client.toAsync().publishes(ALL, publish -> {
			System.out.println("Received message: " +
					publish.getTopic() + " -> " +
					UTF_8.decode(publish.getPayload().get()));
			try {
				String result = dataService.processFileDataContent(UTF_8.decode(publish.getPayload().get()));
				System.out.println("Dataset processed: " + result);
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}

		});
	}
}
