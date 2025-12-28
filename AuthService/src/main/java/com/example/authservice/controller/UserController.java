package com.example.authservice.controller;

import com.example.authservice.dto.UserRequest;
import com.example.authservice.dto.UserResponse;
import com.example.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE') or hasAuthority('USER_VIEW')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE') or hasAuthority('USER_VIEW')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // @PostMapping
    // public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
    //     return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    // }

    // @PutMapping("/{id}")
    // public ResponseEntity<UserResponse> updateUser(
    //         @PathVariable Long id,
    //         @Valid @RequestBody UserRequest request) {
    //     return ResponseEntity.ok(userService.updateUser(id, request));
    // }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

