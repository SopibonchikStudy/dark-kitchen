package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Время приготовления одного блюда")
public record ItemCookingTimeInfo(
        @Schema(description = "ID блюда", example = "burger-001")
        String menuItemId,

        @Schema(description = "Время приготовления в секундах", example = "300")
        int seconds
) {}