package edu.rutmiit.demo.orderservice.graphql.fetcher;

import com.netflix.graphql.dgs.*;
import edu.rutmiit.demo.darkkitchenapi.dto.*;
import edu.rutmiit.demo.orderservice.graphql.types.CreateOrderInput;
import edu.rutmiit.demo.orderservice.graphql.types.StatusUpdateInput;
import edu.rutmiit.demo.orderservice.service.OrderService;

import java.util.List;

@DgsComponent
public class OrderMutationFetcher {

    private final OrderService orderService;

    public OrderMutationFetcher(OrderService orderService) {
        this.orderService = orderService;
    }

    @DgsMutation
    public OrderResponse createOrder(@InputArgument CreateOrderInput input) {
        OrderRequest request = new OrderRequest(
                input.customerName(),
                input.customerPhone(),
                input.deliveryAddress(),
                input.items().stream()
                        .map(item -> new OrderItemRequest(
                                item.menuItemId(),
                                item.quantity(),
                                item.specialInstructions()))
                        .toList(),
                input.notes()
        );
        return orderService.createOrder(request);
    }

    @DgsMutation
    public OrderResponse cancelOrder(@InputArgument String orderId) {
        return orderService.cancelOrder(orderId);
    }

    @DgsMutation
    public OrderResponse updateOrderStatus(
            @InputArgument String orderId,
            @InputArgument StatusUpdateInput status) {
        return orderService.updateStatus(orderId, status.status(), status.message());
    }
}