package com.example.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * RouteConfig - Cấu hình routes động từ Eureka
 * 
 * Với discovery.locator.enabled=true, API Gateway sẽ tự động tạo routes cho tất cả services
 * đăng ký trên Eureka với pattern: /{service-name}/**
 * 
 * Ví dụ:
 * - /auth-service/** -> lb://auth-service
 * - /booking-service/** -> lb://booking-service
 * - /event-service/** -> lb://event-service
 * - /notify-service/** -> lb://notify-service
 * - /payment-service/** -> lb://payment-service
 * 
 * Nếu cần custom routes hoặc filters, có thể thêm @Bean RouteLocator ở đây.
 */
@Configuration
public class RouteConfig {
    // Routes được tạo tự động từ Eureka Discovery
    // Không cần hardcode nữa!
}
