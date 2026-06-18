package com.company.securityapp.config;

import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUserInitializer {

    @Bean
    public CommandLineRunner demoUserCommandLineRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo-users.enabled}") boolean demoUsersEnabled,
            @Value("${app.demo-users.password}") String demoPassword) {
        return args -> {
            if (!demoUsersEnabled) {
                return;
            }

            createUserIfMissing(userRepository, passwordEncoder, "Admin Demo", "admin@securityapp.local", Role.ADMIN, demoPassword);
            createUserIfMissing(userRepository, passwordEncoder, "Staff Demo", "staff@securityapp.local", Role.STAFF, demoPassword);
            createUserIfMissing(userRepository, passwordEncoder, "User Demo", "user@securityapp.local", Role.USER, demoPassword);
        };
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String fullName,
            String email,
            Role role,
            String password) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email.toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);
    }
}
