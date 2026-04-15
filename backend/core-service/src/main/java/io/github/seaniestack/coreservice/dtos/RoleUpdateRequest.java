package io.github.seaniestack.coreservice.dtos;

import io.github.seaniestack.coreservice.auth.domain.UserRole;

public record RoleUpdateRequest(UserRole role) {
}
