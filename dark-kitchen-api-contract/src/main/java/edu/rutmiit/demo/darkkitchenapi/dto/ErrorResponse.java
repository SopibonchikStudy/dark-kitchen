package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Ответ с ошибкой")
public record ErrorResponse(
        @Schema(description = "HTTP статус код") int status,
        @Schema(description = "Тип ошибки") String type,
        @Schema(description = "Краткое описание") String title,
        @Schema(description = "Детальное описание") String detail,
        @Schema(description = "Путь запроса") String instance,
        @Schema(description = "Время ошибки") Instant timestamp,
        @Schema(description = "Ошибки валидации полей") List<FieldError> fieldErrors
) {
    public record FieldError(
            @Schema(description = "Имя поля") String field,
            @Schema(description = "Сообщение об ошибке") String message
    ) {}
}