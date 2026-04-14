package io.github.seaniestack.api_gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    // ── parseAndValidate ──────────────────────────────────────────────────────

    @Test
    void parseAndValidate_validToken_returnsCorrectClaims() {
        String token = buildToken("42", "ADMIN", 60_000);

        Claims claims = jwtService.parseAndValidate(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void parseAndValidate_studentRole_returnsCorrectRole() {
        String token = buildToken("7", "STUDENT", 60_000);

        Claims claims = jwtService.parseAndValidate(token);

        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
    }

    @Test
    void parseAndValidate_expiredToken_throwsJwtException() {
        String token = buildToken("1", "STUDENT", -1_000); // expired 1 s ago

        assertThatThrownBy(() -> jwtService.parseAndValidate(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseAndValidate_wrongSignature_throwsJwtException() {
        String differentSecret = "wrong-secret-key-that-is-long-enough-for-hmac-sha";
        SecretKey wrongKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("1")
                .claim("role", "ADMIN")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(60_000)))
                .signWith(wrongKey)
                .compact();

        assertThatThrownBy(() -> jwtService.parseAndValidate(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseAndValidate_malformedToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.parseAndValidate("not.a.jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseAndValidate_emptyString_throwsException() {
        assertThatThrownBy(() -> jwtService.parseAndValidate(""))
                .isInstanceOf(Exception.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static String buildToken(String subject, String role, long expirationOffsetMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationOffsetMs)))
                .signWith(key)
                .compact();
    }
}
