package io.github.seaniestack.coreservice.auth.service;

import io.github.seaniestack.coreservice.auth.domain.PasswordResetToken;
import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.repository.PasswordResetTokenRepository;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetNotifier passwordResetNotifier;

    @Value("${app.password-reset.token-expiration-ms}")
    private long tokenExpirationMs;

    @Transactional
    public void requestReset(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(normalized).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);
            PasswordResetToken entity = new PasswordResetToken();
            entity.setToken(UUID.randomUUID().toString());
            entity.setUser(user);
            entity.setExpiresAt(Instant.now().plusMillis(tokenExpirationMs));
            passwordResetTokenRepository.save(entity);
            passwordResetNotifier.sendResetEmail(user.getEmail(), entity.getToken());
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Reset token is required");
        }
        PasswordResetToken existing = passwordResetTokenRepository
                .findByToken(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(existing);
            throw new IllegalArgumentException("Invalid or expired reset link");
        }

        User user = existing.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(existing);
        refreshTokenService.revokeByUser(user);
    }
}
