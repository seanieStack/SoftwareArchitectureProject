package io.github.seaniestack.api_gateway;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig implements WebFilter {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
            // TODO: Add deployed URL when deploying
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders responseHeaders = response.getHeaders();

        String origin = request.getHeaders().getOrigin();
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            responseHeaders.set("Access-Control-Allow-Origin", origin);
            responseHeaders.set("Access-Control-Allow-Credentials", "true");
            responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            responseHeaders.set("Access-Control-Allow-Headers", "Authorization, Content-Type");
            responseHeaders.set("Access-Control-Max-Age", "3600");
        }

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            response.setStatusCode(HttpStatus.OK);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }
}
