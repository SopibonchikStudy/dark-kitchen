// RoutingKeys.java
package edu.rutmiit.demo.darkkitchen.events;

public final class RoutingKeys {
    private RoutingKeys() {}

    public static final String EXCHANGE = "darkkitchen.events";

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_STATUS_UPDATED = "order.status.updated";
    public static final String ORDER_CANCELLED = "order.cancelled";

    public static final String KITCHEN_COOKING_STARTED = "kitchen.cooking.started";
    public static final String KITCHEN_COOKING_COMPLETED = "kitchen.cooking.completed";

    public static final String DELIVERY_COURIER_ASSIGNED = "delivery.courier.assigned";
    public static final String DELIVERY_STARTED = "delivery.started";
    public static final String DELIVERY_COMPLETED = "delivery.completed";
}