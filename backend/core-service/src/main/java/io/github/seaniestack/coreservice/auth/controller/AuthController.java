package io.github.seaniestack.coreservice.auth.controller;

import io.github.seaniestack.coreservice.auth.dto.AuthResponse;
import io.github.seaniestack.coreservice.auth.dto.ForgotPasswordRequest;
import io.github.seaniestack.coreservice.auth.dto.LoginRequest;
import io.github.seaniestack.coreservice.auth.dto.MessageResponse;
import io.github.seaniestack.coreservice.auth.dto.RefreshRequest;
import io.github.seaniestack.coreservice.auth.dto.RegisterRequest;
import io.github.seaniestack.coreservice.auth.dto.ResetPasswordRequest;
import io.github.seaniestack.coreservice.auth.service.AuthService;
import io.github.seaniestack.coreservice.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(new MessageResponse(
                "If that email is registered, you will receive password reset instructions shortly."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Your password has been updated. You can sign in now."));
    }
}
