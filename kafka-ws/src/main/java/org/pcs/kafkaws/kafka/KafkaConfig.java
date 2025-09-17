package org.pcs.kafkaws.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class KafkaConfig {


    @Bean
    Buffer buffer() {
        return new DeafultBuffer();
    }

    @Bean
    Hub hub(Buffer buffer) {
        return new Hub(buffer);
    }

}