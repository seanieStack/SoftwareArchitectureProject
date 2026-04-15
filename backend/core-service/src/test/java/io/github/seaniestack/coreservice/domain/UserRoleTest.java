package io.github.seaniestack.coreservice.domain;

import io.github.seaniestack.coreservice.auth.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    // ── fromRegistrationEmail() ───────────────────────────────────────────────

    @Test
    @DisplayName("student email returns STUDENT role")
    void fromRegistrationEmail_studentDomain_returnsStudent() {
        assertThat(UserRole.fromRegistrationEmail("leo@studentmail.ul.ie"))
                .isEqualTo(UserRole.STUDENT);
    }

    @Test
    @DisplayName("staff email returns STAFF role")
    void fromRegistrationEmail_staffDomain_returnsStaff() {
        assertThat(UserRole.fromRegistrationEmail("staff@ul.ie"))
                .isEqualTo(UserRole.STAFF);
    }

    @Test
    @DisplayName("student email with mixed case is normalised correctly")
    void fromRegistrationEmail_mixedCaseStudent_returnsStudent() {
        assertThat(UserRole.fromRegistrationEmail("Leo@STUDENTMAIL.UL.IE"))
                .isEqualTo(UserRole.STUDENT);
    }

    @Test
    @DisplayName("staff email with mixed case is normalised correctly")
    void fromRegistrationEmail_mixedCaseStaff_returnsStaff() {
        assertThat(UserRole.fromRegistrationEmail("STAFF@UL.IE"))
                .isEqualTo(UserRole.STAFF);
    }

    @Test
    @DisplayName("email with leading/trailing whitespace is trimmed")
    void fromRegistrationEmail_withWhitespace_trimsAndReturnsRole() {
        assertThat(UserRole.fromRegistrationEmail("  leo@studentmail.ul.ie  "))
                .isEqualTo(UserRole.STUDENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@gmail.com", "test@yahoo.com", "test@hotmail.com", "test@ul.ie.fake.com"})
    @DisplayName("non-UL domains throw IllegalArgumentException")
    void fromRegistrationEmail_invalidDomain_throwsException(String email) {
        assertThatThrownBy(() -> UserRole.fromRegistrationEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registration is only allowed");
    }

    @Test
    @DisplayName("null email throws IllegalArgumentException")
    void fromRegistrationEmail_nullEmail_throwsException() {
        assertThatThrownBy(() -> UserRole.fromRegistrationEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email is required");
    }

    @Test
    @DisplayName("blank email throws IllegalArgumentException")
    void fromRegistrationEmail_blankEmail_throwsException() {
        assertThatThrownBy(() -> UserRole.fromRegistrationEmail("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email is required");
    }

    @Test
    @DisplayName("empty string throws IllegalArgumentException")
    void fromRegistrationEmail_emptyString_throwsException() {
        assertThatThrownBy(() -> UserRole.fromRegistrationEmail(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── toClientValue() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("STUDENT toClientValue returns lowercase 'student'")
    void toClientValue_student_returnsLowercase() {
        assertThat(UserRole.STUDENT.toClientValue()).isEqualTo("student");
    }

    @Test
    @DisplayName("STAFF toClientValue returns lowercase 'staff'")
    void toClientValue_staff_returnsLowercase() {
        assertThat(UserRole.STAFF.toClientValue()).isEqualTo("staff");
    }

    @Test
    @DisplayName("ADMIN toClientValue returns lowercase 'admin'")
    void toClientValue_admin_returnsLowercase() {
        assertThat(UserRole.ADMIN.toClientValue()).isEqualTo("admin");
    }
}
