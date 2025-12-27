package com.example.authservice.dto;

import com.example.authservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean enabled;
    private Set<RoleResponse> roles;
    private Set<PermissionResponse> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setEnabled(user.getEnabled());
        if (user.getRoles() != null) {
            response.setRoles(
                user.getRoles().stream()
                    .map(RoleResponse::fromEntity)
                    .collect(Collectors.toSet())
            );
        }
        response.setPermissions(
            user.getPermissions().stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toSet())
        );
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}

