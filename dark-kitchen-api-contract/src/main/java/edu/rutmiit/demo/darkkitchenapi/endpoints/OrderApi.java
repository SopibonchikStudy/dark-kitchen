package edu.rutmiit.demo.darkkitchenapi.endpoints;

import edu.rutmiit.demo.darkkitchenapi.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Управление заказами")
@RequestMapping("/api/orders")
public interface OrderApi {

    @Operation(summary = "Создать новый заказ")
    @ApiResponse(responseCode = "201", description = "Заказ создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EntityModel<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request);

    @Operation(summary = "Получить заказ по ID")
    @ApiResponse(responseCode = "200", description = "Заказ найден")
    @ApiResponse(responseCode = "404", description = "Заказ не найден")
    @GetMapping("/{orderId}")
    EntityModel<OrderResponse> getOrderById(
            @Parameter(description = "ID заказа", example = "ORD-12345")
            @PathVariable String orderId
    );

    @Operation(summary = "Получить все заказы")
    @ApiResponse(responseCode = "200", description = "Список заказов")
    @GetMapping
    ResponseEntity<List<EntityModel<OrderResponse>>> getAllOrders(
            @Parameter(description = "Фильтр по статусу")
            @RequestParam(required = false) String status
    );

    @Operation(summary = "Отменить заказ")
    @ApiResponse(responseCode = "200", description = "Заказ отменён")
    @ApiResponse(responseCode = "404", description = "Заказ не найден")
    @ApiResponse(responseCode = "409", description = "Заказ нельзя отменить в текущем статусе")
    @PostMapping("/{orderId}/cancel")
    EntityModel<OrderResponse> cancelOrder(
            @Parameter(description = "ID заказа") @PathVariable String orderId
    );

    @Operation(summary = "Обновить статус заказа")
    @ApiResponse(responseCode = "200", description = "Статус обновлён")
    @PutMapping("/{orderId}/status")
    EntityModel<OrderResponse> updateOrderStatus(
            @Parameter(description = "ID заказа") @PathVariable String orderId,
            @Valid @RequestBody StatusUpdateRequest request
    );


}