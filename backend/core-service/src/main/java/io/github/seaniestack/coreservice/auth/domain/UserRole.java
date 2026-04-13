package io.github.seaniestack.coreservice.auth.domain;

import java.util.Locale;

public enum UserRole {
    STUDENT,
    STAFF,
    ADMIN;

    /**
     * Role for self-service registration from email domain.
     * Admins are hardcoded in the database.
     */
    public static UserRole fromRegistrationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        String e = email.trim().toLowerCase(Locale.ROOT);
        if (e.endsWith("@studentmail.ul.ie")) {
            return STUDENT;
        }
        if (e.endsWith("@ul.ie")) {
            return STAFF;
        }
        throw new IllegalArgumentException(
                "Registration is only allowed for @studentmail.ul.ie or @ul.ie email addresses");
    }

    /** Lowercase label for API responses matching the frontend. */
    public String toClientValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
