// storage/OrderStorage.java
package edu.rutmiit.demo.orderservice.storage;

import edu.rutmiit.demo.darkkitchenapi.dto.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderStorage {

    private final Map<String, OrderResponse> orders = new ConcurrentHashMap<>();
    private final AtomicInteger orderSequence = new AtomicInteger(1000);

    // Меню (in-memory)
    private final Map<String, MenuItem> menu = new ConcurrentHashMap<>();

    public OrderStorage() {
        initMenu();
    }

    private void initMenu() {
        menu.put("burger-001", new MenuItem("burger-001", "Классический бургер", 350.0));
        menu.put("burger-002", new MenuItem("burger-002", "Чизбургер", 400.0));
        menu.put("pizza-001", new MenuItem("pizza-001", "Маргарита", 550.0));
        menu.put("pizza-002", new MenuItem("pizza-002", "Пепперони", 650.0));
        menu.put("salad-001", new MenuItem("salad-001", "Цезарь", 300.0));
        menu.put("drink-001", new MenuItem("drink-001", "Кола", 100.0));
        menu.put("drink-002", new MenuItem("drink-002", "Морс", 120.0));
    }

    public String generateOrderId() {
        return "ORD-" + orderSequence.incrementAndGet();
    }

    public OrderResponse save(OrderResponse order) {
        orders.put(order.getOrderId(), order);
        return order;
    }

    public Optional<OrderResponse> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public List<OrderResponse> findAll(String statusFilter) {
        return orders.values().stream()
                .filter(o -> statusFilter == null || o.getStatus().equals(statusFilter))
                .sorted(Comparator.comparing(OrderResponse::getCreatedAt).reversed())
                .toList();
    }

    public void delete(String orderId) {
        orders.remove(orderId);
    }

    public Optional<MenuItem> getMenuItem(String menuItemId) {
        return Optional.ofNullable(menu.get(menuItemId));
    }

    public record MenuItem(String id, String name, double price) {}
}