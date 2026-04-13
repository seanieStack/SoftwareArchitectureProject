package io.github.seaniestack.coreservice.auth.service;

import io.github.seaniestack.coreservice.auth.domain.RefreshToken;
import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh-token.expiration-ms}")
    private long expirationMs;

    @Transactional
    public RefreshToken create(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusMillis(expirationMs));
        return refreshTokenRepository.save(token);
    }

    /**
     * Validates the given token value, deletes it, and issues a new one (rotation).
     * Throws {@link CredentialsExpiredException} if the token is unknown or expired.
     */
    @Transactional
    public RefreshToken validateAndRotate(String tokenValue) {
        RefreshToken existing = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new CredentialsExpiredException("Invalid refresh token"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existing);
            throw new CredentialsExpiredException("Refresh token has expired");
        }

        User user = existing.getUser();
        refreshTokenRepository.delete(existing);

        RefreshToken rotated = new RefreshToken();
        rotated.setToken(UUID.randomUUID().toString());
        rotated.setUser(user);
        rotated.setExpiresAt(Instant.now().plusMillis(expirationMs));
        return refreshTokenRepository.save(rotated);
    }

    @Transactional
    public void revokeByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
