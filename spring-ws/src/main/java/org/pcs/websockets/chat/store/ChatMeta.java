package org.pcs.websockets.chat.store;

import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ChatMeta {

    final AtomicLong offset;
    final int id;
    private final List<String> users;
    private final Map<String, WebSocketSession> sessions;
    private final boolean isf2f;

    private ChatMeta(int id, List<String> users, Map<String, WebSocketSession> sessions, AtomicLong offset, boolean isf2f) {
        this.id = id;
        this.users = users;
        this.sessions = sessions;
        this.offset = offset;
        this.isf2f = isf2f;
    }

    static ChatMeta ofId(int id) {
        return new ChatMeta(id, List.of(), Map.of(), new AtomicLong(0), false);
    }

    static ChatMeta ofIdF2F(int id) {
        return new ChatMeta(id, List.of(), Map.of(), new AtomicLong(0), true);
    }

    ChatMeta addUser(WebSocketSession user) {
        List<String> tmpUsers = new ArrayList<>(users);
        tmpUsers.add(user.getId());
        HashMap<String, WebSocketSession> tmpMap = new HashMap<>(sessions);
        tmpMap.put(user.getId(), user);
        return new ChatMeta(id, tmpUsers, tmpMap, offset, isf2f);
    }

    ChatMeta removeUser(String userId) {
        List<String> tmpUsers = new ArrayList<>(users);
        tmpUsers.remove(userId);
        HashMap<String, WebSocketSession> tmpMap = new HashMap<>(sessions);
        tmpMap.remove(userId);
        return new ChatMeta(id, tmpUsers, tmpMap, offset, isf2f);
    }

    public boolean canAddUser() {
        if (isf2f) {
            return users.size() < 2;
        }
        return true;
    }

    public List<WebSocketSession> sessions() {
        return sessions.values().stream().toList();
    }
}
