package edu.rutmiit.demo.orderservice.controller;

import edu.rutmiit.demo.orderservice.dto.CookingTimeEstimation;
import edu.rutmiit.demo.orderservice.dto.ItemTime;
import edu.rutmiit.demo.orderservice.dto.OrderDetailedStatus;
import edu.rutmiit.demo.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderGrpcController {

    private final OrderService orderService;

    public OrderGrpcController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}/detailed")
    public OrderDetailedStatus getDetailedStatus(@PathVariable String orderId) {
        return orderService.getDetailedStatus(orderId);
    }

    @PostMapping("/estimate-cooking-time")
    public CookingTimeEstimation estimateCookingTime(@RequestBody List<String> menuItemIds) {
        var grpcResponse = orderService.estimateCookingTime(menuItemIds);

        List<ItemTime> items = grpcResponse.getItemsList().stream()
                .map(item -> new ItemTime(item.getMenuItemId(), item.getSeconds()))
                .toList();

        return new CookingTimeEstimation(grpcResponse.getTotalSeconds(), items);
    }
}