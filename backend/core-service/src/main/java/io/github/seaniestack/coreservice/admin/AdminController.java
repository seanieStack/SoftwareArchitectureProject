package io.github.seaniestack.coreservice.admin;

import io.github.seaniestack.coreservice.admin.dto.AdminCountsDTO;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.github.seaniestack.coreservice.auth.dto.UserResponse;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/counts")
    public ResponseEntity<AdminCountsDTO> getCounts() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        long totalStaff = userRepository.countByRole(UserRole.STAFF);
        long totalAdmins = userRepository.countByRole(UserRole.ADMIN);
        long totalBooks = bookRepository.count();
        return ResponseEntity.ok(new AdminCountsDTO(totalUsers, totalStudents, totalStaff, totalAdmins, totalBooks));
    }
}
