package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Запись в истории статусов")
public record StatusHistoryEntry(
        @Schema(description = "Статус") String status,
        @Schema(description = "Сообщение") String message,
        @Schema(description = "Время изменения") LocalDateTime timestamp
) {}