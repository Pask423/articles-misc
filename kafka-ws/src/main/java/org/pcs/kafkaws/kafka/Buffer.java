package org.pcs.kafkaws.kafka;

import org.pcs.kafkaws.model.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

class Buffer {

    private final Sinks.Many<Message> buffer;

    Buffer() {
        this.buffer = Sinks.many().unicast().onBackpressureBuffer();
    }

    void offer(Message message) {
        buffer.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    Flux<Message> listen() {
        return buffer.asFlux();
    }
}
