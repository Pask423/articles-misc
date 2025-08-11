package org.pcs.websockets.chat.api.websockets;

import org.pcs.websockets.chat.store.ChatStore;
import org.pcs.websockets.chat.store.DefaultChatStore;
import org.pcs.websockets.messages.MessagesStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.time.Clock;
import java.util.Map;

@Configuration
class WebsocketConfig {

    @Bean
    SimpleUrlHandlerMapping handlerMapping(WebSocketHandler wsh) {
        Map<String, WebSocketHandler> paths = Map.of("/chats/{id}", wsh);
        return new SimpleUrlHandlerMapping(paths, 1);
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    ChatStore chatStore(MessagesStore messagesStore) {
        return new DefaultChatStore(Clock.systemUTC(), messagesStore);
    }

    @Bean
    ChatsHandler chatsHandler(ChatStore chatStore) {
        return new ChatsHandler(chatStore);
    }
}