package org.pcs.websockets.chat.api.websockets;

import org.pcs.websockets.chat.store.ChatMeta;
import org.pcs.websockets.chat.store.ChatStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.List;

class ChatsHandler implements WebSocketHandler {

    private final Logger log = LoggerFactory.getLogger(ChatsHandler.class);

    private final ChatStore store;

    ChatsHandler(ChatStore store) {
        this.store = store;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String[] split = session.getHandshakeInfo()
                .getUri()
                .getPath()
                .split("/");
        String chatIdStr = split[split.length - 1];
        int chatId = Integer.parseInt(chatIdStr);
        ChatMeta chatMeta = store.get(chatId);
        if (chatMeta == null) {
            return session.close(CloseStatus.GOING_AWAY);
        }
        if (!chatMeta.canAddUser()) {
            return session.close(CloseStatus.NOT_ACCEPTABLE);
        }

        String sessionId = session.getId();
        store.addNewUser(chatId, session);
        log.info("New User {} join the chat {}", sessionId, chatId);
        return session
                .receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(message -> store.addNewMessage(chatId, sessionId, message))
                .flatMap(message -> broadcastToSessions(sessionId, message, store.get(chatId).sessions()))
                .doFinally(sig -> store.removeSession(chatId, session.getId()))
                .then();
    }

    private Mono<Void> broadcastToSessions(String sessionId, String message, List<WebSocketSession> sessions) {
        return sessions
                .stream()
                .filter(session -> !session.getId().equals(sessionId))
                .map(session -> session.send(Mono.just(session.textMessage(message))))
                .reduce(Mono.empty(), Mono::then);
    }
}
