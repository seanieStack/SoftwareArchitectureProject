package io.github.seaniestack.coreservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seaniestack.coreservice.CoreServiceApplication;
import io.github.seaniestack.coreservice.auth.repository.PasswordResetTokenRepository;
import io.github.seaniestack.coreservice.auth.repository.RefreshTokenRepository;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import io.github.seaniestack.coreservice.messaging.BookEventPublisher;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CoreServiceApplication.class)

class AuthControllerIntegrationTest {

    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private WebApplicationContext context;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private BookRepository bookRepository;

    @MockitoBean
    private BookEventPublisher bookEventPublisher;

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

    // ── POST /api/auth/register ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register returns 201 and AuthResponse for valid student email")
    void register_validStudentEmail_returns201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Leo Test",
                                "email", "leo@studentmail.ul.ie",
                                "password", "password123"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("leo@studentmail.ul.ie"))
                .andExpect(jsonPath("$.user.userType").value("student"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns 201 and AuthResponse for valid staff email")
    void register_validStaffEmail_returns201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Staff User",
                                "email", "staff@ul.ie",
                                "password", "password123"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.userType").value("staff"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns 409 for a duplicate email")
    void register_duplicateEmail_returns409() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "fullName", "Leo Test",
                "email", "leo@studentmail.ul.ie",
                "password", "password123"
        ));
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register returns 400 for a non-UL email")
    void register_nonUlEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Leo Test",
                                "email", "leo@gmail.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register returns 400 when fullName is blank")
    void register_blankFullName_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "",
                                "email", "leo@studentmail.ul.ie",
                                "password", "password123"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register returns 400 when password is too short")
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Leo Test",
                                "email", "leo@studentmail.ul.ie",
                                "password", "short"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register returns 400 when email is not valid format")
    void register_invalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Leo Test",
                                "email", "not-an-email",
                                "password", "password123"
                        ))))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login returns 200 with tokens for valid credentials")
    void login_validCredentials_returns200() throws Exception {
        // Register first
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fullName", "Leo Test",
                        "email", "leo@studentmail.ul.ie",
                        "password", "password123"
                ))));

        // Then login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "leo@studentmail.ul.ie",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("leo@studentmail.ul.ie"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 for wrong password")
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fullName", "Leo Test",
                        "email", "leo@studentmail.ul.ie",
                        "password", "password123"
                ))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "leo@studentmail.ul.ie",
                                "password", "wrongpassword"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 for unknown email")
    void login_unknownEmail_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "nobody@ul.ie",
                                "password", "password123"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login is case-insensitive for email")
    void login_uppercaseEmail_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fullName", "Leo Test",
                        "email", "leo@studentmail.ul.ie",
                        "password", "password123"
                ))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "LEO@STUDENTMAIL.UL.IE",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk());
    }

    // ── POST /api/auth/refresh ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/refresh returns 200 with a new access token for a valid refresh token")
    void refresh_validRefreshToken_returns200() throws Exception {
        // Register and get refresh token
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Leo Test",
                                "email", "leo@studentmail.ul.ie",
                                "password", "password123"
                        ))))
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(registerResponse).get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/refresh returns 401 for an invalid refresh token")
    void refresh_invalidRefreshToken_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "bad-token"))))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /api/auth/forgot-password ────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/forgot-password returns 200 with a generic message regardless of whether email exists")
    void forgotPassword_alwaysReturns200() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "nobody@ul.ie"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password returns 400 for an invalid email format")
    void forgotPassword_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "not-an-email"))))
                .andExpect(status().isBadRequest());
    }
}
