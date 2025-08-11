package org.pcs.websockets.chat.api.rest;

import org.pcs.websockets.chat.store.ChatStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration(proxyBeanMethods = false)
class ChatRouter {

    private final ChatStore chatStore;

    ChatRouter(ChatStore chatStore) {
        this.chatStore = chatStore;
    }

    @Bean
    RouterFunction<ServerResponse> routes() {
        return RouterFunctions
                .route(POST("api/v1/chats/create"), e -> create(false))
                .andRoute(POST("api/v1/chats/create-f2f"), e -> create(true))
                .andRoute(GET("api/v1/chats/{id}"), this::get);
    }

    private Mono<ServerResponse> create(boolean isF2F) {
        int chatId = chatStore.create(isF2F);
        return ServerResponse
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ChatCreated.ofId(chatId)));
    }

    private Mono<ServerResponse> get(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(chatStore.get(Integer.parseInt(id))));
    }
}