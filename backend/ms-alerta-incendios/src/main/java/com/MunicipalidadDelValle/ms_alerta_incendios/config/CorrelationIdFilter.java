package com.MunicipalidadDelValle.ms_alerta_incendios.config;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String id = request.getHeader("X-Correlation-ID");
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        response.setHeader("X-Correlation-ID", id);
        try { MDC.put("correlationId", id); chain.doFilter(request, response); } finally { MDC.remove("correlationId"); }
    }
}
