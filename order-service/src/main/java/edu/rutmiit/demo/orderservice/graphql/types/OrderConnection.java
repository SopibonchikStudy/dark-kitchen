package edu.rutmiit.demo.orderservice.graphql.types;

import edu.rutmiit.demo.darkkitchenapi.dto.OrderResponse;

import java.util.List;

public record OrderConnection(
        List<OrderResponse> content,
        int totalElements,
        int pageNumber,
        int pageSize,
        int totalPages
) {}