package org.pcs.websockets.chat.store;

import org.pcs.websockets.messages.Message;
import org.pcs.websockets.messages.MessagesStore;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultChatStore implements ChatStore {

    private final Map<Integer, ChatMeta> chats;
    private final AtomicInteger idGen;
    private final MessagesStore messagesStore;
    private final Clock clock;

    public DefaultChatStore(Clock clock, MessagesStore store) {
        this.chats = new ConcurrentHashMap<>();
        this.idGen = new AtomicInteger(0);
        this.clock = clock;
        this.messagesStore = store;
    }

    @Override
    public int create(boolean isF2F) {
        int newId = idGen.incrementAndGet();
        ChatMeta chatMeta = chats.computeIfAbsent(newId, id -> {
            if (isF2F) {
                return ChatMeta.ofId(id);
            }
            return ChatMeta.ofIdF2F(id);
        });
        return chatMeta.id;

    }

    @Override
    public void addNewUser(int id, WebSocketSession session) {
        chats.computeIfPresent(id, (k, v) -> v.addUser(session));
    }

    @Override
    public void removeSession(int id, String sessionId) {
        chats.computeIfPresent(id, (k, v) -> v.removeUser(sessionId));
    }

    @Override
    public Mono<String> addNewMessage(int id, String userId, String message) {
        ChatMeta meta = chats.getOrDefault(id, null);
        if (meta != null) {
            Message messageDoc = new Message(id, userId, meta.offset.getAndIncrement(), clock.instant(), message);
            return messagesStore.save(messageDoc)
                    .map(Message::getContent);
        }
        return Mono.empty();
    }

    @Override
    public ChatMeta get(int id) {
        return chats.get(id);
    }
}
