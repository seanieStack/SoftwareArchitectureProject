package io.github.seaniestack.coreservice.services;

import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import io.github.seaniestack.coreservice.auth.repository.UserRepository;
import io.github.seaniestack.coreservice.dtos.StatsDTO;
import io.github.seaniestack.coreservice.dtos.UserDTO;
import io.github.seaniestack.coreservice.exceptions.ResourceNotFoundException;
import io.github.seaniestack.coreservice.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public StatsDTO getCounts() {
        StatsDTO stats = new StatsDTO();
        stats.setTotalBooks(bookRepository.count());
        stats.setRegisteredUsers(userRepository.count());
        return stats;
    }

    public List<UserDTO> getUsers() {
        return userRepository.findAll().stream().map(UserDTO::from).toList();
    }

    public UserDTO updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
        return UserDTO.from(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }
}
