package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Autowired
    private ServiceProperties serviceProperties;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route 1: AuthService
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri(serviceProperties.getAuthUrl()))

                // Route 2: BookingService
                .route("booking-service", r -> r
                        .path("/api/booking/**")
                        .uri(serviceProperties.getBookingUrl()))

                // Route 3: EventService
                .route("event-service", r -> r
                        .path("/api/event/**")
                        .uri(serviceProperties.getEventUrl()))

                // Route 4: NotifyService
                .route("notify-service", r -> r
                        .path("/api/notify/**")
                        .uri(serviceProperties.getNotifyUrl()))

                // Route 5: PaymentService
                .route("payment-service", r -> r
                        .path("/api/payment/**")
                        .uri(serviceProperties.getPaymentUrl()))

                .build();
    }
}

