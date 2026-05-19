// OrderRequest.java
package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Запрос на создание заказа")
public record OrderRequest(
        @Schema(description = "Имя клиента", example = "Иван Петров")
        @NotBlank(message = "Имя клиента обязательно")
        @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
        String customerName,

        @Schema(description = "Телефон клиента", example = "+79001234567")
        @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен быть в формате +7XXXXXXXXXX")
        String customerPhone,

        @Schema(description = "Адрес доставки", example = "ул. Пушкина, д. 10, кв. 5")
        @NotBlank(message = "Адрес доставки обязателен")
        @Size(min = 5, max = 200, message = "Адрес должен быть от 5 до 200 символов")
        String deliveryAddress,

        @Schema(description = "Список позиций заказа")
        @NotEmpty(message = "Заказ должен содержать хотя бы одну позицию")
        @Valid
        List<OrderItemRequest> items,

        @Schema(description = "Примечания к заказу", example = "Без лука, пожалуйста")
        @Size(max = 500, message = "Примечания не должны превышать 500 символов")
        String notes
) {}