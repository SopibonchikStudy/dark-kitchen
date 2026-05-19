package edu.rutmiit.demo.orderservice.graphql.types;

public record StatusUpdateInput(
        String status,
        String message
) {}