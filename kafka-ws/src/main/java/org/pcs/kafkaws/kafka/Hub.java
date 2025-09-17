package org.pcs.kafkaws;

import org.pcs.kafkaws.buffer.Buffer;
import org.pcs.kafkaws.model.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;


public class Hub {

    private final ConcurrentHashMap<String, Sinks.Many<Message>> subscribers = new ConcurrentHashMap<>();

    public Hub(Buffer buffer) {
        buffer.listen()
                .doOnNext(message ->
                        subscribers
                                .computeIfAbsent(message.topic(), topic -> Sinks.many().multicast().onBackpressureBuffer())
                                .tryEmitNext(message)
                );

    }

    public Flux<Message> subscribe(String topic) {
        if (subscribers.containsKey(topic)) {
            return subscribers.get(topic).asFlux();
        }
        return Flux.empty();
    }

    public int unsubscribe(String topic) {
        Sinks.Many<Message> messageMany = subscribers.computeIfPresent(topic, (k, v) -> {
            if (v.currentSubscriberCount() == 0) {
                return null;
            }
            return v;
        });
        if (messageMany == null) {
            return 0;
        }
        return messageMany.currentSubscriberCount();
    }
}

