package edu.rutmiit.demo.orderservice.graphql.types;

import java.util.List;

public record CreateOrderInput(
        String customerName,
        String customerPhone,
        String deliveryAddress,
        List<OrderItemInput> items,
        String notes
) {}
