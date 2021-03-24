package com.egen.orderevents.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.receiver.internals.ConsumerFactory;
import reactor.kafka.receiver.internals.DefaultKafkaReceiver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/api")
public class OrderReceiver {

    private static final String ORDER_TOPIC = "moms-pizza-orders";

    /**
     * Handing multiple clients to get order based on stores.
     * @param storeId
     *
     **/
    @GetMapping(value = "/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<JsonNode> getOrdersEventsFlux(@RequestParam(name = "storeId") String storeId){
        Map<String, Object> propsMaps = this.kafkaReceiverConfigurations(storeId);

        DefaultKafkaReceiver<String, JsonNode> kafkaReceiver =
                new DefaultKafkaReceiver(ConsumerFactory.INSTANCE, ReceiverOptions.create(propsMaps).subscription(Collections.singleton(ORDER_TOPIC)));

        Flux<ReceiverRecord<String, JsonNode>> kafkaFlux = kafkaReceiver.receive();

        return kafkaFlux
                .filter(receivedRecord -> {
                    receivedRecord.receiverOffset().acknowledge();
                    return receivedRecord.value().get("storeId").asText().equals(storeId);
                })
                .map(ReceiverRecord::value).log();

    }

    /**
     * Static Kafka Consumer configurations.
     * @param id
     *
     **/
    private Map<String, Object> kafkaReceiverConfigurations(String id){

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "order-consumer-"+id+"-"+ UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, id);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }

}
