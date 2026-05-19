package edu.rutmiit.demo.orderservice.graphql.types;


public record OrderItemInput(
        String menuItemId,
        int quantity,
        String specialInstructions
) {}