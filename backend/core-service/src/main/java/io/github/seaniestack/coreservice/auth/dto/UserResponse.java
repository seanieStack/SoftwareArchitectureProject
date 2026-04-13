package io.github.seaniestack.coreservice.auth.dto;

import io.github.seaniestack.coreservice.auth.domain.User;

public record UserResponse(long id, String email, String fullName, String userType) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().toClientValue()
        );
    }
}
