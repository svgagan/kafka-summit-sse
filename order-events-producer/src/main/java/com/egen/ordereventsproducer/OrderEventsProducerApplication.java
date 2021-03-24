package com.egen.ordereventsproducer;

import com.egen.ordereventsproducer.producer.OrderProducer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class OrderEventsProducerApplication {

	private static final String TOPIC = "moms-pizza-orders";
	private static final String BOOTSTRAP_SERVERS = "localhost:9092";
	private static final String CLIENT_ID_CONFIG = "order-generator";
	private static KafkaSender<String, JsonNode> kafkaSender;

	public static reactor.kafka.sender.KafkaSender<String, JsonNode> getKafkaSender(){

		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		SenderOptions<String, JsonNode> senderOptions = SenderOptions.create(props);
		return reactor.kafka.sender.KafkaSender.create(senderOptions);
	}

	public static void main(String[] args) {
		SpringApplication.run(OrderEventsProducerApplication.class, args);
		OrderProducer producer = new OrderProducer();
		kafkaSender = getKafkaSender();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				producer.generateMessages(kafkaSender, TOPIC, 1);
			}
		}, 0, 4000);
	}
}
