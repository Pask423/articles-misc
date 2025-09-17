package org.pcs.kafkaws.kafka;


import org.pcs.kafkaws.model.Message;
import org.springframework.kafka.annotation.KafkaListener;

public class Consumer {

    private final Buffer buffer;

    public Consumer(Buffer buffer) {
        this.buffer = buffer;
    }

    @KafkaListener(topics = "${consumer.topics}")
    public void handle(Message message) {
        buffer.offer(message);
    }
}