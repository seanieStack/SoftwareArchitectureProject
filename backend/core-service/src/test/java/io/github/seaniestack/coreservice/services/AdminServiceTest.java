package io.github.seaniestack.coreservice.services;

import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import io.github.seaniestack.coreservice.dtos.StatsDTO;
import io.github.seaniestack.coreservice.dtos.UserDTO;
import io.github.seaniestack.coreservice.exceptions.ResourceNotFoundException;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    // ── getCounts() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCounts returns total books and registered users")
    void getCounts_returnsTotals() {
        when(bookRepository.count()).thenReturn(5L);
        when(userRepository.count()).thenReturn(10L);

        StatsDTO stats = adminService.getCounts();

        assertThat(stats.getTotalBooks()).isEqualTo(5L);
        assertThat(stats.getRegisteredUsers()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getCounts returns zero values when no data exists")
    void getCounts_zeroes_whenEmpty() {
        when(bookRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);

        StatsDTO stats = adminService.getCounts();

        assertThat(stats.getTotalBooks()).isZero();
        assertThat(stats.getRegisteredUsers()).isZero();
    }

    // ── getUsers() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUsers returns all users mapped to DTOs")
    void getUsers_returnsAllUsersAsDTOs() {
        User staff = makeUser(1L, "staff@ul.ie", UserRole.STAFF);
        User student = makeUser(2L, "student@studentmail.ul.ie", UserRole.STUDENT);
        when(userRepository.findAll()).thenReturn(List.of(staff, student));

        List<UserDTO> result = adminService.getUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDTO::getEmail)
                .containsExactlyInAnyOrder("staff@ul.ie", "student@studentmail.ul.ie");
    }

    @Test
    @DisplayName("getUsers returns empty list when no users exist")
    void getUsers_emptyList_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDTO> result = adminService.getUsers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUsers maps role correctly to UserDTO")
    void getUsers_roleIsMappedCorrectly() {
        User admin = makeUser(1L, "admin@ul.ie", UserRole.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of(admin));

        List<UserDTO> result = adminService.getUsers();

        assertThat(result.get(0).getRole()).isEqualTo(UserRole.ADMIN);
    }

    // ── updateUserRole() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserRole changes the user's role and saves")
    void updateUserRole_validUser_updatesRole() {
        User user = makeUser(1L, "test@ul.ie", UserRole.STAFF);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserDTO result = adminService.updateUserRole(1L, UserRole.ADMIN);

        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUserRole can downgrade from ADMIN to STUDENT")
    void updateUserRole_downgrade_works() {
        User user = makeUser(1L, "admin@ul.ie", UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserDTO result = adminService.updateUserRole(1L, UserRole.STUDENT);

        assertThat(result.getRole()).isEqualTo(UserRole.STUDENT);
    }

    @Test
    @DisplayName("updateUserRole throws ResourceNotFoundException for unknown user")
    void updateUserRole_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUserRole(99L, UserRole.ADMIN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("updateUserRole does not save when user not found")
    void updateUserRole_userNotFound_doesNotSave() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUserRole(99L, UserRole.ADMIN));

        verify(userRepository, never()).save(any());
    }

    // ── deleteUser() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser deletes the user when found")
    void deleteUser_validUser_deletesUser() {
        User user = makeUser(1L, "test@ul.ie", UserRole.STAFF);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteUser throws ResourceNotFoundException for unknown user")
    void deleteUser_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("deleteUser does not call delete when user not found")
    void deleteUser_userNotFound_doesNotCallDelete() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(99L));

        verify(userRepository, never()).delete(any(User.class));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User makeUser(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setFullName("Test User");
        user.setRole(role);
        user.setCreatedAt(Instant.now());
        return user;
    }
}
