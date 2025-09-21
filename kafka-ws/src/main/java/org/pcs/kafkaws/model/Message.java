package org.pcs.kafkaws.model;

public record Message(
        String topic,
        int partition,
        long offset,
        String content
) {
}