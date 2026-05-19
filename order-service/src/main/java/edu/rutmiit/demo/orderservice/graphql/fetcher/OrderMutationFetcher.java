package edu.rutmiit.demo.orderservice.graphql.fetcher;

import edu.rutmiit.demo.darkkitchenapi.dto.*;
import edu.rutmiit.demo.orderservice.graphql.types.CreateOrderInput;
import edu.rutmiit.demo.orderservice.graphql.types.OrderFilterInput;
import edu.rutmiit.demo.orderservice.graphql.types.StatusUpdateInput;
import edu.rutmiit.demo.orderservice.service.OrderService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class OrderMutationFetcher {

    private final OrderService orderService;

    public OrderMutationFetcher(OrderService orderService) {
        this.orderService = orderService;
    }

    @MutationMapping
    public OrderResponse createOrder(@Argument CreateOrderInput input) {
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

    @MutationMapping
    public OrderResponse cancelOrder(@Argument String orderId) {
        return orderService.cancelOrder(orderId);
    }

    @MutationMapping
    public OrderResponse updateOrderStatus(@Argument String orderId, @Argument StatusUpdateInput status) {
        return orderService.updateStatus(orderId, status.status(), status.message());
    }

    @MutationMapping
    public boolean deleteOrder(@Argument String orderId) {
        try {
            orderService.cancelOrder(orderId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}