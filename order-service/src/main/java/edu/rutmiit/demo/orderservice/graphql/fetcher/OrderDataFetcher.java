package edu.rutmiit.demo.orderservice.graphql.fetcher;

import edu.rutmiit.demo.darkkitchenapi.dto.OrderResponse;
import edu.rutmiit.demo.orderservice.graphql.types.OrderConnection;
import edu.rutmiit.demo.orderservice.graphql.types.OrderFilterInput;
import edu.rutmiit.demo.orderservice.service.OrderService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class OrderDataFetcher {

    private final OrderService orderService;

    public OrderDataFetcher(OrderService orderService) {
        this.orderService = orderService;
    }

    @QueryMapping
    public OrderResponse order(@Argument String orderId) {
        return orderService.findById(orderId);
    }

    @QueryMapping
    public OrderConnection orders(
            @Argument OrderFilterInput filter,
            @Argument int page,
            @Argument int size) {

        List<OrderResponse> allOrders = orderService.findAll(filter != null ? filter.status() : null);

        // Пагинация
        int totalElements = allOrders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int from = page * size;
        int to = Math.min(from + size, totalElements);

        List<OrderResponse> content;
        if (from >= totalElements) {
            content = List.of();
        } else {
            content = allOrders.subList(from, to);
        }

        return new OrderConnection(content, totalElements, page, size, totalPages);
    }
}