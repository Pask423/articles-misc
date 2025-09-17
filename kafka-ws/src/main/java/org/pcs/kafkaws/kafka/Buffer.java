package org.pcs.kafkaws.kafka;

import org.pcs.kafkaws.model.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class Buffer {

    private final Sinks.Many<Message> flux;

    Buffer() {
        this.flux = Sinks.many().unicast().onBackpressureBuffer();
    }

    public void offer(Message message) {
        flux.tryEmitNext(message);
    }

    public Flux<Message> listen() {
        return flux.asFlux();
    }
}
