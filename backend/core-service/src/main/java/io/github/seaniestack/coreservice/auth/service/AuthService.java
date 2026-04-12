package io.github.seaniestack.coreservice.auth.service;

import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.github.seaniestack.coreservice.auth.dto.AuthResponse;
import io.github.seaniestack.coreservice.auth.dto.LoginRequest;
import io.github.seaniestack.coreservice.auth.dto.RegisterRequest;
import io.github.seaniestack.coreservice.auth.dto.UserResponse;
import io.github.seaniestack.coreservice.auth.exception.EmailAlreadyRegisteredException;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import io.github.seaniestack.coreservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException();
        }
        UserRole role = UserRole.fromRegistrationEmail(email);

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(role);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        long expiresInSeconds = jwtExpirationMs / 1000;
        return new AuthResponse(token, "Bearer", expiresInSeconds, UserResponse.from(user));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
