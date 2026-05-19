// KitchenEvent.java
package edu.rutmiit.demo.darkkitchen.events;

import java.time.LocalDateTime;

public sealed interface KitchenEvent {

    record CookingStarted(
            String orderId,
            LocalDateTime startedAt
    ) implements KitchenEvent {}

    record CookingCompleted(
            String orderId,
            LocalDateTime completedAt,
            int cookingTimeSeconds
    ) implements KitchenEvent {}
}