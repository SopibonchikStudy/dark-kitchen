package edu.rutmiit.demo.orderservice.service;

import edu.rutmiit.demo.darkkitchenapi.dto.*;
import edu.rutmiit.demo.grpc.DeliveryStatusResponse;
import edu.rutmiit.demo.grpc.EstimateCookingTimeResponse;
import edu.rutmiit.demo.grpc.OrderStatusResponse;
import edu.rutmiit.demo.orderservice.dto.OrderDetailedStatus;
import edu.rutmiit.demo.orderservice.event.OrderEventPublisher;
import edu.rutmiit.demo.orderservice.exception.ResourceNotFoundException;
import edu.rutmiit.demo.orderservice.grpc.DeliveryGrpcClient;
import edu.rutmiit.demo.orderservice.grpc.KitchenGrpcClient;
import edu.rutmiit.demo.orderservice.storage.OrderStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderStorage storage;
    private final OrderEventPublisher eventPublisher;
    private final KitchenGrpcClient kitchenGrpcClient;      // ← Добавлено
    private final DeliveryGrpcClient deliveryGrpcClient;    // ← Добавлено

    public OrderService(OrderStorage storage,
                        OrderEventPublisher eventPublisher,
                        KitchenGrpcClient kitchenGrpcClient,        // ← Добавлено
                        DeliveryGrpcClient deliveryGrpcClient) {    // ← Добавлено
        this.storage = storage;
        this.eventPublisher = eventPublisher;
        this.kitchenGrpcClient = kitchenGrpcClient;
        this.deliveryGrpcClient = deliveryGrpcClient;
    }

    public OrderResponse createOrder(OrderRequest request) {
        String orderId = storage.generateOrderId();

        List<OrderItemResponse> items = new ArrayList<>();
        double totalAmount = 0;

        for (OrderItemRequest itemRequest : request.items()) {
            Optional<OrderStorage.MenuItem> menuItem = storage.getMenuItem(itemRequest.menuItemId());

            OrderItemResponse item = new OrderItemResponse(
                    itemRequest.menuItemId(),
                    menuItem.map(OrderStorage.MenuItem::name).orElse("Неизвестное блюдо"),
                    itemRequest.quantity(),
                    menuItem.map(OrderStorage.MenuItem::price).orElse(0.0),
                    itemRequest.specialInstructions()
            );
            items.add(item);
            totalAmount += item.price() * item.quantity();
        }

        OrderResponse order = OrderResponse.builder()
                .orderId(orderId)
                .customerName(request.customerName())
                .customerPhone(request.customerPhone())
                .deliveryAddress(request.deliveryAddress())
                .items(items)
                .notes(request.notes())
                .status("NEW")
                .totalAmount(totalAmount)
                .courier(null)
                .statusHistory(new ArrayList<>(List.of(
                        new StatusHistoryEntry("NEW", "Заказ создан", LocalDateTime.now())
                )))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        storage.save(order);
        eventPublisher.publishOrderCreated(order);

        return order;
    }

    public OrderResponse findById(String orderId) {
        return storage.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    public List<OrderResponse> findAll(String status) {
        return storage.findAll(status);
    }

    public OrderResponse cancelOrder(String orderId) {
        OrderResponse order = findById(orderId);

        if (!List.of("NEW", "COOKING").contains(order.getStatus())) {
            throw new IllegalStateException("Заказ в статусе " + order.getStatus() + " нельзя отменить");
        }

        return updateStatus(orderId, "CANCELLED", "Заказ отменён клиентом");
    }

    public OrderResponse updateStatus(String orderId, String status, String message) {
        OrderResponse order = findById(orderId);

        List<StatusHistoryEntry> history = new ArrayList<>(order.getStatusHistory());
        history.add(new StatusHistoryEntry(status, message, LocalDateTime.now()));

        OrderResponse updated = OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .items(order.getItems())
                .notes(order.getNotes())
                .status(status)
                .totalAmount(order.getTotalAmount())
                .courier(order.getCourier())
                .statusHistory(history)
                .createdAt(order.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        storage.save(updated);
        eventPublisher.publishOrderStatusUpdated(updated);

        return updated;
    }

    public OrderResponse updateOrderWithCourier(String orderId, CourierInfo courier, String status, String message) {
        OrderResponse order = findById(orderId);

        List<StatusHistoryEntry> history = new ArrayList<>(order.getStatusHistory());
        history.add(new StatusHistoryEntry(status, message, LocalDateTime.now()));

        OrderResponse updated = OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .items(order.getItems())
                .notes(order.getNotes())
                .status(status)
                .totalAmount(order.getTotalAmount())
                .courier(courier)
                .statusHistory(history)
                .createdAt(order.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        storage.save(updated);
        eventPublisher.publishOrderStatusUpdated(updated);

        return updated;
    }

    // ==========================================
    // НОВЫЕ МЕТОДЫ С gRPC
    // ==========================================

    /**
     * Получить детальный статус заказа с информацией от кухни и доставки через gRPC
     */
    public OrderDetailedStatus getDetailedStatus(String orderId) {
        OrderResponse order = findById(orderId);

        // gRPC запрос к кухне
        OrderStatusResponse kitchenStatus = null;
        try {
            kitchenStatus = kitchenGrpcClient.getOrderStatus(orderId);
            log.info("Получен статус кухни для {}: {}", orderId, kitchenStatus.getStatus());
        } catch (Exception e) {
            log.error("Ошибка gRPC запроса к кухне для {}: {}", orderId, e.getMessage());
        }

        // gRPC запрос к доставке (только если заказ уже доставляется)
        DeliveryStatusResponse deliveryStatus = null;
        if (List.of("ASSIGNED", "DELIVERING", "DELIVERED").contains(order.getStatus())) {
            try {
                deliveryStatus = deliveryGrpcClient.getDeliveryStatus(orderId);
                log.info("Получен статус доставки для {}: {}", orderId, deliveryStatus.getStatus());
            } catch (Exception e) {
                log.error("Ошибка gRPC запроса к доставке для {}: {}", orderId, e.getMessage());
            }
        }

        return new OrderDetailedStatus(order, kitchenStatus, deliveryStatus);
    }

    /**
     * Оценить время приготовления блюд ДО создания заказа
     */
    public EstimateCookingTimeResponse estimateCookingTime(List<String> menuItemIds) {
        try {
            EstimateCookingTimeResponse response = kitchenGrpcClient.estimateCookingTime(menuItemIds);
            log.info("Оценка времени приготовления: {} сек для {} блюд",
                    response.getTotalSeconds(), menuItemIds.size());
            return response;
        } catch (Exception e) {
            log.error("Ошибка оценки времени приготовления: {}", e.getMessage());
            throw new RuntimeException("Не удалось оценить время приготовления", e);
        }
    }
}