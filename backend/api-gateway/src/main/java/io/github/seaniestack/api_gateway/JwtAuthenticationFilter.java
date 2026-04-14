package io.github.seaniestack.api_gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            if (userId == null || role == null) {
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            if (isAdminOnly(path, method) && !"ADMIN".equals(role)) {
                return reject(exchange, HttpStatus.FORBIDDEN, "Admin access required");
            }

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private boolean isPublicPath(String path, HttpMethod method) {
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }
        return path.startsWith("/api/auth")
                || path.startsWith("/api/fallback")
                || path.startsWith("/actuator/health")
                || path.startsWith("/api/core-service")
                || path.startsWith("/api/support-service");
    }

    private boolean isAdminOnly(String path, HttpMethod method) {
        if (!path.startsWith("/api/books")) {
            return false;
        }
        return HttpMethod.POST.equals(method)
                || HttpMethod.PUT.equals(method)
                || HttpMethod.DELETE.equals(method);
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
