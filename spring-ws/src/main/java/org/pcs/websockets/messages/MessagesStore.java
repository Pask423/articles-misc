package org.pcs.websockets.messages;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MessagesStore extends ReactiveMongoRepository<Message, String> {
}