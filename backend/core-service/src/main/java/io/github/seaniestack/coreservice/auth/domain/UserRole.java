package io.github.seaniestack.coreservice.auth.domain;

import java.util.Locale;

public enum UserRole {
    STUDENT,
    STAFF,
    ADMIN;

    /** Values sent by the frontend: {@code student}, {@code staff}, {@code admin}. */
    public static UserRole fromClientValue(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("userType is required");
        }
        try {
            return UserRole.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("userType must be one of: student, staff, admin");
        }
    }

    /** Lowercase label for API responses matching the frontend. */
    public String toClientValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
