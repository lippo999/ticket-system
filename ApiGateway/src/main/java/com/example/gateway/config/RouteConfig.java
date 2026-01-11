package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RouteConfig - Manual route configuration
 * Routes are explicitly defined instead of auto-discovery from Eureka
 */
@Configuration
public class RouteConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth Service - /api/auth/** -> lb://AUTHSERVICE/api/auth/**
            .route("authservice", r -> r
                .path("/api/auth/**")
                .uri("lb://AUTH-SERVICE"))
            
            // Booking Service
            .route("bookingservice", r -> r
                .path("/api/bookings/**")
                .uri("lb://BOOKINGSERVICE"))
            
            // Event Service
            .route("eventservice", r -> r
                .path("/api/events/**")
                .uri("lb://EVENTSERVICE"))
            
            // Notification Service
            .route("notifyservice", r -> r
                .path("/api/notifications/**")
                .uri("lb://NOTIFYSERVICE"))
            
            // Payment Service
            .route("paymentservice", r -> r
                .path("/api/payments/**")
                .uri("lb://PAYMENTSERVICE"))
            
            .build();
    }
}
