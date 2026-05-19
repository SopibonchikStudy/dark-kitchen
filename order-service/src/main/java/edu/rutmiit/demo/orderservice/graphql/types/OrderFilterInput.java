package edu.rutmiit.demo.orderservice.graphql.types;

public record OrderFilterInput(
        String status,
        String customerName
) {}