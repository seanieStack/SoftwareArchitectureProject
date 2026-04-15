package io.github.seaniestack.coreservice.services;

import io.github.seaniestack.coreservice.auth.domain.PasswordResetToken;
import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.github.seaniestack.coreservice.auth.repository.PasswordResetTokenRepository;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import io.github.seaniestack.coreservice.auth.service.PasswordResetNotifier;
import io.github.seaniestack.coreservice.auth.service.PasswordResetService;
import io.github.seaniestack.coreservice.auth.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordResetNotifier passwordResetNotifier;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "tokenExpirationMs", 3_600_000L);
    }

    // ── requestReset() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("requestReset for a known email creates a token and sends email")
    void requestReset_knownEmail_createsTokenAndSendsEmail() {
        User user = makeUser();
        when(userRepository.findByEmailIgnoreCase("test@ul.ie")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.requestReset("test@ul.ie");

        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(passwordResetNotifier).sendResetEmail(eq("test@ul.ie"), any());
    }

    @Test
    @DisplayName("requestReset deletes any existing reset token for the user first")
    void requestReset_deletesOldTokenFirst() {
        User user = makeUser();
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.requestReset("test@ul.ie");

        verify(passwordResetTokenRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("requestReset for an unknown email does nothing silently")
    void requestReset_unknownEmail_doesNothing() {
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

        passwordResetService.requestReset("nobody@ul.ie");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(passwordResetNotifier, never()).sendResetEmail(any(), any());
    }

    @Test
    @DisplayName("requestReset with blank email returns immediately without hitting the DB")
    void requestReset_blankEmail_returnsImmediately() {
        passwordResetService.requestReset("   ");

        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    @DisplayName("requestReset with null email returns immediately")
    void requestReset_nullEmail_returnsImmediately() {
        passwordResetService.requestReset(null);

        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    @DisplayName("requestReset token has a future expiry")
    void requestReset_tokenExpiryIsInFuture() {
        User user = makeUser();
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(user));
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        when(passwordResetTokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.requestReset("test@ul.ie");

        assertThat(captor.getValue().getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("requestReset normalises email to lowercase for lookup")
    void requestReset_uppercaseEmail_normalisesForLookup() {
        when(userRepository.findByEmailIgnoreCase("test@ul.ie")).thenReturn(Optional.empty());

        passwordResetService.requestReset("TEST@UL.IE");

        verify(userRepository).findByEmailIgnoreCase("test@ul.ie");
    }

    // ── resetPassword() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("resetPassword with valid token updates password and revokes refresh tokens")
    void resetPassword_validToken_updatesPasswordAndRevokesTokens() {
        User user = makeUser();
        PasswordResetToken token = makeToken(user, Instant.now().plusMillis(3_600_000L));
        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword1")).thenReturn("new-hash");
        when(userRepository.save(any())).thenReturn(user);

        passwordResetService.resetPassword("valid-token", "newPassword1");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(passwordResetTokenRepository).delete(token);
        verify(refreshTokenService).revokeByUser(user);
    }

    @Test
    @DisplayName("resetPassword deletes the reset token after success")
    void resetPassword_deletesTokenAfterSuccess() {
        User user = makeUser();
        PasswordResetToken token = makeToken(user, Instant.now().plusMillis(3_600_000L));
        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(any())).thenReturn("new-hash");
        when(userRepository.save(any())).thenReturn(user);

        passwordResetService.resetPassword("valid-token", "newPassword1");

        verify(passwordResetTokenRepository).delete(token);
    }

    @Test
    @DisplayName("resetPassword throws for an unknown token")
    void resetPassword_unknownToken_throwsIllegalArgument() {
        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword("bad-token", "newPassword1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    @DisplayName("resetPassword throws and deletes token when it is expired")
    void resetPassword_expiredToken_throwsAndDeletesToken() {
        User user = makeUser();
        PasswordResetToken token = makeToken(user, Instant.now().minusMillis(1000L));
        when(passwordResetTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword("expired", "newPassword1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired");

        verify(passwordResetTokenRepository).delete(token);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword throws for a blank token")
    void resetPassword_blankToken_throwsIllegalArgument() {
        assertThatThrownBy(() -> passwordResetService.resetPassword("  ", "newPassword1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reset token is required");
    }

    @Test
    @DisplayName("resetPassword trims whitespace from the token before lookup")
    void resetPassword_tokenWithWhitespace_trims() {
        User user = makeUser();
        PasswordResetToken token = makeToken(user, Instant.now().plusMillis(3_600_000L));
        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(any())).thenReturn("new-hash");
        when(userRepository.save(any())).thenReturn(user);

        passwordResetService.resetPassword("  valid-token  ", "newPassword1");

        verify(passwordResetTokenRepository).findByToken("valid-token");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User makeUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@ul.ie");
        user.setPasswordHash("old-hash");
        user.setFullName("Test User");
        user.setRole(UserRole.STAFF);
        return user;
    }

    private PasswordResetToken makeToken(User user, Instant expiresAt) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpiresAt(expiresAt);
        return token;
    }
}
