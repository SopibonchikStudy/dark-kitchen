package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о статусе приготовления")
public record KitchenStatusInfo(
        @Schema(description = "ID заказа", example = "ORD-1001")
        String orderId,

        @Schema(description = "Статус приготовления", example = "COOKING")
        String status,

        @Schema(description = "Сообщение от кухни", example = "Заказ готовится")
        String message,

        @Schema(description = "Оставшееся время в секундах", example = "180")
        int estimatedSeconds
) {}