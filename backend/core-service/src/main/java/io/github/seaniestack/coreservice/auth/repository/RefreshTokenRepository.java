package io.github.seaniestack.coreservice.auth.repository;

import io.github.seaniestack.coreservice.auth.domain.RefreshToken;
import io.github.seaniestack.coreservice.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
