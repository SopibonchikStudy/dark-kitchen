// DeliveryEvent.java
package edu.rutmiit.demo.darkkitchen.events;

import java.time.LocalDateTime;

public sealed interface DeliveryEvent {

    record CourierAssigned(
            String orderId,
            String courierId,
            String courierName,
            LocalDateTime assignedAt
    ) implements DeliveryEvent {}

    record DeliveryStarted(
            String orderId,
            String courierId,
            LocalDateTime startedAt
    ) implements DeliveryEvent {}

    record Delivered(
            String orderId,
            String courierId,
            LocalDateTime deliveredAt
    ) implements DeliveryEvent {}
}