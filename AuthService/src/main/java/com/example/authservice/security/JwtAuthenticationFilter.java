package com.example.authservice.security;

import com.example.authservice.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT Authentication Filter to extract and validate JWT tokens from requests.
 * This filter runs once per request and sets up the SecurityContext with user permissions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Claims claims = jwtService.validateTokenAndGetClaims(token);
                
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                List<String> permissions = claims.get("permissions", List.class);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Create authorities from permissions
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    
                    // Add role as authority (prefixed with ROLE_)
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                    
                    // Add permissions as authorities
                    if (permissions != null) {
                        permissions.forEach(permission -> 
                            authorities.add(new SimpleGrantedAuthority(permission))
                        );
                    }
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT authentication successful for user: {}, authorities: {}", username, authorities);
                }
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                // Don't set authentication, let the request continue (will be blocked by security rules)
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
