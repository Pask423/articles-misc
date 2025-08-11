package org.pcs.websockets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class WebsocketsChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketsChatApplication.class, args);
    }
}