package edu.rutmiit.demo.orderservice.controller;

import edu.rutmiit.demo.darkkitchenapi.dto.*;
import edu.rutmiit.demo.darkkitchenapi.endpoints.OrderGrpcApi;
import edu.rutmiit.demo.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderGrpcController implements OrderGrpcApi {

    private final OrderService orderService;

    public OrderGrpcController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public OrderDetailedStatusResponse getDetailedStatus(String orderId) {
        var status = orderService.getDetailedStatus(orderId);

        KitchenStatusInfo kitchenInfo = null;
        if (status.kitchenStatus() != null) {
            kitchenInfo = new KitchenStatusInfo(
                    status.kitchenStatus().getOrderId(),
                    status.kitchenStatus().getStatus(),
                    status.kitchenStatus().getMessage(),
                    status.kitchenStatus().getEstimatedSeconds()
            );
        }

        DeliveryStatusInfo deliveryInfo = null;
        if (status.deliveryStatus() != null) {
            deliveryInfo = new DeliveryStatusInfo(
                    status.deliveryStatus().getOrderId(),
                    status.deliveryStatus().getStatus(),
                    status.deliveryStatus().getCourierId(),
                    status.deliveryStatus().getCourierName(),
                    status.deliveryStatus().getEstimatedMinutes()
            );
        }

        return new OrderDetailedStatusResponse(status.order(), kitchenInfo, deliveryInfo);
    }

    @Override
    public CookingTimeEstimationResponse estimateCookingTime(List<String> menuItemIds) {
        var grpcResponse = orderService.estimateCookingTime(menuItemIds);

        List<ItemCookingTimeInfo> items = grpcResponse.getItemsList().stream()
                .map(item -> new ItemCookingTimeInfo(item.getMenuItemId(), item.getSeconds()))
                .toList();

        return new CookingTimeEstimationResponse(grpcResponse.getTotalSeconds(), items);
    }
}