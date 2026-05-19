// EventMetadata.java
package edu.rutmiit.demo.darkkitchen.events;

import java.time.Instant;
import java.util.UUID;

public record EventMetadata(
        String eventId,
        String source,
        String eventType,
        Instant timestamp
) {
    public static EventMetadata create(String source, String eventType) {
        return new EventMetadata(
                UUID.randomUUID().toString(),
                source,
                eventType,
                Instant.now()
        );
    }
}