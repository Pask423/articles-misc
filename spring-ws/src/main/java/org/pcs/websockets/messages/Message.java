package org.pcs.websockets.messages;

import org.springframework.data.annotation.Id;

import java.time.Instant;

public class Message {

    @Id
    private String id;
    private int chatId;
    private String owner;
    private long offset;
    private Instant timestamp;
    private String content;

    public Message() {
    }

    public Message(int chatId, String owner, long offset, Instant timestamp, String content) {
        this.chatId = chatId;
        this.owner = owner;
        this.offset = offset;
        this.timestamp = timestamp;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public int getChatId() {
        return chatId;
    }

    public String getOwner() {
        return owner;
    }

    public long getOffset() {
        return offset;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }
}
