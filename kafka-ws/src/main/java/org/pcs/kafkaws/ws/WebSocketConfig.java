package org.pcs.kafkaws.ws;

import org.pcs.kafkaws.Hub;
import org.pcs.kafkaws.buffer.Buffer;
import org.pcs.kafkaws.buffer.DeafultBuffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

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
    Buffer buffer() {
        return new DeafultBuffer();
    }

    @Bean
    Hub hub(Buffer buffer) {
        return new Hub(buffer);
    }

    @Bean
    ConnectionHandler chatsHandler(Hub hub) {
        return new ConnectionHandler(hub);
    }
}