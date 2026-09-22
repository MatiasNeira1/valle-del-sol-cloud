package com.MunicipalidadDelValle.api_gateway.config;

import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {
    public static final String HEADER = "X-Correlation-ID";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (correlationId == null || correlationId.isBlank()) correlationId = UUID.randomUUID().toString();
        String finalId = correlationId;
        exchange.getResponse().getHeaders().set(HEADER, finalId);
        ServerWebExchange mutated = exchange.mutate().request(request -> request.headers(headers -> headers.set(HEADER, finalId))).build();
        return chain.filter(mutated);
    }
}
