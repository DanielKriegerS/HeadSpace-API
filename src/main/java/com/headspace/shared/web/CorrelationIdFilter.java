package com.headspace.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String MDC_KEY = "correlationId";
    public static final String HEADER_NAME = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);
        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    public static String resolveCorrelationId(HttpServletRequest request) {
        String incoming = request.getHeader(HEADER_NAME);
        if (isValid(incoming)) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (value.length() > 128) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char charAt = value.charAt(i);
            if (Character.isISOControl(charAt)) {
                return false;
            }
            if (charAt == ' ' || charAt == '\t' || charAt == '\n' || charAt == '\r') {
                return false;
            }
        }
        return value.matches("[A-Za-z0-9\\-_.]+$");
    }
}
