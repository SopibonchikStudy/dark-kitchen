package edu.rutmiit.demo.deliveryservice.model;

/**
 * Модель курьера для сервиса доставки.
 * Доступна для использования из других пакетов.
 */
public record Courier(
        String id,
        String name,
        String phone
) {}