package com.example.authservice.service;

import com.example.authservice.dto.PermissionRequest;
import com.example.authservice.dto.PermissionResponse;
import com.example.authservice.entity.Permission;
import com.example.authservice.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
        return PermissionResponse.fromEntity(permission);
    }

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Permission already exists: " + request.getName());
        }

        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());

        Permission savedPermission = permissionRepository.save(permission);
        return PermissionResponse.fromEntity(savedPermission);
    }

    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));

        if (!permission.getName().equals(request.getName()) &&
            permissionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Permission name already exists: " + request.getName());
        }

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());

        Permission updatedPermission = permissionRepository.save(permission);
        return PermissionResponse.fromEntity(updatedPermission);
    }

    @Transactional
    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permission not found with id: " + id);
        }
        permissionRepository.deleteById(id);
    }
}

