package org.pcs.websockets.chat.api.rest;

record ChatCreated(String url) {

    private static final String URL_FORMAT = "ws://localhost:8080/chats/%s";

    static ChatCreated ofId(int id) {
        return new ChatCreated(URL_FORMAT.formatted(id));
    }
}
