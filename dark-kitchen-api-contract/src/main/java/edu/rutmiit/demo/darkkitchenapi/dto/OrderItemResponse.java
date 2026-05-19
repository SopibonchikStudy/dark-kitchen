package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Позиция заказа в ответе")
public record OrderItemResponse(
        @Schema(description = "ID блюда из меню") String menuItemId,
        @Schema(description = "Название блюда") String name,
        @Schema(description = "Количество") int quantity,
        @Schema(description = "Цена за единицу") double price,
        @Schema(description = "Особые пожелания") String specialInstructions
) {}