// OrderEvent.java
package edu.rutmiit.demo.darkkitchen.events;

import java.time.LocalDateTime;
import java.util.List;

public sealed interface OrderEvent {

    record Created(
            String orderId,
            String customerName,
            String customerPhone,
            String deliveryAddress,
            List<OrderItem> items,
            String notes,
            LocalDateTime createdAt
    ) implements OrderEvent {}

    record StatusUpdated(
            String orderId,
            String status,
            String message,
            LocalDateTime updatedAt
    ) implements OrderEvent {}

    record OrderItem(
            String menuItemId,
            String name,
            int quantity,
            double price
    ) {}
}