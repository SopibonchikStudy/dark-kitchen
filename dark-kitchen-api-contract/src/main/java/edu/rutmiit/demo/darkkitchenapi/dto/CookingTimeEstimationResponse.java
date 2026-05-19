package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Оценка времени приготовления")
public record CookingTimeEstimationResponse(
        @Schema(description = "Общее время в секундах", example = "900")
        int totalSeconds,

        @Schema(description = "Время приготовления каждого блюда")
        List<ItemCookingTimeInfo> items
) {}