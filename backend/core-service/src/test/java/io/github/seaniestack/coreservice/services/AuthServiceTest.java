package io.github.seaniestack.coreservice.services;

import io.github.seaniestack.coreservice.auth.domain.RefreshToken;
import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.github.seaniestack.coreservice.auth.dto.AuthResponse;
import io.github.seaniestack.coreservice.auth.dto.LoginRequest;
import io.github.seaniestack.coreservice.auth.dto.RefreshRequest;
import io.github.seaniestack.coreservice.auth.dto.RegisterRequest;
import io.github.seaniestack.coreservice.auth.exception.EmailAlreadyRegisteredException;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import io.github.seaniestack.coreservice.auth.service.AuthService;
import io.github.seaniestack.coreservice.auth.service.RefreshTokenService;
import io.github.seaniestack.coreservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 3_600_000L);
    }

    // ── register() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register returns AuthResponse for a valid student email")
    void register_validStudentEmail_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest("Leo Test", "leo@studentmail.ul.ie", "password123");
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> { User u = inv.getArgument(0); u.setId(1L); return u; });
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(refreshTokenService.create(any())).thenReturn(makeRefreshToken());
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("leo@studentmail.ul.ie");
        assertThat(response.user().userType()).isEqualTo("student");
    }

    @Test
    @DisplayName("register returns AuthResponse for a valid staff email")
    void register_validStaffEmail_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest("Staff User", "staff@ul.ie", "password123");
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> { User u = inv.getArgument(0); u.setId(2L); return u; });
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(refreshTokenService.create(any())).thenReturn(makeRefreshToken());
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.user().userType()).isEqualTo("staff");
    }

    @Test
    @DisplayName("register throws EmailAlreadyRegisteredException for duplicate email")
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("Leo Test", "leo@studentmail.ul.ie", "password123");
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register throws IllegalArgumentException for non-UL email")
    void register_nonUlEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("Leo Test", "leo@gmail.com", "password123");
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("register normalises email to lowercase before saving")
    void register_uppercaseEmail_normalisesToLowercase() {
        RegisterRequest request = new RegisterRequest("Leo Test", "LEO@STUDENTMAIL.UL.IE", "password123");
        when(userRepository.existsByEmailIgnoreCase("leo@studentmail.ul.ie")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> { User u = inv.getArgument(0); u.setId(1L); return u; });
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(refreshTokenService.create(any())).thenReturn(makeRefreshToken());
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        authService.register(request);

        verify(userRepository).existsByEmailIgnoreCase("leo@studentmail.ul.ie");
    }

    @Test
    @DisplayName("register hashes the password before saving")
    void register_passwordIsHashed() {
        RegisterRequest request = new RegisterRequest("Leo Test", "leo@studentmail.ul.ie", "plaintext");
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("bcrypt-hash");
        when(userRepository.save(any())).thenAnswer(inv -> { User u = inv.getArgument(0); u.setId(1L); return u; });
        when(refreshTokenService.create(any())).thenReturn(makeRefreshToken());
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        authService.register(request);

        verify(passwordEncoder).encode("plaintext");
    }

    @Test
    @DisplayName("register response contains expiresIn in seconds")
    void register_responseContainsExpiresInSeconds() {
        RegisterRequest request = new RegisterRequest("Leo Test", "leo@studentmail.ul.ie", "password123");
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> { User u = inv.getArgument(0); u.setId(1L); return u; });
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(refreshTokenService.create(any())).thenReturn(makeRefreshToken());
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.expiresIn()).isEqualTo(3600L); // 3_600_000ms / 1000
    }

    // ── login() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login returns AuthResponse for valid credentials")
    void login_validCredentials_returnsAuthResponse() {
        User user = makeUser();
        when(userRepository.findByEmailIgnoreCase("test@ul.ie")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(refreshTokenService.create(any())).thenReturn(makeRefreshToken());
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("test@ul.ie", "password"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("test@ul.ie");
    }

    @Test
    @DisplayName("login throws BadCredentialsException when user not found")
    void login_unknownEmail_throwsBadCredentials() {
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@ul.ie", "password")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("login throws BadCredentialsException for wrong password")
    void login_wrongPassword_throwsBadCredentials() {
        User user = makeUser();
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@ul.ie", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login normalises email to lowercase before lookup")
    void login_uppercaseEmail_normalisesToLowercase() {
        User user = makeUser();
        when(userRepository.findByEmailIgnoreCase("test@ul.ie")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(refreshTokenService.create(any())).thenReturn(makeRefreshToken());
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        authService.login(new LoginRequest("TEST@UL.IE", "password"));

        verify(userRepository).findByEmailIgnoreCase("test@ul.ie");
    }

    // ── refresh() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("refresh returns new AuthResponse for a valid refresh token")
    void refresh_validToken_returnsNewAuthResponse() {
        User user = makeUser();
        RefreshToken rotated = makeRefreshToken();
        rotated.setUser(user);
        when(refreshTokenService.validateAndRotate("refresh-token")).thenReturn(rotated);
        when(jwtService.generateToken(any())).thenReturn("new-jwt");

        AuthResponse response = authService.refresh(new RefreshRequest("refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-jwt");
    }

    @Test
    @DisplayName("refresh propagates CredentialsExpiredException for expired token")
    void refresh_expiredToken_throwsCredentialsExpired() {
        when(refreshTokenService.validateAndRotate(any()))
                .thenThrow(new CredentialsExpiredException("expired"));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("old-token")))
                .isInstanceOf(CredentialsExpiredException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User makeUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@ul.ie");
        user.setPasswordHash("hashed");
        user.setFullName("Test User");
        user.setRole(UserRole.STAFF);
        return user;
    }

    private RefreshToken makeRefreshToken() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("refresh-token");
        rt.setUser(makeUser());
        rt.setExpiresAt(Instant.now().plusMillis(86_400_000L));
        return rt;
    }
}
