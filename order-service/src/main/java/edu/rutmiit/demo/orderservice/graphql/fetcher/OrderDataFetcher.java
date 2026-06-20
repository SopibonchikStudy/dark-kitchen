package edu.rutmiit.demo.orderservice.graphql.fetcher;

import com.netflix.graphql.dgs.*;
import edu.rutmiit.demo.darkkitchenapi.dto.OrderResponse;
import edu.rutmiit.demo.grpc.EstimateCookingTimeResponse;
import edu.rutmiit.demo.orderservice.dto.OrderDetailedStatus;
import edu.rutmiit.demo.orderservice.graphql.types.OrderConnection;
import edu.rutmiit.demo.orderservice.graphql.types.OrderFilterInput;
import edu.rutmiit.demo.orderservice.service.OrderService;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;

@DgsComponent
public class OrderDataFetcher {

    private final OrderService orderService;

    public OrderDataFetcher(OrderService orderService) {
        this.orderService = orderService;
    }

    @DgsQuery
    public OrderResponse order(@InputArgument String orderId) {
        return orderService.findById(orderId);
    }

    @DgsQuery
    public OrderDetailedStatus orderDetailed(@InputArgument String orderId) {
        return orderService.getDetailedStatus(orderId);
    }

    @DgsQuery
    public CookingTimeResult estimateCookingTime(@InputArgument List<String> menuItemIds) {
        EstimateCookingTimeResponse grpcResponse = orderService.estimateCookingTime(menuItemIds);

        List<CookingItem> items = grpcResponse.getItemsList().stream()
                .map(item -> new CookingItem(item.getMenuItemId(), item.getSeconds()))
                .toList();

        return new CookingTimeResult(grpcResponse.getTotalSeconds(), items);
    }

    // Вспомогательные record'ы
    public record CookingTimeResult(int totalSeconds, List<CookingItem> items) {}
    public record CookingItem(String menuItemId, int seconds) {}

    @DgsQuery
    public OrderConnection orders(
            @InputArgument OrderFilterInput filter,
            @InputArgument int page,
            @InputArgument int size) {

        List<OrderResponse> allOrders = orderService.findAll(filter != null ? filter.status() : null);

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