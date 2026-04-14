package io.github.seaniestack.api_gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway-wide JWT verification filter.
 *
 * <p>Runs at highest precedence so it executes before circuit-breaker and routing filters.
 * On a valid token it strips any caller-supplied spoofing headers and replaces them with
 * values extracted from the verified JWT before forwarding to downstream services.
 *
 * <p>Public paths ({@code /api/auth/**}, {@code /actuator/**}) bypass token verification
 * but still have spoofing headers stripped.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Always strip caller-supplied identity headers to prevent spoofing
        ServerHttpRequest stripped = request.mutate()
                .headers(h -> {
                    h.remove(HEADER_USER_ID);
                    h.remove(HEADER_USER_ROLE);
                })
                .build();

        if (isPublicPath(stripped)) {
            return chain.filter(exchange.mutate().request(stripped).build());
        }

        String authHeader = stripped.getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return writeUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            return writeUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        try {
            Claims claims = jwtService.parseAndValidate(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            if (!StringUtils.hasText(userId) || !StringUtils.hasText(role)) {
                return writeUnauthorized(exchange, "Invalid token claims");
            }

            // Forward verified identity to downstream services
            ServerHttpRequest forwarded = stripped.mutate()
                    .header(HEADER_USER_ID, userId)
                    .header(HEADER_USER_ROLE, role)
                    .build();

            return chain.filter(exchange.mutate().request(forwarded).build());

        } catch (JwtException | IllegalArgumentException e) {
            return writeUnauthorized(exchange, "Invalid or expired token");
        }
    }

    private static boolean isPublicPath(ServerHttpRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return true;
        }
        String path = request.getURI().getPath();
        return path.startsWith("/api/auth")
                || path.startsWith("/api/core-service")
                || path.startsWith("/api/support-service")
                || path.startsWith("/actuator");
    }

    private static Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + escapeJson(message) + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
