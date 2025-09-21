package org.pcs.kafkaws.ws;

import org.pcs.kafkaws.kafka.Hub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

class ConnectionHandler implements WebSocketHandler {

    private final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

    private final Hub hub;
    private final ObjectMapper objectMapper;
    private final Set<String> topics;

    ConnectionHandler(Hub hub, ObjectMapper objectMapper, Set<String> topics) {
        this.hub = hub;
        this.objectMapper = objectMapper;
        this.topics = topics;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String[] split = session.getHandshakeInfo()
                .getUri()
                .getPath()
                .split("/");
        String topic = split[split.length - 1];
        if (!topics.contains(topic)) {
            return session.close(CloseStatus.NOT_ACCEPTABLE);
        }

        Flux<WebSocketMessage> sendFlux = hub
                .subscribe(topic)
                .map(objectMapper::writeValueAsString)
                .map(session::textMessage);
        return session
                .send(sendFlux)
                .doOnSubscribe(s -> log.info("New subscriber for topic {}, current subscribers {}", topic, hub.subscribersCount(topic) + 1))
                .doOnError(e -> log.warn("Outbound error for {}: {}", session.getId(), e.toString()))
                .doFinally(sig -> {
                    int subscribers = hub.subscribersCount(topic);
                    if (subscribers == 0) {
                        hub.unsubscribe(topic);
                        log.info("Unsubscribe last client from topic {} - removed topic tracker", topic);
                    }
                    log.info("Unsubscribe client from topic {}, current subscribers {}", topic, subscribers);
                })
                .then(session.close(CloseStatus.NORMAL));
    }
}