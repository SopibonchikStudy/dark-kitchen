package edu.rutmiit.demo.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Order Service
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("http://localhost:8081"))

                // Order Service API Docs
                .route("order-api-docs", r -> r
                        .path("/order-service-api-docs")
                        .filters(f -> f.rewritePath("/order-service-api-docs", "/api-docs"))
                        .uri("http://localhost:8081"))

                // Notification Service
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://localhost:8084"))

                // Notification Service API Docs
                .route("notification-api-docs", r -> r
                        .path("/notification-service-api-docs")
                        .filters(f -> f.rewritePath("/notification-service-api-docs", "/api-docs"))
                        .uri("http://localhost:8084"))

                .build();
    }
}