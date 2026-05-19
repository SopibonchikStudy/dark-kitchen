package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на обновление статуса")
public record StatusUpdateRequest(
        @Schema(description = "Новый статус", example = "COOKING")
        @NotBlank(message = "Статус обязателен")
        String status,

        @Schema(description = "Сообщение к статусу", example = "Заказ готовится")
        String message
) {}