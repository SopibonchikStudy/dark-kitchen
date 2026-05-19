package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о статусе доставки")
public record DeliveryStatusInfo(
        @Schema(description = "ID заказа", example = "ORD-1001")
        String orderId,

        @Schema(description = "Статус доставки", example = "DELIVERING")
        String status,

        @Schema(description = "ID курьера", nullable = true)
        String courierId,

        @Schema(description = "Имя курьера", nullable = true)
        String courierName,

        @Schema(description = "Примерное время доставки в минутах", example = "10")
        int estimatedMinutes
) {}