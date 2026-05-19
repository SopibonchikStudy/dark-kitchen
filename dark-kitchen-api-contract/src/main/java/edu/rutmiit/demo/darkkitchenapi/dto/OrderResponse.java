package edu.rutmiit.demo.darkkitchenapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Ответ с информацией о заказе")
public class OrderResponse extends RepresentationModel<OrderResponse> {

    @Schema(description = "Уникальный идентификатор заказа", example = "ORD-12345")
    private final String orderId;

    @Schema(description = "Имя клиента", example = "Иван Петров")
    private final String customerName;

    @Schema(description = "Телефон клиента", example = "+79001234567")
    private final String customerPhone;

    @Schema(description = "Адрес доставки", example = "ул. Пушкина, д. 10, кв. 5")
    private final String deliveryAddress;

    @Schema(description = "Список позиций заказа")
    private final List<OrderItemResponse> items;

    @Schema(description = "Примечания к заказу")
    private final String notes;

    @Schema(description = "Текущий статус заказа", example = "COOKING")
    private final String status;

    @Schema(description = "Общая стоимость заказа", example = "1250.50")
    private final double totalAmount;

    @Schema(description = "Курьер (если назначен)")
    private final CourierInfo courier;

    @Schema(description = "История изменения статусов")
    private final List<StatusHistoryEntry> statusHistory;

    @Schema(description = "Дата создания заказа")
    private final LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления")
    private final LocalDateTime updatedAt;

    public OrderResponse(String orderId, String customerName, String customerPhone,
                         String deliveryAddress, List<OrderItemResponse> items,
                         String notes, String status, double totalAmount,
                         CourierInfo courier, List<StatusHistoryEntry> statusHistory,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.deliveryAddress = deliveryAddress;
        this.items = items;
        this.notes = notes;
        this.status = status;
        this.totalAmount = totalAmount;
        this.courier = courier;
        this.statusHistory = statusHistory;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Геттеры
    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public List<OrderItemResponse> getItems() { return items; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public CourierInfo getCourier() { return courier; }
    public List<StatusHistoryEntry> getStatusHistory() { return statusHistory; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String orderId;
        private String customerName;
        private String customerPhone;
        private String deliveryAddress;
        private List<OrderItemResponse> items;
        private String notes;
        private String status;
        private double totalAmount;
        private CourierInfo courier;
        private List<StatusHistoryEntry> statusHistory;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder orderId(String orderId) { this.orderId = orderId; return this; }
        public Builder customerName(String customerName) { this.customerName = customerName; return this; }
        public Builder customerPhone(String customerPhone) { this.customerPhone = customerPhone; return this; }
        public Builder deliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; return this; }
        public Builder items(List<OrderItemResponse> items) { this.items = items; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder totalAmount(double totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder courier(CourierInfo courier) { this.courier = courier; return this; }
        public Builder statusHistory(List<StatusHistoryEntry> statusHistory) { this.statusHistory = statusHistory; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OrderResponse build() {
            return new OrderResponse(orderId, customerName, customerPhone, deliveryAddress,
                    items, notes, status, totalAmount, courier, statusHistory,
                    createdAt, updatedAt);
        }
    }
}