package com.aegis.agent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    private final AegisProperties properties;

    public ApiKeyAuthFilter(AegisProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isApiAuthEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String configuredKey = properties.getApiAuthKey();
        String incomingKey = request.getHeader(API_KEY_HEADER);
        if (configuredKey == null || configuredKey.isBlank() || !configuredKey.equals(incomingKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Valid API key is required\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
