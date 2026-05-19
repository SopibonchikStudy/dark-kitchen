package edu.rutmiit.demo.orderservice.controller;

// Импорт из КОНТРАКТА
import edu.rutmiit.demo.darkkitchenapi.dto.OrderRequest;
import edu.rutmiit.demo.darkkitchenapi.dto.OrderResponse;
import edu.rutmiit.demo.darkkitchenapi.dto.StatusUpdateRequest;
import edu.rutmiit.demo.darkkitchenapi.endpoints.OrderApi;

// НЕ создавайте локальные копии DTO!
import edu.rutmiit.demo.orderservice.assembler.OrderModelAssembler;
import edu.rutmiit.demo.orderservice.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final OrderModelAssembler assembler;

    public OrderController(OrderService orderService, OrderModelAssembler assembler) {
        this.orderService = orderService;
        this.assembler = assembler;
    }

    @Override
    public ResponseEntity<EntityModel<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        EntityModel<OrderResponse> model = assembler.toModel(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Override
    public EntityModel<OrderResponse> getOrderById(String orderId) {
        return assembler.toModel(orderService.findById(orderId));
    }

    @Override
    public ResponseEntity<List<EntityModel<OrderResponse>>> getAllOrders(String status) {
        List<EntityModel<OrderResponse>> orders = orderService.findAll(status)
                .stream()
                .map(assembler::toModel)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @Override
    public EntityModel<OrderResponse> cancelOrder(String orderId) {
        return assembler.toModel(orderService.cancelOrder(orderId));
    }

    @Override
    public EntityModel<OrderResponse> updateOrderStatus(
            String orderId, StatusUpdateRequest request) {
        return assembler.toModel(
                orderService.updateStatus(orderId, request.status(), request.message()));
    }


}