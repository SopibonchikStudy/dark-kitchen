package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о курьере")
public record CourierInfo(
        @Schema(description = "ID курьера") String courierId,
        @Schema(description = "Имя курьера") String courierName,
        @Schema(description = "Телефон курьера") String courierPhone
) {}