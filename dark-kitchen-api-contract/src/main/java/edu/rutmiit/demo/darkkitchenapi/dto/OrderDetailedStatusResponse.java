package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Детальный статус заказа с информацией от всех сервисов")
public record OrderDetailedStatusResponse(
        @Schema(description = "Основная информация о заказе")
        OrderResponse order,

        @Schema(description = "Статус приготовления от Kitchen Service", nullable = true)
        KitchenStatusInfo kitchenStatus,

        @Schema(description = "Статус доставки от Delivery Service", nullable = true)
        DeliveryStatusInfo deliveryStatus
) {}