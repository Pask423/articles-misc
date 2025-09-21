package org.pcs.kafkaws.kafka;

import org.pcs.kafkaws.model.Message;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

class Consumer {

    private final Buffer buffer;

    Consumer(Buffer buffer) {
        this.buffer = buffer;
    }

    @KafkaListener(
            topics = "#{'${consumer.topics}'.split(',')}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handle(String message,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.OFFSET) long offset) {
        buffer.offer(new Message(topic, partition, offset, message));
    }
}