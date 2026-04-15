package io.github.seaniestack.coreservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seaniestack.coreservice.CoreServiceApplication;
import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.github.seaniestack.coreservice.auth.repository.PasswordResetTokenRepository;
import io.github.seaniestack.coreservice.auth.repository.RefreshTokenRepository;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import io.github.seaniestack.coreservice.messaging.BookEventPublisher;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CoreServiceApplication.class)


class AdminControllerIntegrationTest {

    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private WebApplicationContext context;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockitoBean
    private BookEventPublisher bookEventPublisher;

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";



    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        refreshTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll(); // AdminController only
    }

    // ── GET /api/admin/counts ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/counts returns 200 and stats for ADMIN user")
    void getCounts_asAdmin_returns200() throws Exception {
        createUser("staff@ul.ie", UserRole.STAFF);
        createUser("student@studentmail.ul.ie", UserRole.STUDENT);

        mockMvc.perform(get("/api/admin/counts")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registeredUsers").value(2))
                .andExpect(jsonPath("$.totalBooks").value(0));
    }

    @Test
    @DisplayName("GET /api/admin/counts returns 403 for a non-admin user")
    void getCounts_asStudent_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/counts")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/counts returns 401 when no auth headers present")
    void getCounts_noHeaders_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/counts"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/admin/users ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/users returns 200 and list of all users")
    void getUsers_asAdmin_returns200WithUserList() throws Exception {
        createUser("staff@ul.ie", UserRole.STAFF);
        createUser("student@studentmail.ul.ie", UserRole.STUDENT);

        mockMvc.perform(get("/api/admin/users")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/admin/users returns empty list when no users exist")
    void getUsers_noUsers_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/admin/users returns 403 for STAFF user")
    void getUsers_asStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STAFF"))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /api/admin/users/{id}/role ──────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/role returns 200 and updates the user's role")
    void updateUserRole_asAdmin_returns200() throws Exception {
        User user = createUser("staff@ul.ie", UserRole.STAFF);

        mockMvc.perform(patch("/api/admin/users/" + user.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN")
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/role returns 404 for non-existent user")
    void updateUserRole_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(patch("/api/admin/users/99999/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN")
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/role returns 403 for non-admin user")
    void updateUserRole_asStudent_returns403() throws Exception {
        User user = createUser("staff@ul.ie", UserRole.STAFF);

        mockMvc.perform(patch("/api/admin/users/" + user.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STUDENT")
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/admin/users/{id} ──────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/admin/users/{id} returns 204 and removes the user")
    void deleteUser_asAdmin_returns204() throws Exception {
        User user = createUser("staff@ul.ie", UserRole.STAFF);

        mockMvc.perform(delete("/api/admin/users/" + user.getId())
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} returns 404 for non-existent user")
    void deleteUser_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(delete("/api/admin/users/99999")
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} returns 403 for non-admin user")
    void deleteUser_asStaff_returns403() throws Exception {
        User user = createUser("staff@ul.ie", UserRole.STAFF);

        mockMvc.perform(delete("/api/admin/users/" + user.getId())
                        .header(USER_ID_HEADER, "1")
                        .header(USER_ROLE_HEADER, "STAFF"))
                .andExpect(status().isForbidden());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User createUser(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setFullName("Test User");
        user.setRole(role);
        return userRepository.save(user);
    }
}
