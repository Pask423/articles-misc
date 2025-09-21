package org.pcs.kafkaws.ws;

import org.pcs.kafkaws.kafka.Hub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
class WebSocketConfig {

    @Bean
    SimpleUrlHandlerMapping handlerMapping(WebSocketHandler wsh) {
        Map<String, WebSocketHandler> paths = Map.of("/subscribe/{topic}", wsh);
        return new SimpleUrlHandlerMapping(paths, 1);
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    ConnectionHandler connectionHandler(Hub hub, ObjectMapper objectMapper, @Value("#{'${consumer.topics}'.split(',')}") Set<String> topics) {
        return new ConnectionHandler(hub, objectMapper, topics);
    }
}