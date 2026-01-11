package com.example.gateway.config;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration để aggregate Swagger docs từ tất cả microservices
 * thông qua Eureka Service Discovery
 */
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final DiscoveryClient discoveryClient;

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();

        // Get all services from Eureka and create Swagger URLs
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();

        discoveryClient.getServices().forEach(serviceName -> {
            // Skip api-gateway and discovery-server
            if (!serviceName.equals("api-gateway") && !serviceName.equals("discovery-server")) {
                AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl();
                swaggerUrl.setName(serviceName);
                // Route to service's api-docs through gateway
                swaggerUrl.setUrl("/api/auth/v3/api-docs");
                urls.add(swaggerUrl);
            }
        });

        properties.setUrls(urls);
        return properties;
    }

    @Bean
    @Primary
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("API Gateway"));
    }
}
