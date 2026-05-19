package edu.rutmiit.demo.orderservice.dto;

import edu.rutmiit.demo.darkkitchenapi.dto.OrderResponse;
import edu.rutmiit.demo.grpc.DeliveryStatusResponse;
import edu.rutmiit.demo.grpc.OrderStatusResponse;

/**
 * Детальный статус заказа с информацией от всех сервисов
 */
public record OrderDetailedStatus(
        OrderResponse order,
        OrderStatusResponse kitchenStatus,
        DeliveryStatusResponse deliveryStatus
) {}