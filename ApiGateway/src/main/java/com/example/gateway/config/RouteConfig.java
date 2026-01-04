package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

        @Bean
        public RouteLocator customRoutes(RouteLocatorBuilder builder) {
                return builder.routes()
                                // Route 1: AuthService - sử dụng load balancing với Eureka
                                .route("auth-service", r -> r
                                                .path("/api/auth/**")
                                                .uri("lb://auth-service"))

                                // Route 2: BookingService
                                .route("booking-service", r -> r
                                                .path("/api/booking/**")
                                                .uri("lb://booking-service"))

                                // Route 3: EventService
                                .route("event-service", r -> r
                                                .path("/api/event/**")
                                                .uri("lb://event-service"))

                                // Route 4: NotifyService
                                .route("notify-service", r -> r
                                                .path("/api/notify/**")
                                                .uri("lb://notify-service"))

                                // Route 5: PaymentService
                                .route("payment-service", r -> r
                                                .path("/api/payment/**")
                                                .uri("lb://payment-service"))

                                .build();
        }
}
