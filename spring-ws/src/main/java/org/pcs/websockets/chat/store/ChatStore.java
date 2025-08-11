package org.pcs.websockets.chat.store;

import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public interface ChatStore {

    int create(boolean isF2F);

    void addNewUser(int id, WebSocketSession session);

    Mono<String> addNewMessage(int id, String userId, String message);

    void removeSession(int id, String session);

    ChatMeta get(int id);
}