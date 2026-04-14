package io.github.seaniestack.coreservice.controllers;

import io.github.seaniestack.coreservice.dtos.StatsDTO;
import io.github.seaniestack.coreservice.dtos.UserDTO;
import io.github.seaniestack.coreservice.dtos.RoleUpdateRequest;
import io.github.seaniestack.coreservice.services.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/counts")
    public ResponseEntity<StatsDTO> getAdminCounts() {
        log.trace("GET /api/admin/counts");
        StatsDTO stats = adminService.getCounts();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers() {
        log.trace("GET /api/admin/users");
        List<UserDTO> users = adminService.getUsers();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long userId, @RequestBody RoleUpdateRequest request) {
        log.trace("PATCH /api/admin/users/{}/role", userId);
        UserDTO updated = adminService.updateUserRole(userId, request.role());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.trace("DELETE /api/admin/users/{}", userId);
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
