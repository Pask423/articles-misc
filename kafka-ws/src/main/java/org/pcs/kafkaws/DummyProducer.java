package org.pcs.kafkaws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class DummyProducer {

    public DummyProducer(KafkaTemplate<String, String> kafkaTemplate,
                         @Value("${consumer.topics}") String topic) {
        List<String> topics = Arrays.stream(topic.split(",")).toList();
        new ScheduledThreadPoolExecutor(1)
                .scheduleAtFixedRate(() -> topics.forEach(t -> kafkaTemplate.send(t, randomString(10))), 5000, 1000, TimeUnit.MILLISECONDS);
    }

    public String randomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}