package com.bleep.learnhub.config;

import com.bleep.learnhub.entity.User;
import com.bleep.learnhub.entity.enums.AccountStatus;
import com.bleep.learnhub.entity.enums.Role;
import com.bleep.learnhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.existsByUsername("admin")) {
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("adminbleepdemo@yopmail.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(Role.SUPER_ADMIN)
                .status(AccountStatus.ACTIVE) // Corrected from Status.ACTIVE to AccountStatus.ACTIVE to match the project schema
                .build();

        userRepository.save(admin);
    }
}
