package org.pcs.kafkaws;

public record Message(
        String topic,
        int partition,
        long offset,
        byte[] key,
        byte[] value
) {
}
