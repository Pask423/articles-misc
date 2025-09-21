package org.pcs.kafkaws.kafka;

import org.pcs.kafkaws.model.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

public class Hub {

    private final ConcurrentHashMap<String, Sinks.Many<Message>> subscribers;

    Hub(Buffer buffer) {
        this.subscribers = new ConcurrentHashMap<>();
        buffer.listen()
                .subscribe(message -> {
                    Sinks.Many<Message> messageMany = subscribers.get(message.topic());
                    if (messageMany != null) {
                        messageMany.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
                    }
                });
    }

    public Flux<Message> subscribe(String topic) {
        return subscribers
                .computeIfAbsent(topic, t -> Sinks.many().multicast().onBackpressureBuffer())
                .asFlux();
    }

    public int subscribersCount(String topic) {
        Sinks.Many<Message> topicTracker = subscribers.get(topic);
        if (topicTracker == null) {
            return 0;
        }
        return topicTracker.currentSubscriberCount();
    }

    public void unsubscribe(String topic) {
        subscribers.computeIfPresent(topic, (k, v) -> {
            v.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            return null;
        });
    }
}