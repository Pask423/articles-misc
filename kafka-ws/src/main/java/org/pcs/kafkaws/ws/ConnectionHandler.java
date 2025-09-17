package org.pcs.kafkaws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ChatsHandler implements WebSocketHandler {

    private final Logger log = LoggerFactory.getLogger(ChatsHandler.class);

    private final Hub hub;

    public ChatsHandler(Hub hub) {
        this.hub = hub;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String[] split = session.getHandshakeInfo()
                .getUri()
                .getPath()
                .split("/");
        String topic = split[split.length - 1];


        String sessionId = session.getId();
        log.info("New User {} join the chat {}", sessionId, topic);
        // Outbound stream: convert your hub messages to WebSocketMessage
        Flux<WebSocketMessage> outbound =
                hub.subscribe(topic) // <- do NOT subscribe manually here
                        .map(msg -> session.textMessage(String.valueOf(msg.offset())))
                        .doOnSubscribe(s -> log.info("Session {} joined topic {}", session.getId(), topic))
                        .doOnNext(ws -> log.debug("Sending to {}: {}", session.getId(), ws.getPayloadAsText()))
                        .doOnError(e -> log.warn("Outbound error for {}: {}", session.getId(), e.toString()));

        return session.send(outbound)
                .doFinally(sig -> System.out.println(hub.unsubscribe(topic)))
                .then();
    }
}
