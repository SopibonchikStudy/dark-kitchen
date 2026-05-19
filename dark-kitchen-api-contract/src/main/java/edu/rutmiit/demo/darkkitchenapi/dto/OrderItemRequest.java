package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Позиция заказа")
public record OrderItemRequest(
        @Schema(description = "ID блюда из меню", example = "burger-001")
        @NotBlank(message = "ID блюда обязателен")
        String menuItemId,

        @Schema(description = "Количество порций", example = "2")
        @Min(value = 1, message = "Количество должно быть не менее 1")
        int quantity,

        @Schema(description = "Особые пожелания к блюду", example = "Без сыра")
        String specialInstructions
) {}