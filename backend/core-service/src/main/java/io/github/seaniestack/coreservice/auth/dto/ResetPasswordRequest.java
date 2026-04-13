package io.github.seaniestack.coreservice.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(min = 8, max = 128) String newPassword,
        @NotBlank String token
) {
}
