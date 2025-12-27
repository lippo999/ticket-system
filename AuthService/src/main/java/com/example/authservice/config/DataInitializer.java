package com.example.authservice.config;

import com.example.authservice.entity.Permission;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import com.example.authservice.repository.PermissionRepository;
import com.example.authservice.repository.RoleRepository;
import com.example.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Starting database initialization...");

            // Create basic permissions
            List<Permission> permissions = createPermissions();

            // Create roles with permissions
            createRoles(permissions);

            // Create admin user
            createAdminUser();

            log.info("Database initialization completed!");
        };
    }

    private List<Permission> createPermissions() {
        log.info("Creating permissions...");

        List<String[]> permissionData = Arrays.asList(
            new String[]{"USER_READ", "Read user information"},
            new String[]{"USER_CREATE", "Create new users"},
            new String[]{"USER_UPDATE", "Update existing users"},
            new String[]{"USER_DELETE", "Delete users"},

            new String[]{"ROLE_READ", "Read role information"},
            new String[]{"ROLE_CREATE", "Create new roles"},
            new String[]{"ROLE_UPDATE", "Update existing roles"},
            new String[]{"ROLE_DELETE", "Delete roles"},

            new String[]{"PERMISSION_READ", "Read permission information"},
            new String[]{"PERMISSION_CREATE", "Create new permissions"},
            new String[]{"PERMISSION_UPDATE", "Update existing permissions"},
            new String[]{"PERMISSION_DELETE", "Delete permissions"},

            new String[]{"EVENT_READ", "Read event information"},
            new String[]{"EVENT_CREATE", "Create new events"},
            new String[]{"EVENT_UPDATE", "Update existing events"},
            new String[]{"EVENT_DELETE", "Delete events"},

            new String[]{"BOOKING_READ", "Read booking information"},
            new String[]{"BOOKING_CREATE", "Create new bookings"},
            new String[]{"BOOKING_UPDATE", "Update existing bookings"},
            new String[]{"BOOKING_DELETE", "Delete bookings"},

            new String[]{"PAYMENT_READ", "Read payment information"},
            new String[]{"PAYMENT_CREATE", "Process payments"},
            new String[]{"PAYMENT_UPDATE", "Update payment status"},
            new String[]{"PAYMENT_DELETE", "Delete payments"}
        );

        for (String[] data : permissionData) {
            if (!permissionRepository.existsByName(data[0])) {
                Permission permission = new Permission();
                permission.setName(data[0]);
                permission.setDescription(data[1]);
                permissionRepository.save(permission);
                log.info("Created permission: {}", data[0]);
            }
        }

        return permissionRepository.findAll();
    }

    private void createRoles(List<Permission> allPermissions) {
        log.info("Creating roles...");

        // ADMIN role - all permissions
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator with full access");
            adminRole.setPermissions(new HashSet<>(allPermissions));
            roleRepository.save(adminRole);
            log.info("Created role: ADMIN with all permissions");
        }

        // USER role - basic read permissions
        if (!roleRepository.existsByName("USER")) {
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Regular user with basic access");

            Set<Permission> userPermissions = new HashSet<>();
            for (Permission p : allPermissions) {
                if (p.getName().endsWith("_READ") ||
                    p.getName().equals("BOOKING_CREATE") ||
                    p.getName().equals("BOOKING_UPDATE")) {
                    userPermissions.add(p);
                }
            }
            userRole.setPermissions(userPermissions);
            roleRepository.save(userRole);
            log.info("Created role: USER with basic permissions");
        }

        // EVENT_MANAGER role - manage events
        if (!roleRepository.existsByName("EVENT_MANAGER")) {
            Role eventManagerRole = new Role();
            eventManagerRole.setName("EVENT_MANAGER");
            eventManagerRole.setDescription("Manage events and view bookings");

            Set<Permission> eventPermissions = new HashSet<>();
            for (Permission p : allPermissions) {
                if (p.getName().startsWith("EVENT_") ||
                    p.getName().equals("BOOKING_READ") ||
                    p.getName().equals("PAYMENT_READ")) {
                    eventPermissions.add(p);
                }
            }
            eventManagerRole.setPermissions(eventPermissions);
            roleRepository.save(eventManagerRole);
            log.info("Created role: EVENT_MANAGER");
        }

        // BOOKING_MANAGER role - manage bookings and payments
        if (!roleRepository.existsByName("BOOKING_MANAGER")) {
            Role bookingManagerRole = new Role();
            bookingManagerRole.setName("BOOKING_MANAGER");
            bookingManagerRole.setDescription("Manage bookings and payments");

            Set<Permission> bookingPermissions = new HashSet<>();
            for (Permission p : allPermissions) {
                if (p.getName().startsWith("BOOKING_") ||
                    p.getName().startsWith("PAYMENT_") ||
                    p.getName().equals("EVENT_READ")) {
                    bookingPermissions.add(p);
                }
            }
            bookingManagerRole.setPermissions(bookingPermissions);
            roleRepository.save(bookingManagerRole);
            log.info("Created role: BOOKING_MANAGER");
        }
    }

    private void createAdminUser() {
        log.info("Creating admin user...");

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@ticketflow.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setEnabled(true);

            // Assign ADMIN role
            Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
            log.info("Created admin user - Username: admin, Password: admin123");
            log.warn("WARNING: Please change the default admin password in production!");
        } else {
            log.info("Admin user already exists, skipping creation");
        }
    }
}

