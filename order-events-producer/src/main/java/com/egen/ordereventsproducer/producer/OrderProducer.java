package com.egen.ordereventsproducer.producer;

import com.egen.ordereventsproducer.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class.getName());

    public Order generateOrder(){
        List<String> stores = Arrays.asList("STORE1", "STORE2", "STORE3");
        double total = ThreadLocalRandom.current().nextDouble(20, 100);
        return new Order(UUID.randomUUID().toString(), stores.get((int) (Math.random()*3)), total, new Date());
    }

    public void generateMessages(KafkaSender<String, JsonNode> sender, String topic, int count) {

        ObjectMapper objectMapper = new ObjectMapper();
        sender.<Integer>send(Flux.range(1, count)
                .map(i -> {
                    JsonNode jsonValue = null;
                    try {

                        String value = objectMapper.writeValueAsString(generateOrder());
                        jsonValue = objectMapper.readTree(value);

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    log.info("Message: {}, sent successfully", jsonValue);
                    return SenderRecord.create(new ProducerRecord<>(topic, "Key_" + UUID.randomUUID(), jsonValue), i);
                }))
                .doOnError(e -> log.error("Send failed", e))
                .subscribe(r -> {
                    RecordMetadata metadata = r.recordMetadata();
                    log.debug("Partition: {} -> Offset: {}", metadata.partition(), metadata.offset());
                });
    }
}
