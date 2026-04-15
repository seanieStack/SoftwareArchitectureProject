package io.github.seaniestack.coreservice.security;

import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-characters!!";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    //generateToken()

    @Test
    @DisplayName("generateToken returns a non-blank JWT string")
    void generateToken_returnsNonBlankToken() {
        String token = jwtService.generateToken(makeUser(1L, UserRole.STAFF));
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateToken produces different tokens for different users")
    void generateToken_differentUsers_differentTokens() {
        String token1 = jwtService.generateToken(makeUser(1L, UserRole.STAFF));
        String token2 = jwtService.generateToken(makeUser(2L, UserRole.STUDENT));
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("generateToken embeds user ID as subject")
    void generateToken_subjectIsUserId() {
        String token = jwtService.generateToken(makeUser(42L, UserRole.STAFF));
        Claims claims = jwtService.parseAndValidate(token);
        assertThat(claims.getSubject()).isEqualTo("42");
    }

    @Test
    @DisplayName("generateToken embeds role claim")
    void generateToken_roleClaimMatchesUserRole() {
        String token = jwtService.generateToken(makeUser(1L, UserRole.ADMIN));
        Claims claims = jwtService.parseAndValidate(token);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("generateToken includes STUDENT role correctly")
    void generateToken_studentRolePresentInClaims() {
        String token = jwtService.generateToken(makeUser(1L, UserRole.STUDENT));
        Claims claims = jwtService.parseAndValidate(token);
        assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
    }

    //parseAndValidate()

    @Test
    @DisplayName("parseAndValidate returns claims for a valid token")
    void parseAndValidate_validToken_returnsClaims() {
        String token = jwtService.generateToken(makeUser(1L, UserRole.STAFF));
        Claims claims = jwtService.parseAndValidate(token);
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("1");
    }

    @Test
    @DisplayName("parseAndValidate throws for a completely invalid string")
    void parseAndValidate_invalidString_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.parseAndValidate("not.a.jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("parseAndValidate throws for a token signed with a different secret")
    void parseAndValidate_wrongSecret_throwsException() {
        JwtService otherService = new JwtService("other-secret-key-at-least-32-characters!!", EXPIRATION_MS);
        String token = otherService.generateToken(makeUser(1L, UserRole.STAFF));
        assertThatThrownBy(() -> jwtService.parseAndValidate(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("parseAndValidate throws for an expired token")
    void parseAndValidate_expiredToken_throwsException() {
        JwtService shortLived = new JwtService(SECRET, -1000L); // already expired
        String token = shortLived.generateToken(makeUser(1L, UserRole.STAFF));
        assertThatThrownBy(() -> jwtService.parseAndValidate(token))
                .isInstanceOf(JwtException.class);
    }

    //helpers

    private User makeUser(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail("test@ul.ie");
        user.setPasswordHash("hash");
        user.setFullName("Test User");
        user.setRole(role);
        return user;
    }
}
