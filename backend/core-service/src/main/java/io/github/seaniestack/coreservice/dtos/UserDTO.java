package io.github.seaniestack.coreservice.dtos;

import io.github.seaniestack.coreservice.auth.domain.User;
import io.github.seaniestack.coreservice.auth.domain.UserRole;
import lombok.Data;

import java.time.Instant;

@Data
public class UserDTO {
    String email;
    Long id;
    Instant createdAt;
    UserRole role;
    String fullName;

    public static UserDTO from(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.email = user.getEmail();
        userDTO.id = user.getId();
        userDTO.createdAt = user.getCreatedAt();
        userDTO.role = user.getRole();
        userDTO.fullName = user.getFullName();
        return userDTO;
    }
}
