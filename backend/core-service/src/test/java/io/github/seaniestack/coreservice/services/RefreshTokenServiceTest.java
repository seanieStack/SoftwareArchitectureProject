package io.github.seaniestack.coreservice.services;

import io.github.seaniestack.coreservice.auth.domain.RefreshToken;
import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.github.seaniestack.coreservice.auth.repository.RefreshTokenRepository;
import io.github.seaniestack.coreservice.auth.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "expirationMs", 86_400_000L);
    }

    // ── create() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create deletes existing tokens for the user before saving a new one")
    void create_deletesExistingTokenFirst() {
        User user = makeUser();
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        refreshTokenService.create(user);

        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("create saves a new token and returns it")
    void create_savesAndReturnsNewToken() {
        User user = makeUser();
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.create(user);

        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("create sets expiry in the future")
    void create_setsExpiryInFuture() {
        User user = makeUser();
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.create(user);

        assertThat(result.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("create generates a unique token value each time")
    void create_generatesUniqueTokens() {
        User user = makeUser();
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken first = refreshTokenService.create(user);
        RefreshToken second = refreshTokenService.create(user);

        assertThat(first.getToken()).isNotEqualTo(second.getToken());
    }

    // ── validateAndRotate() ───────────────────────────────────────────────────

    @Test
    @DisplayName("validateAndRotate returns a new token and deletes the old one")
    void validateAndRotate_validToken_rotatesSuccessfully() {
        User user = makeUser();
        RefreshToken existing = makeToken(user, Instant.now().plusMillis(86_400_000L));
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.validateAndRotate("valid-token");

        verify(refreshTokenRepository).delete(existing);
        assertThat(result.getToken()).isNotEqualTo("valid-token");
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("validateAndRotate throws CredentialsExpiredException for unknown token")
    void validateAndRotate_unknownToken_throwsCredentialsExpired() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateAndRotate("unknown"))
                .isInstanceOf(CredentialsExpiredException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    @DisplayName("validateAndRotate throws and deletes token when it is expired")
    void validateAndRotate_expiredToken_throwsAndDeletesToken() {
        User user = makeUser();
        RefreshToken expired = makeToken(user, Instant.now().minusMillis(1000L));
        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.validateAndRotate("expired"))
                .isInstanceOf(CredentialsExpiredException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    @DisplayName("validateAndRotate new token has expiry in the future")
    void validateAndRotate_newTokenExpiryIsInFuture() {
        User user = makeUser();
        RefreshToken existing = makeToken(user, Instant.now().plusMillis(86_400_000L));
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.validateAndRotate("valid-token");

        assertThat(result.getExpiresAt()).isAfter(Instant.now());
    }

    // ── revokeByUser() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("revokeByUser calls deleteByUser on the repository")
    void revokeByUser_callsRepository() {
        User user = makeUser();
        refreshTokenService.revokeByUser(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User makeUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@ul.ie");
        user.setPasswordHash("hash");
        user.setFullName("Test User");
        user.setRole(UserRole.STAFF);
        return user;
    }

    private RefreshToken makeToken(User user, Instant expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpiresAt(expiresAt);
        return token;
    }
}
