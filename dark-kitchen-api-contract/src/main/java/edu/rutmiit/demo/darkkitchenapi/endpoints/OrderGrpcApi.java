package edu.rutmiit.demo.darkkitchenapi.endpoints;

import edu.rutmiit.demo.darkkitchenapi.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders gRPC", description = "Детальная информация о заказах через gRPC")
@RequestMapping("/api/orders")
public interface OrderGrpcApi {

    @Operation(
            summary = "Получить детальный статус заказа",
            description = "Возвращает полную информацию о заказе, включая статус приготовления и доставки"
    )
    @ApiResponse(responseCode = "200", description = "Детальный статус заказа")
    @ApiResponse(responseCode = "404", description = "Заказ не найден")
    @GetMapping("/{orderId}/detailed")
    OrderDetailedStatusResponse getDetailedStatus(
            @Parameter(description = "ID заказа", example = "ORD-1001")
            @PathVariable String orderId
    );

    @Operation(
            summary = "Оценить время приготовления блюд",
            description = "Отправляет запрос в Kitchen Service через gRPC для оценки времени приготовления"
    )
    @ApiResponse(responseCode = "200", description = "Оценка времени приготовления")
    @ApiResponse(responseCode = "500", description = "Ошибка gRPC соединения")
    @PostMapping("/estimate-cooking-time")
    CookingTimeEstimationResponse estimateCookingTime(
            @Parameter(description = "Список ID блюд", example = "[\"burger-001\", \"pizza-001\", \"salad-001\"]")
            @RequestBody List<String> menuItemIds
    );
}